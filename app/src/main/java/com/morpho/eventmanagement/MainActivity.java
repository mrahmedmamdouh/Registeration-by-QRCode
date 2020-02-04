package com.morpho.eventmanagement;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private MaterialButton scan,showList;
    private FileInputStream fis;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        Click();
    }

    private void init() {
        getSupportActionBar().hide();
        scan = findViewById(R.id.scan);
        showList = findViewById(R.id.showList);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        SharedPreferences prefs = getSharedPreferences("ExcelSheet", MODE_PRIVATE);
//        boolean isinserted = prefs.getBoolean("inserted",false);
//        if (isinserted = f)

    }

    private void Click(){

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ScanFragment.class);
                startActivity(intent);

            }
        });
        showList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,ShowListFragment.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();}
}
