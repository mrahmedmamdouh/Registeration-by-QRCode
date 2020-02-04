package com.morpho.eventmanagement;

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;


import com.morpho.eventmanagement.core.Configuration;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.morpho.eventmanagement.core.Configuration.key;
import static com.morpho.eventmanagement.core.Configuration.keyType;
import static com.morpho.eventmanagement.core.Configuration.loadConfiguration;
import static com.morpho.eventmanagement.core.Configuration.sectorNo;

public class MiFareClassicTagHandler {

    /**
     * Core Logging
     */

    private final MifareClassic mMFC;

    /**
     * Placeholder for not found keys.
     */
    public static final String NO_KEY = "------------";
    /**
     * Placeholder for unreadable blocks.
     */
    public static final String NO_DATA = "--------------------------------";

    /**
     * Initialize a MIFARE Classic reader for the given tag.
     *
     * @param tag The tag to operate on.
     */
    private MiFareClassicTagHandler(Tag tag) {
        MifareClassic tmpMFC = null;
        try {
            tmpMFC = MifareClassic.get(tag);
        } catch (Exception e) {
        }
        mMFC = tmpMFC;
    }

    /**
     * Patch a possibly broken Tag object of HTC One (m7/m8) or Sony
     * Xperia Z3 devices (with Android 5.x.)
     * <p>
     * HTC One: "It seems, the reason of this bug is TechExtras of NfcA is null.
     * However, TechList contains MifareClassic." -- bildin.
     * This method will fix this. For more information please refer to
     * https://github.com/ikarus23/MifareClassicTool/issues/52
     * This patch was provided by bildin (https://github.com/bildin).
     * <p>
     * Sony Xperia Z3 (+ emmulated MIFARE Classic tag): The buggy tag has
     * two NfcA in the TechList with different SAK values and a MifareClassic
     * (with the Extra of the second NfcA). Both, the second NfcA and the
     * MifareClassic technique, have a SAK of 0x20. According to NXP's
     * guidelines on identifying MIFARE tags (Page 11), this a MIFARE Plus or
     * MIFARE DESFire tag. This method creates a new Extra with the SAK
     * values of both NfcA occurrences ORed (as mentioned in NXP's
     * MIFARE type identification procedure guide) and replace the Extra of
     * the first NfcA with the new one. For more information please refer to
     * https://github.com/ikarus23/MifareClassicTool/issues/64
     * This patch was provided by bildin (https://github.com/bildin).
     *
     * @param tag The possibly broken tag.
     * @return The fixed tag.
     */
    public static Tag patchTag(Tag tag) {
        if (tag == null) {
            return null;
        }

        String[] techList = tag.getTechList();

        Parcel oldParcel = Parcel.obtain();
        tag.writeToParcel(oldParcel, 0);
        oldParcel.setDataPosition(0);

        int len = oldParcel.readInt();
        byte[] id = new byte[0];
        if (len >= 0) {
            id = new byte[len];
            oldParcel.readByteArray(id);
        }
        int[] oldTechList = new int[oldParcel.readInt()];
        oldParcel.readIntArray(oldTechList);
        Bundle[] oldTechExtras = oldParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oldParcel.readInt();
        int isMock = oldParcel.readInt();
        IBinder tagService;
        if (isMock == 0) {
            tagService = oldParcel.readStrongBinder();
        } else {
            tagService = null;
        }
        oldParcel.recycle();

        int nfcaIdx = -1;
        int mcIdx = -1;
        short sak = 0;
        boolean isFirstSak = true;

        for (int i = 0; i < techList.length; i++) {
            if (techList[i].equals(NfcA.class.getName())) {
                if (nfcaIdx == -1) {
                    nfcaIdx = i;
                }
                if (oldTechExtras[i] != null
                        && oldTechExtras[i].containsKey("sak")) {
                    sak = (short) (sak
                            | oldTechExtras[i].getShort("sak"));
                    isFirstSak = nfcaIdx == i;
                }
            } else if (techList[i].equals(MifareClassic.class.getName())) {
                mcIdx = i;
            }
        }

        boolean modified = false;

        // Patch the double NfcA issue (with different SAK) for
        // Sony Z3 devices.
        if (!isFirstSak) {
            oldTechExtras[nfcaIdx].putShort("sak", sak);
            modified = true;
        }

        // Patch the wrong index issue for HTC One devices.
        if (nfcaIdx != -1 && mcIdx != -1 && oldTechExtras[mcIdx] == null) {
            oldTechExtras[mcIdx] = oldTechExtras[nfcaIdx];
            modified = true;
        }

        if (!modified) {
            // Old tag was not modivied. Return the old one.
            return tag;
        }

        // Old tag was modified. Create a new tag with the new data.
        Parcel newParcel = Parcel.obtain();
        newParcel.writeInt(id.length);
        newParcel.writeByteArray(id);
        newParcel.writeInt(oldTechList.length);
        newParcel.writeIntArray(oldTechList);
        newParcel.writeTypedArray(oldTechExtras, 0);
        newParcel.writeInt(serviceHandle);
        newParcel.writeInt(isMock);
        if (isMock == 0) {
            newParcel.writeStrongBinder(tagService);
        }
        newParcel.setDataPosition(0);
        Tag newTag = Tag.CREATOR.createFromParcel(newParcel);
        newParcel.recycle();

        return newTag;
    }

