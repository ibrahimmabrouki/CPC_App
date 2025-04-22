package com.example.cpc;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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

        //section below for testing
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }*/

        createNotificationChannel();

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
                    showCustomToast("Please enter a valid email or phone number!", R.drawable.ic_uncheck);
                }

                else if (isProbablyEmail(contactInfo) && isValidEmail(contactInfo)) {
                    //Toast.makeText(getApplicationContext(), "this is a valid email", Toast.LENGTH_SHORT).show();
                    checkIfContactExists(contactInfo);
                }
                else if (isProbablyPhone(contactInfo) && isValidPhone(contactInfo)) {
                    //Toast.makeText(getApplicationContext(), "this is a valid phone number", Toast.LENGTH_SHORT).show();
                    checkIfContactExists(contactInfo);

                }
                else {
                    // Invalid input
                    showCustomToast("Please enter a valid email or phone number", R.drawable.ic_uncheck);
                }
            }
        });

        //initially make the button disabled
        verifyBtn.setEnabled(false);
        verifyBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), android.R.color.darker_gray));

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
                    verifyBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.deep_blue));
                }
                else if(l > 6 || l <6){
                    verifyBtn.setEnabled(false);
                    verifyBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
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
                verifyBtn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

                otpInput.setText("");
                String contactInfo = contactInput.getText().toString().trim();
                if(TextUtils.isEmpty(contactInfo)){
                    showCustomToast("Please enter a valid email or phone number!", R.drawable.ic_uncheck);
                }

                else if (isProbablyEmail(contactInfo) && isValidEmail(contactInfo)) {
                    //Toast.makeText(getApplicationContext(), "this is a valid email", Toast.LENGTH_SHORT).show();
                    checkIfContactExists(contactInfo);
                }
                else if (isProbablyPhone(contactInfo) && isValidPhone(contactInfo)) {
                    //Toast.makeText(getApplicationContext(), "this is a valid phone", Toast.LENGTH_SHORT).show();
                    checkIfContactExists(contactInfo);

                }
                else {
                    // Invalid input
                    showCustomToast("Please enter a valid email or phone number", R.drawable.ic_uncheck);
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
                            //Toast.makeText(OPT_page.this, "User does not exist", Toast.LENGTH_SHORT).show();
                            contactExists[0] = false;
                            contactInput.setError("user not exists!");
                            // Maybe show an error or block next step
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        String url = "http://10.0.2.2/testfyp/send_otp.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //making send code disabled
                        sendcontactinfo.setEnabled(false);
                        sendcontactinfo.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        startResendCountdown();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                ContextCompat.checkSelfPermission(OPT_page.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

                            showOtpNotification("OTP Sent", "Check your Email for the 6-digit code.");
                        }

                        //Toast.makeText(getApplicationContext(), "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        String url = "http://10.0.2.2/testfyp/ResetPassword/verify_otp.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("true")) {

                            setContentView(R.layout.activity_success_screen);
                            ImageView successImage = findViewById(R.id.success_image);
                            successImage.setImageResource(R.drawable.ic_opt_check);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // now we are allowing the user to reset the password
                                    Intent change_password = new Intent(getApplicationContext(), ChangePassword.class);
                                    change_password.putExtra("contact_value", contactValue);
                                    startActivity(change_password);                                }
                            }, 2000);

                        }

                        else {
                            showCustomToast("Invalid or expired OTP.", R.drawable.ic_uncheck);
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

    //This method includes a count down that allows the user to resend the code only
    //after passing 5 mins
    private void startResendCountdown() {
        resendText.setEnabled(false);
        resendText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        new CountDownTimer(300000, 1000) { // 5 minutes = 300000ms

            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                resendText.setText(String.format("Resend in %02d:%02d", minutes, remainingSeconds));
            }

            public void onFinish() {
                resendText.setEnabled(true);
                resendText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                resendText.setText("Resend Code");
                resendText.setAlpha(1f);

                sendcontactinfo.setEnabled(true);
                sendcontactinfo.setText("Send Code");
                sendcontactinfo.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                sendcontactinfo.setTypeface(null, Typeface.BOLD);
                sendcontactinfo.setAlpha(1f);

                otpInput.setText("");
                verifyBtn.setEnabled(false);
            }

        }.start();
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "OTP Channel";
            String description = "Channel for OTP notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel("otp_channel", name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showOtpNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                //here we are giving the user another chance to accept the notification
                //line below used for testing
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }

        int icon = R.drawable.key_ic_otp;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "otp_channel");
        builder.setSmallIcon(icon);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1001, builder.build());
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


    //used for testing
    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showOtpNotification("OTP Sent", "Check your Email for the 6-digit code.");
            }
            else {
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS);
                if (!showRationale) {
                    // User checked "Don't ask again"
                    AlertDialog.Builder settings = new AlertDialog.Builder(this);
                    settings.setTitle("Permission Required");
                    settings.setMessage("To receive notifications, please enable notification permission from app settings.");
                    settings.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                    settings.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    settings.show();
                }
                else {
                    Toast.makeText(this, "Notification permission is required to show alerts.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }*/

}