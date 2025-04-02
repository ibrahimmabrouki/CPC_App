package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class OPT_page extends AppCompatActivity {
    EditText contactInput, otpInput;
    Button verifyBtn;
    TextView sendcontactinfo, resendText;

    final boolean[] contactExists = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_opt_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        contactInput = findViewById(R.id.contactInput);
        otpInput = findViewById(R.id.otpInput);
        verifyBtn = findViewById(R.id.verifyBtn);
        sendcontactinfo = findViewById(R.id.sendcontactinfo);
        resendText = findViewById(R.id.resendText);

        sendcontactinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactInfo = contactInput.getText().toString().trim();
                if(TextUtils.isEmpty(contactInfo)){
                    Toast.makeText(OPT_page.this, "Please enter a valid email or phone number!", Toast.LENGTH_SHORT).show();
                }

                if (isProbablyEmail(contactInfo) && isValidEmail(contactInfo)) {
                    Toast.makeText(getApplicationContext(), "this is a valid email", Toast.LENGTH_SHORT).show();
                    checkIfContactExists(contactInfo);
                }
                else if (isProbablyPhone(contactInfo) && isValidPhone(contactInfo)) {
                    checkIfContactExists(contactInfo);

                }
                else {
                    // Invalid input
                    Toast.makeText(getApplicationContext(), "Please enter a valid email or phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //initially make the button disabled
        verifyBtn.setEnabled(false);
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                verifyOtp(contactInput.getText().toString().trim(), otpInput.getText().toString().trim());
            }
        });

        otpInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int l = otpInput.getText().toString().trim().length();
                if( l == 6 && contactExists[0] == true){
                    verifyBtn.setEnabled(true);
                }
                else if(l > 6 || l <6){
                    verifyBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        resendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactExists[0] = false;
                verifyBtn.setEnabled(false);
                otpInput.setText("");
                String contactInfo = contactInput.getText().toString().trim();
                if(TextUtils.isEmpty(contactInfo)){
                    Toast.makeText(OPT_page.this, "Please enter a valid email or phone number!", Toast.LENGTH_SHORT).show();
                }

                else if (isProbablyEmail(contactInfo) && isValidEmail(contactInfo)) {
                    Toast.makeText(getApplicationContext(), "this is a valid email", Toast.LENGTH_SHORT).show();
                    checkIfContactExists(contactInfo);
                }
                else if (isProbablyPhone(contactInfo) && isValidPhone(contactInfo)) {
                    checkIfContactExists(contactInfo);

                }
                else {
                    // Invalid input
                    Toast.makeText(getApplicationContext(), "Please enter a valid email or phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public boolean isProbablyEmail(String input) {
        return input.contains("@");
    }

    public boolean isProbablyPhone(String input) {
        return !input.contains("@");
    }

    // Helper method to validate email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Helper method to validate phone number
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{8}"); // Example: 10-digit number validation
    }

    private void checkIfContactExists(String contactValue) {
        String url = "http://10.0.2.2/testfyp/check_contact.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("true")) {
                            requestOtp(contactValue);
                            contactExists[0] = true;
                        }

                        else {
                            Toast.makeText(OPT_page.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            contactExists[0] = false;
                            contactInput.setError("user not exists!");
                            // Maybe show an error or block next step
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        contactInput.setError("user not exists!");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("contact_value", contactValue);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void requestOtp(String contactValue) {
        String url = "http://10.0.2.2/testfyp/send_otp.php"; // Update this if on real server

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contact_value", contactValue);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void verifyOtp(String contactValue, String otpCode) {
        String url = "http://10.0.2.2/testfyp/ResetPassword/verify_otp.php"; // Update this to your correct server path

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("true")) {
                            // You can now allow the user to reset the password
                            Intent change_password = new Intent(getApplicationContext(), ChangePassword.class);
                            change_password.putExtra("contact_value", contactValue);
                            startActivity(change_password);
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid or expired OTP.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contact_value", contactValue);
                params.put("code", otpCode);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

}