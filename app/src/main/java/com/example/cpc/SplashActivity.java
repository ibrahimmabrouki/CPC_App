package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Apply fade-in animation
        ImageView logo = findViewById(R.id.logoImage);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
           startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            //startActivity(new Intent(SplashActivity.this, OPT_page.class));

            finish();
        }, SPLASH_TIME_OUT);
    }
}
