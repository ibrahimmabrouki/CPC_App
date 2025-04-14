package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    final String[] tag = {"Email"}; // Default contact value is the "Email"
    TextView logintv3, logintv5, logintv6, logintv7,logintv8;
    EditText login_ed_input, login_ed_password;
    Button loginbt;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logintv3 = findViewById(R.id.logintv3);
        logintv5 = findViewById(R.id.logintv5);
        logintv6 = findViewById(R.id.logintv6);
        logintv7 = findViewById(R.id.logintv7);
        logintv8 = findViewById(R.id.logintv8);
        login_ed_input = findViewById(R.id.login_ed_input);
        login_ed_password = findViewById(R.id.login_ed_password);
        loginbt = findViewById(R.id.loginbt);

        logintv7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logintv3.setText("Phone Number");
                login_ed_input.setHint("Enter your Phone Number");
                login_ed_input.setInputType(InputType.TYPE_CLASS_NUMBER);
                tag[0] = "Phone"; // Update tag to "Email"
                login_ed_input.setText(""); // Reset input
                login_ed_input.setError(null); // Clear any lingering errors
            }
        });

        logintv6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logintv3.setText("Email Address");
                login_ed_input.setHint("Enter your Email Address");
                login_ed_input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                tag[0] = "Email"; // Update tag to "Email"
                login_ed_input.setText(""); // Reset input
                login_ed_input.setError(null); // Clear any lingering errors
            }
        });

        login_ed_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!isValidPassword(login_ed_password.getText().toString().trim()) && login_ed_input.length() > 0){
                    login_ed_password.setError("Invalid Password!");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        login_ed_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                login_ed_input.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String inputText = s.toString().trim();

                if (tag[0].equals("Email")) {
                    // wait for at least 5 characters before validating
                    if (!inputText.isEmpty() && inputText.length() >= 5 && !isValidEmail(inputText)) {
                        login_ed_input.setError("Invalid Email format!");
                    }
                }

                if (tag[0].equals("Phone")) {
                    // wait for full 8 digits before validating
                    if (!inputText.isEmpty() && inputText.length() >= 8 && !isValidPhone(inputText)) {
                        login_ed_input.setError("Invalid Phone Number!");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        //transition to change password activity
        logintv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePasswordntent = new Intent(LoginActivity.this, OPT_page.class);
                startActivity(changePasswordntent);
            }
        });

        //moving to create account activity
        logintv8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccountIntent = new Intent(LoginActivity.this, CreateAccount.class);
                startActivity(createAccountIntent);
            }
        });

        loginbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = login_ed_input.getText().toString().trim();
                String password = login_ed_password.getText().toString().trim();

                if(!isValidEmail(input) && tag[0].equals("Email")){
                    Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                }

                else if(!isValidPhone(input) && tag[0].equals("Phone")){
                    Toast.makeText(getApplicationContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                }

                else if(!isValidPassword(password)){
                    Toast.makeText(getApplicationContext(), "Invalid password", Toast.LENGTH_SHORT).show();
                }

                else{loginUser(input, password);}
            }
        });
    }

    // Helper method to validate password
    private boolean isValidPassword(String password) {
        return password.length() >= 8;
        // Example: Minimum length requirement
    }

    // Helper method to validate phone number
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{8}");
        // Example: 10-digit number validation
    }

    // Helper method to validate email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    private void loginUser(String input, String password) {
        String url = "http://10.0.2.2/testfyp/login.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("Success")) {
                            String[] parts = response.split(":");
                            String userType = parts.length > 1 ? parts[1] : "unknown";

                            //Log.d("SERVER_RESPONSE", userType); used for debugging

                            if (userType.equals("Doctor")) {  //put also id with the intent as an extra
                                Toast.makeText(getApplicationContext(), "Logged in as Doctor", Toast.LENGTH_SHORT).show();
                                // startActivity(new Intent(getApplicationContext(), DoctorDashboard.class));
                            }
                            else if (userType.equals("Labratory")) {
                                Toast.makeText(getApplicationContext(), "Logged in as Labratory", Toast.LENGTH_SHORT).show();
                                // startActivity(new Intent(getApplicationContext(), LabDashboard.class));
                            }
                            else if (userType.equals("Pharmacist")) {
                                Toast.makeText(getApplicationContext(), "Logged in as Pharmacist", Toast.LENGTH_SHORT).show();
                                // startActivity(new Intent(getApplicationContext(), PharmacyDashboard.class));
                            }
                            else if (userType.equals("Patient")) {
                                Toast.makeText(getApplicationContext(), "Logged in as Patient", Toast.LENGTH_SHORT).show();
                                // startActivity(new Intent(getApplicationContext(), PatientDashboard.class));
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Unknown user type: " + userType, Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {
                            Toast.makeText(getApplicationContext(), "login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", password);
                params.put("contact_value", input);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}