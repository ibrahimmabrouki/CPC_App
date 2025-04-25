package com.example.cpc;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.cpc.LoginActivity;
import com.example.cpc.R;

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
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = s.toString().trim();

                if (!username.isEmpty()) {
                    if (!isValidUsername(username)) {
                        create_ed_username.setError("Invalid username!");}
                    else {
                        checkUsernameAvailability(username, new UsernameCheckCallback() {
                            @Override
                            public void onResult(boolean isAvailable) {
                                if (!isAvailable) {
                                    create_ed_username.setError("Username already used!");
                                }
                                else {
                                    create_ed_username.setError(null);
                                }
                            }
                        });
                    }
                }
                else {
                    create_ed_username.setError(null);
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
                String input = s.toString().trim();

                if (!input.isEmpty()) {
                    if (tag[0].equals("Email")) {
                        if (!isValidEmail(input)) {
                            create_ed_input_cred.setError("Invalid email format!");
                            return; // Don't check availability
                        }
                    }

                    else if (tag[0].equals("Phone")) {
                        if (!isValidPhone(input)) {
                            create_ed_input_cred.setError("Invalid phone number!");
                            return;
                        }
                    }

                    checkContactAvailability(input, new ContactCheckCallback() {
                        @Override
                        public void onResult(boolean isAvailable) {
                            if (!isAvailable) {
                                create_ed_input_cred.setError("This contact is already used!");
                            } else {
                                create_ed_input_cred.setError(null);
                            }
                        }
                    });
                }
                else {
                    create_ed_input_cred.setError(null);
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
                AlertDialog.Builder termsOfService = new AlertDialog.Builder(CreateAccount.this);
                termsOfService.setTitle("Consent to terms of services");
                termsOfService.setMessage("Agree to terms of service");

                termsOfService.setPositiveButton("I agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String type = tag[0];
                        final String input = create_ed_input_cred.getText().toString().trim();
                        final String username = create_ed_username.getText().toString().trim();
                        final String password = create_ed_password.getText().toString().trim();

                        // Validate inputs
                        if (!isValidUsername(username)) {
                            showCustomToast("Invalid username", R.drawable.ic_uncheck);
                            return;
                        }

                        if (!isValidPassword(password)) {
                            showCustomToast("Invalid password", R.drawable.ic_uncheck);
                            return;
                        }

                        if (type.equals("Email") && !isValidEmail(input)) {
                            showCustomToast("Invalid email address", R.drawable.ic_uncheck);
                            return;
                        }

                        if (type.equals("Phone") && !isValidPhone(input)) {
                            showCustomToast("Invalid phone number", R.drawable.ic_uncheck);
                            return;
                        }

                        checkUsernameAvailability(username, new UsernameCheckCallback() {
                            @Override
                            public void onResult(boolean isUsernameAvailable) {
                                if (!isUsernameAvailable) {
                                    create_ed_username.setError("Choose another username");
                                    return;
                                }

                                checkContactAvailability(input, new ContactCheckCallback() {
                                    @Override
                                    public void onResult(boolean isContactAvailable) {
                                        if (!isContactAvailable) {
                                            create_ed_input_cred.setError("Choose another contact Value");
                                        }
                                        else{
                                            signUpUser(type, input, password, username);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

                termsOfService.setNegativeButton("Cancel", null);
                termsOfService.show();
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
        String url = "http://10.21.134.17/clinic/signup.php";

        // Create the StringRequest for POST request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the response from the server
                        //Log.d("SERVER_RESPONSE", response); used for debugging
                        if (response.contains("Success")) {

                            setContentView(R.layout.activity_success_screen);
                            ImageView successImage = findViewById(R.id.success_image);
                            successImage.setImageResource(R.drawable.patien_toast_bg);

                            showCustomToast("User registered successfully", R.drawable.ic_check);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                }
                            }, 2000);
                        }

                        else {
                            showCustomToast("Registration failed", R.drawable.ic_uncheck);
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
                //params.put("type", "Patient");
                return params;
            }
        };

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void checkUsernameAvailability(String username, UsernameCheckCallback callback) {
        String url = "http://10.21.134.17/clinic/check_username.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (response.equalsIgnoreCase("USED")) {
                            callback.onResult(false); // used
                        }
                        else if (response.equalsIgnoreCase("NOT USED")) {
                            callback.onResult(true);  // available
                        }
                        else {
                            //Toast.makeText(getApplicationContext(), "Unexpected response: " + response, Toast.LENGTH_SHORT).show();
                            callback.onResult(false); // Treat as unavailable
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onResult(false);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void checkContactAvailability(String contactValue, ContactCheckCallback callback) {
        String url = "http://10.21.134.17/clinic/check_contact_value.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        response = response.trim();
                        if (response.equalsIgnoreCase("USED")) {
                            callback.onResult(false); //  used
                        }
                        else if (response.equalsIgnoreCase("NOT USED")) {
                            callback.onResult(true);  // available
                        }
                        else {
                            callback.onResult(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onResult(false); // Treat as used
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("contact_value", contactValue);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    public interface UsernameCheckCallback {
        void onResult(boolean isAvailable);
    }
    public interface ContactCheckCallback {
        void onResult(boolean isAvailable);
    }

    private void showCustomToast(String message, int icon) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView toastText = layout.findViewById(R.id.toast_text);
        toastText.setText(message);

        ImageView custom_icon = layout.findViewById(R.id.custom_icon);
        custom_icon.setImageResource(icon);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, -100);
        toast.show();
    }

}