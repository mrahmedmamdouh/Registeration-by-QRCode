package com.morpho.eventmanagement;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    getSupportActionBar().hide();
     setContentView(R.layout.activity_splash);
        IntentLauncher intentLauncher = new IntentLauncher();
        intentLauncher.start();


    }
    private class IntentLauncher extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                ObjectAnimator animation = ObjectAnimator.ofFloat(R.layout.activity_splash, "translationX", 1000f);
                animation.start();
            } catch (Exception e) {
//                Toast.makeText(Splash.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent(Splash.this, MainDashBoard.class);
            Splash.this.startActivity(intent);
            Splash.this.finish();
        }
    }
}
