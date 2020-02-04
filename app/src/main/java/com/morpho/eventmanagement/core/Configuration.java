package com.morpho.eventmanagement.core;




import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;


public class Configuration {

    /**
     * Core Logging
     */

    public static int sectorNo;
    public static int blockNo;
    public static String keyType;
    public static String key;

    public static boolean loadConfiguration() {
//        LogInterface.debug(logger, "loadConfiguration Start");

        boolean result = true;

        try {
            String jsonConfiguration = FileUtils.readFileToString(new File("/storage/sdcard0/TicketConfiguration.json"), "UTF-8");
            JSONObject config = new JSONObject(jsonConfiguration);

            sectorNo = config.getInt("sectorNo");
            blockNo = config.getInt("blockNo");
            keyType = config.getString("keyType");
            key = config.getString("key");
        } catch (Exception e) {
//            LogInterface.error(logger, e);
            result = false;
        }

//        LogInterface.debug(logger, "loadConfiguration End [" + result + "]");
        return result;
    }
}
