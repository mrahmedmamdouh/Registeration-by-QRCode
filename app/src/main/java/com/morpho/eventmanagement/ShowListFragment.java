package com.morpho.eventmanagement;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShowListFragment extends AppCompatActivity {
    private View view;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    InputStream inputStream;
    private ArrayList<String[]> listdata;
    private String[] data;
    Thread thread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    private MaterialButton back,print;
    private CustomAdapter adapter;
    private RecyclerView recyclerView;
    private ArrayList<Attendance> attendeesList;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor c;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_list);

        init();
        onClick();
    }

    private void init() {
        getSupportActionBar().hide();
        back = findViewById(R.id.back);
        print = findViewById(R.id.print);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), new LinearLayoutManager(this).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
        attendeesList = new ArrayList<>();
        listdata = new ArrayList<>();
        PopulateRecyclerView();
    }

    private void onClick(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShowListFragment.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               extractSheet(getApplicationContext(),"Event History.xls");
            }
        });
    }

    private void PopulateRecyclerView(){
        c = db.rawQuery("select distinct  Att_Flag,Date,Time,VIP,Title,Last_Name,First_Name from Guest where Att_Flag = 'Yes'",null );

//        c = db.rawQuery("select distinct  Att_Flag,Date,Time,VIP,Title,Last_Name,First_Name from Attendance",null );
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String Att_Flag = c.getString(c.getColumnIndex("Att_Flag"));
            String Date = c.getString(c.getColumnIndex("Date"));
            String time = c.getString(c.getColumnIndex("Time"));
            String first_name = c.getString(c.getColumnIndex("First_Name"));
            String Last_Name = c.getString(c.getColumnIndex("Last_Name"));
            String vip = c.getString(c.getColumnIndex("VIP"));
                String Title = c.getString(c.getColumnIndex("Title"));
                Attendance attendees = new Attendance(Att_Flag,Date,time,vip,Last_Name,first_name,Title);
            attendeesList.add(attendees);
        }}
        adapter = new CustomAdapter(getApplicationContext(),attendeesList);
        recyclerView.setAdapter(adapter);
    }

    public boolean extractSheet(Context context, String fileName) {

        listdata.clear();
        c = db.rawQuery("select distinct Att_Flag,Date ,Time ,VIP, Title,Last_Name ,First_Name from Guest ", null);

//        c = db.rawQuery("select distinct Att_Flag,Date ,Time ,VIP, Title,Last_Name ,First_Name from Attendance ", null);
        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String Att_Flag = c.getString(c.getColumnIndex("Att_Flag"));
                String Date = c.getString(c.getColumnIndex("Date"));
                String Time = c.getString(c.getColumnIndex("Time"));
                String VIP = c.getString(c.getColumnIndex("VIP"));
                String Name = c.getString(c.getColumnIndex("Title"))+" "+c.getString(c.getColumnIndex("First_Name"))+" "+c.getString(c.getColumnIndex("Last_Name"));
                data = new String[]{VIP,Name,Att_Flag,Date,Time};
                listdata.add(data);
            }
        }
        c.close();
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return false;
        }
        boolean success = false;
        //New Workbook
        Workbook wb = new HSSFWorkbook();
        Cell c = null;


        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet("Guest List");

        // Generate column headings
        Row header = sheet1.createRow(0);
        c = header.createCell(0);
        c.setCellValue("VIP (YES/NO)");
        c = header.createCell(1);
        c.setCellValue("Name");
        c = header.createCell(2);
        c.setCellValue("Attended");
        c = header.createCell(3);
        c.setCellValue("Date");
        c = header.createCell(4);
        c.setCellValue("Time");
        for (int i = 0; i <listdata.size() ; i++) {
            Row row = sheet1.createRow(i+1);
            for (int j = 0; j <listdata.get(i).length ; j++) {
                c = row.createCell(j);
                c.setCellValue(listdata.get(i)[j]);
            }
        }

        // Create a path where we will place our List of objects on external storage

        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath());
        dir.mkdirs();
        File file = new File(dir, fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
            Toast.makeText(getApplicationContext(),"Excel File Created Successfully",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }
        return success;
    }
    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
