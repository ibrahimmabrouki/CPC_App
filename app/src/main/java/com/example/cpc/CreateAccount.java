package com.example.cpc;

import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class CreateAccount extends AppCompatActivity {

    EditText create_ed_username, create_ed_input_cred, create_ed_password, create_ed_confirm;
    TextView createtv_email_address, createtv_phone_number, createtv_login;
    Button btsignunp;
    final String[] tag = {"Email"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        create_ed_username = findViewById(R.id.create_ed_username);
        create_ed_input_cred = findViewById(R.id.create_ed_input_cred);
        create_ed_password = findViewById(R.id.create_ed_password);
        create_ed_confirm = findViewById(R.id.create_ed_confirm);
        createtv_email_address = findViewById(R.id.createtv_email_address);
        createtv_phone_number = findViewById(R.id.createtv_phone_number);
        createtv_login = findViewById(R.id.createtv_login);
        btsignunp = findViewById(R.id.btsignunp);

        create_ed_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(create_ed_username.length() > 0){
                    if(!isValidUsername(create_ed_username.getText().toString().trim())){
                        create_ed_username.setError("Invalid username!");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        createtv_email_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_ed_input_cred.setHint("Enter your email address");
                create_ed_input_cred.setInputType(InputType.TYPE_CLASS_TEXT);
                tag[0] = "Email";
                if(!isValidEmail(create_ed_input_cred.toString().trim())){
                    create_ed_input_cred.setError("Invalid Email");
                }
            }
        });

        createtv_phone_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_ed_input_cred.setHint("Enter your phone number");
                create_ed_input_cred.setInputType(InputType.TYPE_CLASS_NUMBER);
                tag[0] = "Phone";
                if(!isValidPhone(create_ed_input_cred.toString().trim())){
                    create_ed_input_cred.setError("Invalid Phone");
                }
            }
        });

        create_ed_input_cred.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(create_ed_input_cred.length() > 0){
                    if(tag[0].equals("Email")){
                        if(!isValidEmail(create_ed_input_cred.getText().toString().trim())){
                            create_ed_input_cred.setError("Invalid Email form!");
                        }
                    }

                    if(tag[0].equals("Phone")){
                        if(!isValidPhone(create_ed_input_cred.getText().toString().trim())){
                            create_ed_input_cred.setError("Invalid Phone Number!");
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        create_ed_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(create_ed_password.length() > 0){
                    if(!isValidPassword(create_ed_password.getText().toString().trim())){
                        create_ed_password.setError("Invalid Password");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        create_ed_confirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(create_ed_confirm.length() > 0){
                    if(!create_ed_confirm.getText().toString().trim().equals(create_ed_password.getText().toString().trim())){
                        create_ed_confirm.setError("Password Does not match");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btsignunp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder termsOfService = new AlertDialog.Builder(CreateAccount.this);  // or MainActivity.this

                //Requires setting of icon
                // termsOfService.setIcon();

                termsOfService.setTitle("Consent to terms of services");

                termsOfService.setMessage("agree to to terms of service");

                termsOfService.setPositiveButton("I agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String type = tag[0];
                        String input = create_ed_input_cred.getText().toString().trim();
                        String username = create_ed_username.getText().toString().trim();
                        String password = create_ed_password.getText().toString().trim();

                        if(!isValidUsername(username)){
                            Toast.makeText(CreateAccount.this, "Invalid username", Toast.LENGTH_SHORT).show();
                        }
                        else if(!isValidPassword(password)){
                            Toast.makeText(CreateAccount.this, "Invalid password", Toast.LENGTH_SHORT).show();
                        }
                        else if(!isValidEmail(input) && tag[0].equals("Email")){
                            Toast.makeText(CreateAccount.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                        }
                        else if(!isValidPhone(input) && tag[0].equals("Phone")){
                            Toast.makeText(CreateAccount.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                        }

                        else{
                            signUpUser(type, input, password, username);
                        }
                    }
                });

                termsOfService.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //restoring to the default state.

                        create_ed_input_cred.setInputType(InputType.TYPE_CLASS_TEXT);
                        tag[0] = "Email";
                        create_ed_username.setText("");
                        create_ed_input_cred.setText("");
                        create_ed_password.setText("");
                        create_ed_confirm.setText("");
                        dialog.dismiss();
                    }
                });

                Dialog termsofservice_form = termsOfService.create();
                termsofservice_form.show();
            }
        });




        createtv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });




    }

    //Helper to check the validity of the username for example{my_username, John_123}
    public static boolean isValidUsername(String username) {
        if (username == null) return false;

        // Username must be 4-20 characters, start with a letter, and contain only letters, numbers, or underscores
        String regex = "^[A-Za-z][A-Za-z0-9_]{3,19}$";
        return username.matches(regex);
    }

    // Helper method to validate email
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Helper method to validate phone number
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{8}"); // Example: 10-digit number validation
    }

    // Helper method to validate password
    private boolean isValidPassword(String password) {
        return password.length() >= 8; // Example: Minimum length requirement
    }

    private void signUpUser(String type, String input, String password, String username) {
        String url = "http://10.0.2.2/testfyp/signup.php";

        // Create the StringRequest for POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        //Log.d("SERVER_RESPONSE", response); used for debugging
                        if (response.contains("Success")) {
                            Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                            //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_SHORT).show();
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
                params.put("username", username);
                params.put("password", password);
                params.put("contact_type", type);
                params.put("contact_value", input);
                return params;
            }
        };

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest);
    }

}