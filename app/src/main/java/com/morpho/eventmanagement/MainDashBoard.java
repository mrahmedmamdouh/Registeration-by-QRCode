package com.morpho.eventmanagement;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainDashBoard extends AppCompatActivity {
    private MaterialButton BarcodeTicket,E_Ticket;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    String VIP = "",Title="", CATEGORY = "", Last_Name = "",First_Name = "",Fonction = "",Entity = "",Email = "",Phone_Number = "",barcode="ahahahahahahahahaha";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_dashboard);

        init();
        Click();
    }

    private void init() {
        BarcodeTicket = findViewById(R.id.BarcodeTicket);
        E_Ticket = findViewById(R.id.E_Ticket);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        showDialog(MainDashBoard.this);

    }

    private void Click() {
        BarcodeTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MainDashBoard.this,MainActivity.class);
                startActivity(intent1);
            }
        });

        E_Ticket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(MainDashBoard.this,NFCActivity.class);
                startActivity(intent2);
            }
        });
    }

    private void OpenAndCheckExcelSheet () throws FileNotFoundException {

        try {
            File dbFile = new File(Environment.getExternalStorageDirectory() + "/invitations_list.xlsx");
            FileInputStream excelFile = new FileInputStream(dbFile);
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                if (currentRow.getRowNum() > 2){
                    Iterator<Cell> cellIterator = currentRow.iterator();
                    int colno = 0;

                    while (cellIterator.hasNext()) {

                        Cell myCell = cellIterator.next();
                        if (colno == 0) {
                            VIP = myCell.toString();
                        } else if (colno == 1) {
                            CATEGORY = myCell.toString();
                        } else if (colno == 2) {
                            Title = myCell.toString();
                        }else if (colno == 3) {
                            Last_Name = myCell.toString();
                        } else if (colno == 4) {
                            First_Name = myCell.toString();
                        } else if (colno == 5) {
                            Fonction = myCell.toString();
                        } else if (colno == 6) {
                            Entity = myCell.toString();
                        } else if (colno == 7) {
                            Email = myCell.toString();
                        } else if (colno == 8) {
                            Phone_Number = myCell.toString();
                            if(!VIP.equals("") && !CATEGORY.equals("") && !Last_Name.equals("") && !First_Name.equals("") && !Fonction.equals("") && !Entity.equals("") && !Email.equals("") && !Phone_Number.equals("")) {
                                db.execSQL("insert into Guest(VIP,CATEGORY,Title,Last_Name,First_Name,Fonction,Entity,Email,Phone_Number,Att_Flag,Date,Time) values ('" + VIP + "','" + CATEGORY + "','" + Title + "','" + Last_Name + "','" + First_Name + "','" + Fonction + "','" + Entity + "','" + Email + "','" + Phone_Number + "','No','---','---')");

//                            db.execSQL("insert into Attendees(VIP,CATEGORY,Title,Last_Name,First_Name,Fonction,Entity,Email,Phone_Number) values ('" + VIP + "','" + CATEGORY + "','" + Title + "','" + Last_Name + "','" + First_Name + "','" + Fonction + "','" + Entity + "','" + Email + "','" + Phone_Number + "')");
//                            SharedPreferences.Editor editor = getSharedPreferences("ExcelSheet", MODE_PRIVATE).edit();
//                            editor.putBoolean("inserted", true);
//                            editor.apply();
                            }}
                        colno++;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Please Make Sure the Excel Sheet in the right path",Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog(Activity activity){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialoge_layout);

        Button loadData = (Button) dialog.findViewById(R.id.load);
        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    OpenAndCheckExcelSheet();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        Button DeleteData = (Button) dialog.findViewById(R.id.delete);
        DeleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.execSQL("delete from Guest");
//                db.execSQL("delete from Attendance");
                dialog.dismiss();

            }
        });
        Button Cancel = (Button) dialog.findViewById(R.id.cancel);
        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
