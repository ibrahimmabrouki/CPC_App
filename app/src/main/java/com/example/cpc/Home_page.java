package com.example.cpc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    Button contactBackBtn, serviceBackBtn, btnMyRecords;
    LinearLayout homeLayout, contact_layout;
    ScrollView service_layout;

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

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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
        btnMyRecords = findViewById(R.id.btnMyRecords);

        // Inflate contact section into the main layout
        View contactView = getLayoutInflater().inflate(R.layout.contact_section, (ViewGroup) findViewById(R.id.main), false);
        ((ViewGroup) findViewById(R.id.main)).addView(contactView);
        contact_layout = contactView.findViewById(R.id.contactLayout);
        contactBackBtn = contactView.findViewById(R.id.contactBackToHome);
        LinearLayout phoneCard = contactView.findViewById(R.id.phoneCard);
        LinearLayout emailCard = contactView.findViewById(R.id.emailCard);

        // Inflate services section into the main layout
        View serviceView = getLayoutInflater().inflate(R.layout.services_section, (ViewGroup) findViewById(R.id.main), false);
        ((ViewGroup) findViewById(R.id.main)).addView(serviceView);
        service_layout = serviceView.findViewById(R.id.servicesLayout);
        serviceBackBtn = serviceView.findViewById(R.id.serviceBackBtn);

        // Open login screen
        make_appointment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Open login screen
        homepage_login.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Open login screen
        btnMyRecords.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Show contact section
        view_contact.setOnClickListener(v -> {
            contact_layout.setVisibility(View.VISIBLE);
            homeLayout.setVisibility(View.GONE);
        });

        // Back from contact section
        contactBackBtn.setOnClickListener(v -> {
            contact_layout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        });

        // Show services section
        view_services.setOnClickListener(v -> {
            service_layout.setVisibility(View.VISIBLE);
            homeLayout.setVisibility(View.GONE);
        });

        // Back from services section
        serviceBackBtn.setOnClickListener(v -> {
            service_layout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);
        });

        // Open dialer
        phoneCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+9611234567"));
            startActivity(intent);
        });

        // Send email
        emailCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:cpcclinc3@gmail.com"));
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
