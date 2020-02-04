package com.morpho.eventmanagement;

import android.app.Application;
import android.content.Context;
import android.os.Build;

public class Program extends Application {
    /**
     * Core Logging
     */

    /**
     * Application Context
     */
    public static Context applicationContext;

    @Override
    public void onCreate() {



        super.onCreate();
        //-----------------------------------------------------------------------
       
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private String getAndroidVersion(int sdk) {
        switch (sdk) {
            case 1:
                return "Android 1.0";
            case 2:
                return "Petit Four (Android 1.1)";
            case 3:
                return "Cupcake (Android 1.5)";
            case 4:
                return "Donut (Android 1.6)";
            case 5:
                return "Eclair (Android 2.0)";
            case 6:
                return "Eclair (Android 2.0.1)";
            case 7:
                return "Eclair (Android 2.1)";
            case 8:
                return "Froyo (Android 2.2)";
            case 9:
                return "Gingerbread (Android 2.3)";
            case 10:
                return "Gingerbread (Android 2.3.3)";
            case 11:
                return "Honeycomb (Android 3.0)";
            case 12:
                return "Honeycomb (Android 3.1)";
            case 13:
                return "Honeycomb (Android 3.2)";
            case 14:
                return "Ice Cream Sandwich (Android 4.0)";
            case 15:
                return "Ice Cream Sandwich (Android 4.0.3)";
            case 16:
                return "Jelly Bean (Android 4.1)";
            case 17:
                return "Jelly Bean (Android 4.2)";
            case 18:
                return "Jelly Bean (Android 4.3)";
            case 19:
                return "KitKat (Android 4.4)";
            case 20:
                return "KitKat Watch (Android 4.4)";
            case 21:
                return "Lollipop (Android 5.0)";
            case 22:
                return "Lollipop (Android 5.1)";
            case 23:
                return "Marshmallow (Android 6.0)";
            default:
                return "";
        }
    }
}
