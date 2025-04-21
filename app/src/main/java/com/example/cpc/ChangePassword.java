package com.example.cpc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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
        createNotificationChannel();

        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        contactValue = getIntent().getStringExtra("contact_value");
        //contactValue = "ibrahim.mabrouki@lau.edu"; //for testing
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_password = newPasswordInput.getText().toString().trim();
                String new_confirm_password = confirmPasswordInput.getText().toString().trim();

                if(!isValidPassword(new_password)){
                    showCustomToast("Invalid new password", R.drawable.ic_uncheck);
                }
                else if(!isValidPassword(new_confirm_password)) {
                    showCustomToast("Invalid new confirm password", R.drawable.ic_uncheck);
                }
                else if(!new_password.equals(new_confirm_password)){
                    showCustomToast("Confirm password Does not match", R.drawable.ic_uncheck);
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
        String url = "http://10.21.148.28/testfyp/ResetPassword/update_password.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equalsIgnoreCase("success")) {
                            //Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                                    ContextCompat.checkSelfPermission(ChangePassword.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {

                                showOtpNotification("Password Updated", "Your password has been changed successfully.");
                            }

                            setContentView(R.layout.activity_success_screen);
                            ImageView successImage = findViewById(R.id.success_image);
                            successImage.setImageResource(R.drawable.ic_changed_password);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();
                                }
                            }, 2000);

                        }
                        else {
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

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Change Password Channel";
            String description = "Channel for Change Password notifications";
            int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
            android.app.NotificationChannel channel = new android.app.NotificationChannel("Change_password_channel", name, importance);
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


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Change_password_channel");
        builder.setSmallIcon(icon);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1002, builder.build());
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