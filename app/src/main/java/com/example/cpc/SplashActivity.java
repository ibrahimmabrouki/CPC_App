package com.example.cpc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView frame1 = findViewById(R.id.frame1);
        ImageView frame2 = findViewById(R.id.frame2);
        ImageView finalImage = findViewById(R.id.finalImage);

        frame1.setTranslationX(-500f);
        frame2.setTranslationX(500f);

        finalImage.setVisibility(View.INVISIBLE);
        finalImage.setAlpha(0f);

        frame1.animate()
                .translationXBy(500f)
                .setDuration(1000)
                .start();

        frame2.animate()
                .translationXBy(-500f)
                .setDuration(1000)
                .withEndAction(() -> {
                    frame1.setVisibility(View.GONE);
                    frame2.setVisibility(View.GONE);
                    finalImage.setVisibility(View.VISIBLE);
                    finalImage.setScaleX(0.8f);
                    finalImage.setScaleY(0.8f);
                    finalImage.animate().alpha(1f).setDuration(600).start();
                })
                .start();

        new Handler().postDelayed(() -> {
            //startActivity(new Intent(SplashActivity.this, DoctorActivity.class));
            //startActivity(new Intent(SplashActivity.this, PharmacistActivity.class));
            startActivity(new Intent(SplashActivity.this, LabTechnicianActivity.class));
            //startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            //startActivity(new Intent(SplashActivity.this, OPT_page.class));
            //startActivity(new Intent(SplashActivity.this, Home_page.class));
            //startActivity(new Intent(SplashActivity.this, CreateAccount.class));
            //startActivity(new Intent(SplashActivity.this, ChangePassword.class));



            finish();
        }, 1800);
    }
}
