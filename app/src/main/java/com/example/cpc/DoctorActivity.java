package com.example.cpc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.Random;

public class DoctorActivity extends AppCompatActivity {
    private Handler pollingHandler;
    private Runnable pollingRunnable;
    private final String BASE_URL = "http://10.21.134.17/clinic";
    private String currentUserId = "1";

    private boolean isInChatFragment = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        //currentUserId = getIntent().getStringExtra("user_id");

        // Set default fragment
        if (savedInstanceState == null) {
            OverviewFragment overviewFragment = new OverviewFragment();
            Bundle bundle = new Bundle();
            bundle.putString("user_id", currentUserId);
            overviewFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, overviewFragment)
                    .commit();
        }


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                selectedFragment = new OverviewFragment();
            } else if (id == R.id.nav_schedule) {
                selectedFragment = new ScheduleFragment();
            } else if (id == R.id.nav_patients) {
                selectedFragment = new PatientRecordsFragment();
            } else if (id == R.id.nav_notifications) {
                selectedFragment = new NotificationsFragment();
            }

            if (selectedFragment != null) {
                Bundle bundle = new Bundle();
                bundle.putString("user_id", currentUserId);
                selectedFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
        startMessagePolling();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_global_refresh, menu);
        return true;
    }
    public void setInChatFragment(boolean inChatFragment) {
        this.isInChatFragment = inChatFragment;
        adjustPollingFrequency();
    }
    private void adjustPollingFrequency() {
        pollingHandler.removeCallbacks(pollingRunnable);
        if (isInChatFragment) {
            // Less frequent polling when in chat (socket is active)
            pollingHandler.postDelayed(pollingRunnable, 30000);
        } else {
            // More frequent polling when not in chat
            pollingHandler.postDelayed(pollingRunnable, 5000);
        }
    }
    //for better battery

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof RefreshableFragment) {
                ((RefreshableFragment) currentFragment).onRefresh();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startMessagePolling() {
        pollingHandler = new Handler(Looper.getMainLooper());
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                checkForNewMessages(currentUserId);
                pollingHandler.postDelayed(this, 5000); // Poll every 5 sec
            }
        };
        pollingHandler.post(pollingRunnable);
    }

    private void checkForNewMessages(String userId) {
        String url = BASE_URL + "/getUndeliveredMessages.php?recipient_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String senderId = obj.getString("sender_id");
                            String content = obj.getString("message");
                            int read = obj.getInt("read");

                            if (read == 0) {
                                getUsernameById(senderId, username -> {
                                    // Only show notification if not currently in chat with this user
                                    if (!isInChatFragment) {
                                        showNotification(username, content);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                error -> Log.e("Polling", "Error checking new messages")
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void getUsernameById(String id, NotificationsFragment.UsernameCallback callback) {
        String url = BASE_URL + "/getUsernameById.php?id=" + id;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            String username = response.getJSONObject(0).getString("username");
                            callback.onUsernameReceived(username);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("DoctorActivity", "Error fetching username: " + error.toString())
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, DoctorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                return;
            }
        }
        manager.notify(new Random().nextInt(), builder.build());
    }

}