    /**
     * Get new instance of {@link MiFareClassicTagHandler}.
     * If the tag is "null" or if it is not a MIFARE Classic tag, "null"
     * will be returned.
     *
     * @param tag The tag to operate on.
     * @return {@link MiFareClassicTagHandler} object or "null" if tag is "null" or tag is
     * not MIFARE Classic.
     */
    public static MiFareClassicTagHandler get(Tag tag) {
        MiFareClassicTagHandler mcr = null;

        if (tag != null) {
            try {
                mcr = new MiFareClassicTagHandler(tag);
                if (!mcr.isMifareClassic()) {
                    return null;
                }
            } catch (RuntimeException ex) {
                // Should not happen. However, it did happen for OnePlus5T
                // user according to Google Play crash reports.
                return null;
            }
        }
        return mcr;
    }

    public boolean isMifareClassic() {
        return mMFC != null;
    }

    public byte[] read() {
        byte[] data = null;

        try {
            if (keyType.compareToIgnoreCase("A") == 0) {
                // Read with key A.
                data = readSector(sectorNo, Hex.decodeHex(key.toCharArray()), false);
            } else if (keyType.compareToIgnoreCase("B") == 0) {
                // Read with key B.
                data = readSector(sectorNo, Hex.decodeHex(key.toCharArray()), true);
            }
        } catch (DecoderException e) {
        } catch (TagLostException e) {
        }

        return data;
    }

    /**
     * Read as much as possible from a sector with the given key.
     * Best results are gained from a valid key B (except key B is marked as
     * readable in the access conditions).
     *
     * @param sectorIndex Index of the Sector to read. (For MIFARE Classic 1K:
     *                    0-63)
     * @param key         Key for authentication.
     * @param useAsKeyB   If true, key will be treated as key B
     *                    for authentication.
     * @return Array of blocks (index 0-3 or 0-15). If a block or a key is
     * marked with {@link #NO_DATA} or {@link #NO_KEY}
     * it means that this data could not be read or found. On authentication error
     * "null" will be returned.
     * @throws TagLostException When connection with/to tag is lost.
     */
    public byte[] readSector(int sectorIndex, byte[] key, boolean useAsKeyB) throws TagLostException {
        boolean auth = authenticate(sectorIndex, key, useAsKeyB);
        byte[] ret = null;

        if (auth) {
            ret = new byte[48];
            //--------------------------------------------------------------------------------------
            int firstBlock = mMFC.sectorToBlock(sectorIndex);
            int lastBlock = firstBlock + 4;
            int index = 0;

            if (mMFC.getSize() == MifareClassic.SIZE_4K
                    && sectorIndex > 31) {
                lastBlock = firstBlock + 16;
            }

            for (int i = firstBlock; i < lastBlock - 1; i++) {
                try {
                    byte blockBytes[] = mMFC.readBlock(i);

                    // mMFC.readBlock(i) must return 16 bytes or throw an error.
                    // At least this is what the documentation says.
                    // On Samsung's Galaxy S5 and Sony's Xperia Z2 however, it
                    // sometimes returns < 16 bytes for unknown reasons.
                    // Update: Aaand sometimes it returns more than 16 bytes...
                    // The appended byte(s) are 0x00.
                    if (blockBytes.length < 16) {
                        throw new IOException();
                    }
                    if (blockBytes.length > 16) {
                        blockBytes = Arrays.copyOf(blockBytes, 16);
                    }

                    System.arraycopy(blockBytes, 0, ret, index, blockBytes.length);
                    index = index + 16;
                } catch (IOException e) {

                    // Could not read block.
                    // (Maybe due to key/authentication method.)

                    if (!mMFC.isConnected()) {
                        throw new TagLostException("Tag removed during readSector(...)");
                    }

                    // After an error, a re-authentication is needed.
                    authenticate(sectorIndex, key, useAsKeyB);
                    //------------------------------------------------------------------------------
                    ret = null;
                    break;
                }
            }
        }

        return ret;
    }

