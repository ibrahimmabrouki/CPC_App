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
import android.content.SharedPreferences;

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
            //for testing

            //startActivity(new Intent(SplashActivity.this, DoctorActivity.class));
            //startActivity(new Intent(SplashActivity.this, PharmacistActivity.class));
            //startActivity(new Intent(SplashActivity.this, LabTechnicianActivity.class));
            //startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            //startActivity(new Intent(SplashActivity.this, OPT_page.class));
            //startActivity(new Intent(SplashActivity.this, Home_page.class));
            //startActivity(new Intent(SplashActivity.this, CreateAccount.class));
            //startActivity(new Intent(SplashActivity.this, ChangePassword.class));
           //startActivity(new Intent(SplashActivity.this, PatientHomePageActivity.class));


            SharedPreferences prefs = getSharedPreferences("MyAppPreferences", MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
            if (isLoggedIn) {
                String role   = prefs.getString("userType", "");
                String userId = prefs.getString("user_id", "");

                Intent target;
                switch (role) {
                    case "Doctor":
                        target = new Intent(SplashActivity.this, DoctorActivity.class);
                        break;
                    case "Patient":
                        target = new Intent(SplashActivity.this, PatientHomePageActivity.class);
                        break;
                    case "Pharmacist":
                        target = new Intent(SplashActivity.this, PharmacistActivity.class);
                        break;
                    case "Lab Technician":
                        target = new Intent(SplashActivity.this, LabTechnicianActivity.class);
                        break;
                    default:
                        target = new Intent(SplashActivity.this, Home_page.class);
                }
                target.putExtra("user_id", userId);
                startActivity(target);

            } else {
                startActivity(new Intent(SplashActivity.this, Home_page.class));
            }

            finish();
        }, 1800);
    }
}
