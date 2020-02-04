package com.morpho.eventmanagement;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.apache.xmlbeans.impl.util.Base64;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class ScanFragment extends AppCompatActivity {

    private IntentIntegrator integrator;
    private TextView vip,category,title,job,entity,email,phone;
    private MaterialButton confirm;
    private IntentResult CodeResult;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor c;
    private String encryptedString,text;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_layout);
        getSupportActionBar().hide();

        init();
        OnClick();
        Scan();

    }

    private void init(){
        integrator = new IntentIntegrator(ScanFragment.this);
        vip = findViewById(R.id.VIP);
        category = findViewById(R.id.Category);
        title = findViewById(R.id.Title);
        job = findViewById(R.id.Job);
        entity = findViewById(R.id.Entity);
        email = findViewById(R.id.Email);
        phone = findViewById(R.id.Phone);
        confirm= findViewById(R.id.confirm);
        dbHelper = new DBHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();
    }

    @SuppressLint("SetTextI18n")
    private void showData() {
        List<String> personView = Arrays.asList(text.split(";"));
        vip.setText(personView.get(0));
        category.setText(personView.get(1));
        title.setText(personView.get(2)+" "+personView.get(4)+ " " +personView.get(3));
        job.setText(personView.get(5));
        entity.setText(personView.get(6));
        email.setText(personView.get(7));
        phone.setText(personView.get(8));
    }

    private void OnClick(){
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertDataIntoTable();
            }
        });

    }

    private void insertDataIntoTable() {
        List<String> insertedRow = Arrays.asList(text.split(";"));
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter1 = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
//        c = db.rawQuery("select distinct  Att_Flag,Date,Time,VIP,Title,Last_Name,First_Name from Guest where Last_Name =  '" + insertedRow.get(3) + "' and First_Name = '"+insertedRow.get(4) +"'",null );
//        if (c != null && c.getCount() > 0) {
            db.execSQL("update Guest set Att_Flag = 'Yes',Date = '" + formatter.format(date) + "',Time='" + formatter1.format(date) + "' where Last_Name= '" + insertedRow.get(3) + "'and First_Name='" + insertedRow.get(4)+"'");
//        }else {
//                    db.execSQL("insert into Guest (Att_Flag,Date,Time,VIP,Title,Last_Name,First_Name) values ('Yes','" + formatter.format(date) + "','" + formatter1.format(date) + "','" + insertedRow.get(0) + "','" + insertedRow.get(2) + "','" + insertedRow.get(3) + "','" + insertedRow.get(4) + "')");
//
//        }
//        db.execSQL("insert into Attendance (Att_Flag,Date,Time,VIP,Title,Last_Name,First_Name) values ('Yes','" + formatter.format(date) + "','" + formatter1.format(date) + "','" + insertedRow.get(0) + "','" + insertedRow.get(2) + "','" + insertedRow.get(3) + "','" + insertedRow.get(4) + "')");
       Scan();
    }

    private void Scan(){
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null&& resultCode==RESULT_OK) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");
            } else {
                //Verification method
                try {
                     encryptedString = result.getContents().substring(result.getContents().lastIndexOf(";")+1);
                     text = result.getContents().substring(0,result.getContents().lastIndexOf(";")+1);
                    Boolean resultDecrypt = decryptRSA(getApplicationContext(),encryptedString,text);
                    if (resultDecrypt){
                        if (CheckInTable()){
                            if (CheckAttendance()){
                                Toast.makeText(getApplicationContext(),"Invitee has been attended before",Toast.LENGTH_SHORT).show();
                                Scan();
                            }else {
                                showData();
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),"Invitee is not on the list",Toast.LENGTH_SHORT).show();
                            Scan();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),"Ticket is not Verified",Toast.LENGTH_SHORT).show();
                        Scan();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            Intent intent = new Intent(ScanFragment.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Boolean CheckAttendance() {
        List<String> personAttendance = Arrays.asList(text.split(";"));
        c = db.rawQuery("select * from Guest where Last_Name = '"+personAttendance.get(3)+"'and First_Name ='"+personAttendance.get(4)+"'and Att_Flag = 'Yes'",null );

//        c = db.rawQuery("select * from Attendance where Last_Name = '"+personAttendance.get(3)+"'and First_Name ='"+personAttendance.get(4)+"'and Att_Flag = 'Yes'",null );
        if (c != null && c.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }

    private Boolean CheckInTable() {
        List<String> personDetails = Arrays.asList(text.split(";"));
        c = db.rawQuery("select * from Guest where Last_Name = '"+personDetails.get(3)+"'and First_Name ='"+personDetails.get(4)+"'and Email = '"+personDetails.get(7)+"'and Phone_Number = '"+personDetails.get(8)+"' ",null );
//        c = db.rawQuery("select * from Attendees where Last_Name = '"+personDetails.get(3)+"'and First_Name ='"+personDetails.get(4)+"'and Email = '"+personDetails.get(7)+"'and Phone_Number = '"+personDetails.get(8)+"' ",null );
        if (c != null && c.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public Boolean decryptRSA(Context mContext, String message,String text) throws Exception {

        InputStream is = mContext.getResources().openRawResource(R.raw.eventrance_public_key);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        List<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = br.readLine()) != null)
            lines.add(line);

        if (lines.size() > 1 && lines.get(0).startsWith("-----") && lines.get(lines.size()-1).startsWith("-----")) {
            lines.remove(0);
            lines.remove(lines.size()-1);
        }

        StringBuilder sb = new StringBuilder();
        for (String aLine: lines)
            sb.append(aLine);
        String keyString = sb.toString();
        Log.d("log", "keyString:"+keyString);
        byte[] keyBytes = Base64.decode(keyString.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(spec);
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(key);
        publicSignature.update(text.getBytes());
        boolean s = publicSignature.verify(android.util.Base64.decode(message, android.util.Base64.DEFAULT));
        Log.e("verify", String.valueOf(s));
        return s;
    }

}
