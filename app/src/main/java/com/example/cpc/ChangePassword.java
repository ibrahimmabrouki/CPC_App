package com.example.cpc;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ChangePassword extends AppCompatActivity {

    EditText newPasswordInput, confirmPasswordInput;
    Button btnChangePassword;

    String contactValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        contactValue = getIntent().getStringExtra("contact_value");
        //contactValue = "ibrahim.mabrouki@lau.edu";
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_password = newPasswordInput.getText().toString().trim();
                String new_confirm_password = confirmPasswordInput.getText().toString().trim();

                if(!isValidPassword(new_password)){
                    Toast.makeText(ChangePassword.this, "Invalid new password", Toast.LENGTH_SHORT).show();
                }
                else if(!isValidPassword(new_confirm_password)) {
                    Toast.makeText(ChangePassword.this, "Invalid new confirm password", Toast.LENGTH_SHORT).show();
                }
                else if(!new_password.equals(new_confirm_password)){
                    Toast.makeText(ChangePassword.this, "Confirm password Does not match", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendPasswordUpdateRequest(contactValue, new_password);
                }
            }
        });

    }

    // Helper method to validate password
    private boolean isValidPassword(String password) {
        return password.length() >= 8; // Example: Minimum length requirement
    }

    private void sendPasswordUpdateRequest(String contactValue, String new_password) {
        String url = "http://10.0.2.2/testfyp/ResetPassword/update_password.php"; // Adjust this path

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("success")) {
                            Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
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
                params.put("new_password", new_password);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}