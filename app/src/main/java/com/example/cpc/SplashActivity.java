package com.example.cpc;

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

        frame1.setTranslationX(-400f);
        frame2.setTranslationX(400f);

        finalImage.setVisibility(View.INVISIBLE);
        finalImage.setAlpha(0f);

        frame1.animate()
                .translationXBy(400f)
                .setDuration(1000)
                .start();

        frame2.animate()
                .translationXBy(-400f)
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
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000);
    }
}
