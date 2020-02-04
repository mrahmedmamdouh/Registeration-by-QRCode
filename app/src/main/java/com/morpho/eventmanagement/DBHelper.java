package com.morpho.eventmanagement;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

     static final String DATABASE_NAME = "Eventrance.db";
     static final int DATABASE_VERSION = 1;
    static final String TB_NAME="Attendees";
    static final String TB_NAME1="Attendance";
    static final String TB_NAME2="Guest";
    static final String CREATE_TBL = "Create Table Attendees (ROW_ID INTEGER PRIMARY KEY AUTOINCREMENT,"+"VIP text NOT NULL,"+"CATEGORY text NOT NULL,"+"Title text NOT NULL,"+" Last_Name text NOT NULL,"+"First_Name text NOT NULL,"+"Fonction text NOT NULL,"+"Entity text NOT NULL,"+"Email text NOT NULL,"+"Phone_Number text NOT NULL);";
    static final String CREATE_TBL1 = "Create Table Attendance (ROW_ID INTEGER PRIMARY KEY AUTOINCREMENT,"+"Att_Flag text NOT NULL,"+"Date text NOT NULL,"+"Time text NOT NULL,"+"VIP text NOT NULL,"+"Title text NOT NULL,"+" Last_Name text NOT NULL,"+"First_Name text NOT NULL);";
    static final String CREATE_TBL2 = "Create Table Guest (ROW_ID INTEGER PRIMARY KEY AUTOINCREMENT,"+"VIP text NOT NULL,"+"CATEGORY text NOT NULL,"+"Title text NOT NULL,"+" Last_Name text NOT NULL,"+"First_Name text NOT NULL,"+"Fonction text NOT NULL,"+"Entity text NOT NULL,"+"Email text NOT NULL,"+"Phone_Number text NOT NULL,"+"Att_Flag text NOT NULL,"+"Date text NOT NULL,"+"Time text NOT NULL);";
    static final String DROP_TB="DROP TABLE IF EXISTS "+TB_NAME;
    static final String DROP_TB1="DROP TABLE IF EXISTS "+TB_NAME1;
    static final String DROP_TB2="DROP TABLE IF EXISTS "+TB_NAME2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            db.execSQL(CREATE_TBL);
            db.execSQL(CREATE_TBL1);
            db.execSQL(CREATE_TBL2);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Attendees");
        db.execSQL("drop table if exists Attendance");
        db.execSQL("drop table if exists Guest");

        try {
            db.execSQL(DROP_TB);
            db.execSQL(DROP_TB1);
            db.execSQL(DROP_TB2);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        onCreate(db);
    }

    public void deleteTable(String TB_NAME) {

        String selectQuery = "DELETE FROM " + TB_NAME;

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL(selectQuery);
    }

    public void insertData(String TB_NAME) {

//        String selectQuery = "INSERT INTO '" + TB_NAME +"'""]";

        SQLiteDatabase db = this.getWritableDatabase();

//        db.execSQL(selectQuery);
    }

}
