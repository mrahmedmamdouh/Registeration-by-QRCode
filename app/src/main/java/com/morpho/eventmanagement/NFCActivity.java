package com.morpho.eventmanagement;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.morpho.eventmanagement.core.Configuration;
import com.morpho.eventmanagement.core.Ticket;

import org.apache.commons.codec.Charsets;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NFCActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private String[][] techLists;
    private IntentFilter[] filters;
    private Intent cardPresentIntent;

    private ProgressDialog progressDialog = null;
    private ProgressDialog waitCardInsertionDialog = null;

    private boolean cardPresent = false;
    private boolean activityVisible = false;

    private TextView txtValidTicketMessage;
    private TextView txtInvalidTicketMessage;
    private ImageView imgTicketValidityStatus;

    private TextView txtTicketNumber;
    private TextView txtTicketOwner;
    private TextView txtValidityDate;
    private TextView txtTicketClass;
    private TextView txtGateNumber;

    private Tag tagFromIntent;
    private MifareClassic mfc;
    private String cardData;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ticket_validation);

        init();
        Click();
    }

    private void init() {
        txtValidTicketMessage = findViewById(R.id.txt_ValidTicket);
        txtInvalidTicketMessage = findViewById(R.id.txt_InvalidTicket);
        imgTicketValidityStatus = findViewById(R.id.img_TicketValidityStatus);

        txtTicketNumber = findViewById(R.id.txt_TicketNumber);
        txtTicketOwner = findViewById(R.id.txt_TicketOwner);
        txtValidityDate = findViewById(R.id.txt_ValidityDate);
        txtTicketClass = findViewById(R.id.txt_Class);
        txtGateNumber = findViewById(R.id.txt_GateNumber);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        if (!Configuration.loadConfiguration()) {
            Toast.makeText(this, "تعذر تحميل ملف الإعدادات الخاصة بالرنامج", Toast.LENGTH_LONG).show();
        }

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        filters = new IntentFilter[]{
                ndef,
        };

        // Setup a tech list for all NfcF tags
        techLists = new String[][]{new String[]{"android.nfc.tech.NfcA"}, new String[]{"android.nfc.tech.MifareClassic"}};
        //------------------------------------------------------------------------------------------
        waitForCard();
    }

    @Override
    protected void onPause() {
//        LogInterface.debug(logger, "onPause Start");

        super.onPause();
        nfcAdapter.disableForegroundDispatch(NFCActivity.this);

//        LogInterface.debug(logger, "onPause End");
    }

    @Override
    protected void onResume() {
//        LogInterface.debug(logger, "onResume Start");

        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, mPendingIntent, filters, techLists);

//        LogInterface.debug(logger, "onResume End");
    }

    @Override
    protected void onNewIntent(Intent intent) {
//        LogInterface.debug(logger, "onNewIntent Start");

        super.onNewIntent(intent);
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            cardPresent = true;
            progressDialog = ProgressDialog.show(NFCActivity.this, "محقق التذاكر", "برجاء الإنتظار حتى يتم قراءة التذكرة المراد التحقق منها");
            //--------------------------------------------------------------------------------------
            tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            tagFromIntent = MiFareClassicTagHandler.patchTag(tagFromIntent);
            //--------------------------------------------------------------------------------------
            final MiFareClassicTagHandler reader = checkForTagAndCreateReader();

            if (reader == null) {
                Toast.makeText(NFCActivity.this, "خطأ أثناء قراءة التذكرة", Toast.LENGTH_LONG).show();
            }

            runOnUiThread(new Thread(() -> {
                byte[] mRawDump = reader.read();

                if (mRawDump != null) {
                    String data = new String(mRawDump, Charsets.US_ASCII);

                    Ticket.validTicket = true;
                    Ticket.ticketNo = data.subSequence(3, 8) + "";
                    Ticket.ticketOwner = data.substring(11, 29) + "";
                } else {
                    Ticket.validTicket = false;
                }

                reader.close();
                //----------------------------------------------------------------------------------
                //----------------------------------------------------------------------------------
                progressDialog.hide();
                cardPresent = false;

                //----------------------------------------------------------------------------------
                if (Ticket.validTicket) {
                    txtValidTicketMessage.setVisibility(View.VISIBLE);
                    txtInvalidTicketMessage.setVisibility(View.GONE);

                    imgTicketValidityStatus.setBackgroundResource(R.drawable.valid);
                    imgTicketValidityStatus.setVisibility(View.VISIBLE);

                    txtTicketNumber.setText(Ticket.ticketNo);
                    txtTicketOwner.setText(Ticket.ticketOwner);
                    txtValidityDate.setText("");
                    txtTicketClass.setText("");
                    txtGateNumber.setText("");
                    progressDialog.dismiss();
                } else {
                    txtValidTicketMessage.setVisibility(View.GONE);
                    txtInvalidTicketMessage.setVisibility(View.VISIBLE);

                    imgTicketValidityStatus.setBackgroundResource(R.drawable.invalid);
                    imgTicketValidityStatus.setVisibility(View.VISIBLE);

                    txtTicketNumber.setText("");
                    txtTicketOwner.setText("");
                    txtValidityDate.setText("");
                    txtTicketClass.setText("");
                    txtGateNumber.setText("");
                }
            }));
        }

//        LogInterface.debug(logger, "onNewIntent End");
    }

    public MiFareClassicTagHandler checkForTagAndCreateReader() {
        MiFareClassicTagHandler reader;
        boolean tagLost = false;

        // Check for tag.
        if (tagFromIntent != null && (reader = MiFareClassicTagHandler.get(tagFromIntent)) != null) {
            try {
                reader.connect();
            } catch (Exception e) {
                tagLost = true;
            }
            if (!tagLost && !reader.isConnected()) {
                reader.close();
                tagLost = true;
            }
            if (!tagLost) {
                return reader;
            }
        }

        return null;
    }

    @Override
    public void onBackPressed() {
//        LogInterface.debug(logger, "onBackPressed Start");

        nfcAdapter.disableForegroundDispatch(NFCActivity.this);
        finish();

//        LogInterface.debug(logger, "onBackPressed End");
    }

    private void waitForCard() {
//        LogInterface.debug(logger, "waitForCard Start");

        waitCardInsertionDialog = ProgressDialog.show(this, "محقق التذاكر", "من فضلك قم بوضع التذكرة المرادالتحقق منها فى أعلى يمين الجهاز حتى يتم قرائتها والتحقق منها");
        //------------------------------------------------------------------------------------------
        try {
            new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if (cardPresent) {
                        cancel();
                        onFinish();
                        waitCardInsertionDialog.dismiss();
                    }
                }

                public void onFinish() {
                    waitCardInsertionDialog.hide();
                    if (!cardPresent) {
                        Toast.makeText(NFCActivity.this, "لم يتم العثور على تذكرة لقرائتها", Toast.LENGTH_LONG).show();
                    }
                }
            }.start();
        } catch (Exception generalException) {
//            LogInterface.error(logger, generalException);
        }

//        LogInterface.debug(logger, "waitForCard End");
    }

    public void onDismissScreen(View v) {
//        LogInterface.debug(logger, "onDismissScreen Start");

        finish();

//        LogInterface.debug(logger, "onDismissScreen End");
    }

    private void Click() {
        
    }
}
