package com.morpho.eventmanagement;

import android.nfc.tech.MifareClassic;


import java.io.IOException;

public class CardReader implements Runnable {

    /**
     * Core Logging
     */

    MifareClassic mifareTag;

    public CardReader(MifareClassic mifareTag) {
        this.mifareTag = mifareTag;
    }

    @Override
    public void run() {

        try {
            mifareTag.connect();

            //boolean result = mifareTag.authenticateSectorWithKeyA(1, new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
            boolean result = mifareTag.authenticateSectorWithKeyA(1, new byte[]{(byte) 0xAA, (byte) 0x11, (byte) 0xBB, (byte) 0x22, (byte) 0xCC, (byte) 0x33});

            byte[] readBlock4Cmd = new byte[]{0x30, 0x04};
            short crc4 = getCrc(readBlock4Cmd, readBlock4Cmd.length, (short) 0x8408, (short) 0x0FFFF);
            byte[] data4 = mifareTag.transceive(new byte[]{ 0x30, 0x04, (byte)(crc4 & 0xFF), (byte)((crc4 >> 8) & 0xFF) });

            byte[] readBlock5Cmd = new byte[]{0x30, 0x05};
            short crc5 = getCrc(readBlock5Cmd, readBlock5Cmd.length, (short) 0x8408, (short) 0x0FFFF);
            byte[] data5 = mifareTag.transceive(new byte[]{0x30, 0x05, (byte) (crc5 & 0xFF), (byte) ((crc5 >> 8) & 0xFF)});

            byte[] readBlock6Cmd = new byte[]{0x30, 0x06};
            short crc6 = getCrc(readBlock6Cmd, readBlock6Cmd.length, (short) 0x8408, (short) 0x0FFFF);
            byte[] data6 = mifareTag.transceive(new byte[]{0x30, 0x06, (byte) (crc6 & 0xFF), (byte) ((crc6 >> 8) & 0xFF)});


            mifareTag.close();
        } catch (IOException e) {
        }
    }

    // this function calculates a CRC16 over a unsigned char Array with, LSB first
    // @Param1 (DataBuf): An Array, which contains the Data for Calculation
    // @Param2 (SizeOfDataBuf): length of the Data Buffer (DataBuf)
    // @Param3 (Polynom): Value of the Generatorpolynom, 0x8408 is recommended
    // @Param4 (Initial_Value): load value for CRC16, 0xFFFF is recommended for
    // host to reader communication
    // return: calculated CRC16
    short getCrc(byte[] dataBuf, int sizeOfDataBuf, short polynom, short Initial_Value) {
        short Crc16;
        char Byte_Counter, Bit_Counter;
        Crc16 = Initial_Value;

        for (Byte_Counter = 0; Byte_Counter < sizeOfDataBuf; Byte_Counter++) {
            Crc16 ^= dataBuf[Byte_Counter];

            for (Bit_Counter = 0; Bit_Counter < 8; Bit_Counter++) {
                if ((Crc16 & 0x0001) == 0) {
                    Crc16 >>= 1;
                } else {
                    Crc16 = (short) ((Crc16 >> 1) ^ polynom);
                }
            }
        }

        return (short) (Crc16 & 0xFFFF);
    }

}
