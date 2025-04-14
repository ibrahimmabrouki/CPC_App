package com.example.cpc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Home_page extends AppCompatActivity {

    Button make_appointment, homepage_login, view_doctors, view_services, view_contact;
    Button backToHome, contactBackBtn ;
    LinearLayout homeLayout, contact_layout;
    ScrollView servicesLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Initialize all views
        make_appointment = findViewById(R.id.make_appointment);
        homepage_login = findViewById(R.id.homepage_login);
        view_doctors = findViewById(R.id.view_doctors);
        view_services = findViewById(R.id.view_services);
        view_contact = findViewById(R.id.view_contact);

        homeLayout = findViewById(R.id.homeLayout);

        servicesLayout = findViewById(R.id.servicesLayout);
        backToHome = findViewById(R.id.backToHome);

        View contactView = getLayoutInflater().inflate(R.layout.contact_section, null);
        ((FrameLayout) findViewById(R.id.main)).addView(contactView);
        contact_layout = contactView.findViewById(R.id.contactLayout);
        contactBackBtn = contactView.findViewById(R.id.contactBackToHome);
        LinearLayout phoneCard = contactView.findViewById(R.id.phoneCard);
        LinearLayout emailCard = contactView.findViewById(R.id.emailCard);



        // Handle appointment button
        make_appointment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Handle login button
        homepage_login.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });


        // Show Services Overlay
        view_services.setOnClickListener(v -> {
            servicesLayout.setVisibility(View.VISIBLE);
        });

        // Back to Home (hide overlay)
        backToHome.setOnClickListener(v -> {
            servicesLayout.setVisibility(View.GONE);
        });


        view_contact.setOnClickListener(v -> {
            contact_layout.setVisibility(View.VISIBLE);
        });
        contactBackBtn.setOnClickListener(v -> {
            contact_layout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        });
        phoneCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+9611234567"));
                startActivity(intent);
            }
        });
        emailCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:cpcclinc3@gmail.com"));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
            }
            else {
                //Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

