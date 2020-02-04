package com.morpho.eventmanagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME= "invitations_list.xlsx";
    private static final int DATABASE_VERSION=3;


    public DatabaseOpenHelper(Context context){
        super(new DatabaseContext(context),DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
