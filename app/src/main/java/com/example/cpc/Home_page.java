package com.example.cpc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class Home_page extends AppCompatActivity {

    Button homepage_login, view_doctors, view_services, view_contact;
    Button contactBackBtn, serviceBackBtn, doctorBackBtn;
    LinearLayout homeLayout, contact_layout, doctorlayout;
    ScrollView service_layout;
    ListView doctors_lv;
    MaterialToolbar topAppBar;
    private List<String> doctorsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(view -> {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.main),
                    "• Doctors: View staff\n• Services: Explore options\n• Contact Us: Get in touch\n• Login: Access your account",
                    Snackbar.LENGTH_INDEFINITE);

            // Get the Snackbar view
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#153558")); // Optional if you're not using drawable

            // Move to top or center
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = Gravity.TOP;  // Or Gravity.CENTER
            params.setMargins(32, 250, 32, 0);
            snackbarView.setLayoutParams(params);

            // Style the text
            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(18);
            textView.setMaxLines(5);

            // 🔽 SHOW THE SNACKBAR
            snackbar.show();

            // ✅ SET ROUNDED BACKGROUND AFTER SHOW
            snackbarView.setBackground(ContextCompat.getDrawable(Home_page.this, R.drawable.snackbar_background));

            // Auto dismiss after 5 sec
            new android.os.Handler().postDelayed(snackbar::dismiss, 5000);
        });

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
        homepage_login = findViewById(R.id.homepage_login);
        view_doctors = findViewById(R.id.view_doctors);
        view_services = findViewById(R.id.view_services);
        view_contact = findViewById(R.id.view_contact);
        homeLayout = findViewById(R.id.homeLayout);

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

        //Inflate services section into the main layout
        View doctorView = getLayoutInflater().inflate(R.layout.doctor_section, (ViewGroup) findViewById(R.id.main), false);
        ((ViewGroup) findViewById(R.id.main)).addView(doctorView);
        doctorlayout = doctorView.findViewById(R.id.doctorLayout);
        doctorBackBtn = doctorView.findViewById(R.id.doctorBackBtn);
        doctors_lv = doctorView.findViewById(R.id.doctors_lv);




        // Open login screen


        // Open login screen
        homepage_login.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Open login screen


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

        // Show doctors section
        view_doctors.setOnClickListener(v -> {
            doctorlayout.setVisibility(View.VISIBLE);
            homeLayout.setVisibility(View.GONE);
            getDoctorsFromServer();
        });

        // Back from doctors section
        doctorBackBtn.setOnClickListener(v -> {
            doctorlayout.setVisibility(View.GONE);
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
    private void getDoctorsFromServer() {
        String url = "http://10.21.139.29/clinic/get_doctors.php";
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Clear any previous data in the list
                            doctorsList.clear();

                            // Process each doctor's name in the JSON array
                            for (int i = 0; i < response.length(); i++) {
                                String doctorName = response.getString(i);
                                // Add doctor name to the list
                                doctorsList.add(doctorName);
                            }

                            DoctorCustomeAdapter doctor_info = new DoctorCustomeAdapter(getApplicationContext(), doctorsList);
                            doctors_lv.setAdapter(doctor_info);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Parsing error!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle network errors
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the queue
        requestQueue.add(jsonArrayRequest);
    }


}
