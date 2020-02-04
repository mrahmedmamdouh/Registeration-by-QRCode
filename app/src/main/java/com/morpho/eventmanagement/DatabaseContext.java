package com.morpho.eventmanagement;

/**
 * Created by WDX on 5/12/2019.
 */

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public  class DatabaseContext extends ContextWrapper {

    private static final String DEBUG_CONTEXT = "DatabaseContext";

    public DatabaseContext(Context base) {
        super(base);
    }

    @Override
    public File getDatabasePath(String DATABASE_NAME)  {
        File sdcard = Environment.getExternalStorageDirectory();
        String dbfile = sdcard.getAbsolutePath() + DATABASE_NAME;
        if (!dbfile.endsWith(".xlsx")) {
            dbfile += ".xlsx" ;
        }

        File result = new File(dbfile);

        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs();
        }

        if (Log.isLoggable(DEBUG_CONTEXT, Log.WARN)) {
            Log.w(DEBUG_CONTEXT, "getDatabasePath(" + DATABASE_NAME + ") = " + result.getAbsolutePath());
        }

        return result;
    }

    /* this version is called for android devices >= api-11 */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String DATABASE_NAME, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openOrCreateDatabase(getDatabasePath(DATABASE_NAME).getAbsolutePath(), mode, factory);
    }


}