    // TODO: Make this a function with three return values.
    // 0 = Auth. successful.
    // 1 = Auth. not successful.
    // 2 = Error. Most likely tag lost.
    // Once done, update the code of buildNextKeyMapPart().
    /**
     * Authenticate with given sector of the tag.
     * @param sectorIndex The sector with which to authenticate.
     * @param key Key for the authentication.
     * @param useAsKeyB If true, key will be treated as key B
     * for authentication.
     * @return True if authentication was successful. False otherwise.
     */
    private boolean authenticate(int sectorIndex, byte[] key,
                                 boolean useAsKeyB) {
        // Fetch the retry authentication option. Some tags and
        // devices have strange issues and need a retry in order to work...
        // Info: https://github.com/ikarus23/MifareClassicTool/issues/134
        // and https://github.com/ikarus23/MifareClassicTool/issues/106
        boolean retryAuth = false;
        int retryCount = 1;
        boolean ret = false;

        for (int i = 0; i < retryCount+1; i++) {
            try {
                if (!useAsKeyB) {
                    // Key A.
                    ret = mMFC.authenticateSectorWithKeyA(sectorIndex, key);
                } else {
                    // Key B.
                    ret = mMFC.authenticateSectorWithKeyB(sectorIndex, key);
                }
            } catch (IOException e) {
                return false;
            }
            // Retry?
            if (ret || !retryAuth) {
                break;
            }
        }
        return ret;
    }

    /**
     * Check if key B is readable.
     * Key B is readable for the following configurations:
     * <ul>
     * <li>C1 = 0, C2 = 0, C3 = 0</li>
     * <li>C1 = 0, C2 = 0, C3 = 1</li>
     * <li>C1 = 0, C2 = 1, C3 = 0</li>
     * </ul>
     * @param ac The access conditions (4 bytes).
     * @return True if key B is readable. False otherwise.
     */
    private boolean isKeyBReadable(byte[] ac) {
        byte c1 = (byte) ((ac[1] & 0x80) >>> 7);
        byte c2 = (byte) ((ac[2] & 0x08) >>> 3);
        byte c3 = (byte) ((ac[2] & 0x80) >>> 7);
        return c1 == 0
                && (c2 == 0 && c3 == 0)
                || (c2 == 1 && c3 == 0)
                || (c2 == 0 && c3 == 1);
    }

    /**
     * Connect the reader to the tag. If the reader is already connected the
     * "connect" will be skipped. If "connect" will block for more than 500ms
     * then connecting will be aborted.
     * @throws Exception Something went wrong while connecting to the tag.
     */
    public void connect() throws Exception {
        final AtomicBoolean error = new AtomicBoolean(false);

        // Do not connect if already connected.
        if (isConnected()) {
            return;
        }

        // Connect in a worker thread. (connect() might be blocking).
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMFC.connect();
                } catch (IOException | IllegalStateException ex) {
                    error.set(true);
                }
            }
        });
        t.start();

        // Wait for the connection (max 500millis).
        try {
            t.join(500);
        } catch (InterruptedException ex) {
            error.set(true);
        }

        // If there was an error log it and throw an exception.
        if (error.get()) {
            throw new Exception("Error while connecting to tag.");
        }
    }

    /**
     * Check if the reader is connected to the tag.
     * @return True if the reader is connected. False otherwise.
     */
    public boolean isConnected() {
        return mMFC.isConnected();
    }

    /**
     * Close the connection between reader and tag.
     */
    public void close() {
        try {
            mMFC.close();
        }
        catch (IOException e) {
        }
    }
}
