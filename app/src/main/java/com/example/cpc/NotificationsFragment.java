package com.example.cpc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.Random;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.content.res.ColorStateList;
import android.widget.ImageButton;
import org.json.JSONArray;
import org.json.JSONObject;


public class NotificationsFragment extends Fragment implements RefreshableFragment{

    String currentUserId;
    String currentUsername = null;
    private final String BASE_URL = "http://10.21.134.17/clinic";
    private ArrayList<String> staffList = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private final ArrayList<String> staffNames = new ArrayList<>();
    private final ArrayList<String> staffIds = new ArrayList<>();

    private LinearLayout chatListLayout, chatViewLayout;
    private RecyclerView rvStaff, rvChat;
    private ChatAdapter chatAdapter;
    private StaffAdapter staffAdapter;
    private TextView tvChatTitle;
    private ImageButton btnBack;
    private EditText etMessage;
    private ImageButton btnSend;
    private String chatWith;

    Socket socket;
    BufferedReader reader;
    PrintWriter writer;
    Thread listenThread;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        currentUserId = getArguments().getString("user_id");
        chatListLayout = rootView.findViewById(R.id.chat_list_layout);
        chatViewLayout = rootView.findViewById(R.id.chat_view_layout);
        rvStaff = rootView.findViewById(R.id.rv_staff);
        rvChat = rootView.findViewById(R.id.rv_chat);
        tvChatTitle = rootView.findViewById(R.id.tv_chat_title);
        btnBack = rootView.findViewById(R.id.btn_back);
        etMessage = rootView.findViewById(R.id.et_message);
        btnSend = rootView.findViewById(R.id.btn_send);

        rvStaff.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvChat.setLayoutManager(new LinearLayoutManager(requireContext()));

        staffAdapter = new StaffAdapter(staffList, position -> {
            chatWith = staffIds.get(position);
            tvChatTitle.setText(staffList.get(position));

            chatListLayout.setVisibility(View.GONE);
            chatViewLayout.setVisibility(View.VISIBLE);
            messages.clear();
            chatAdapter.notifyItemInserted(messages.size() - 1);
            rvChat.scrollToPosition(messages.size() - 1);
            loadStoredMessages(currentUserId, chatWith);
            markMessagesAsRead(chatWith, currentUserId);
        });
        rvStaff.setAdapter(staffAdapter);

        chatAdapter = new ChatAdapter(messages, currentUserId);
        rvChat.setAdapter(chatAdapter);

// Back button handling
        btnBack.setOnClickListener(v -> {
            chatViewLayout.setVisibility(View.GONE);
            chatListLayout.setVisibility(View.VISIBLE);
            chatListLayout.setAlpha(0f);
            chatListLayout.animate().alpha(1f).setDuration(300).start();
        });

        fetchStaffUsers();
        getCurrentUsernameFromId(currentUserId, username -> {
            currentUsername = username;

            new Thread(() -> {
                try {
                    socket = new Socket("10.21.134.17", 8888);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println(currentUsername);

                    listenForMessages();

                } catch (IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(requireContext(), "Socket connection failed", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });


        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (chatWith == null) {
                Toast.makeText(requireContext(), "Please select a staff member to chat with.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (writer == null) {
                Toast.makeText(requireContext(), "Socket not connected yet. Try again shortly.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!message.isEmpty()) {
                getUsernameById(chatWith, recipientUsername -> {
                    Log.d("Chat", "Sending to " + recipientUsername + " → " + message);

                    new Thread(() -> {
                        writer.println(recipientUsername + "|" + message);

                        saveMessageToDatabase(currentUserId, chatWith, message);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            messages.add("Me: " + message);
                            chatAdapter.notifyItemInserted(messages.size() - 1);
                            rvChat.scrollToPosition(messages.size() - 1);                            etMessage.setText("");
                        });
                    }).start();
                });
            }
        });


        return rootView;
    }

    private void fetchStaffUsers() {
        String url = BASE_URL + "/getStaffUsers.php?exclude_id=" + currentUserId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    staffNames.clear();
                    staffIds.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.getString("id");
                            String username = obj.getString("username");

                            staffNames.add(username);
                            staffIds.add(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    staffList.clear();
                    staffList.addAll(staffNames);
                    staffAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(requireContext(), "Failed to load staff", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void listenForMessages() {
        listenThread = new Thread(() -> {
            try {
                String line;
                while (!Thread.interrupted() && (line = reader.readLine()) != null) {
                    String finalLine = line;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        messages.add(finalLine);
                        chatAdapter.notifyItemInserted(messages.size() - 1);
                        rvChat.scrollToPosition(messages.size() - 1);

                        // Extract sender name for notifications
                        String[] parts = finalLine.split(": ", 2);
                        if (parts.length == 2 && !parts[0].equals("Me")) {
                            showNotification("New message from " + parts[0], parts[1]);
                        }
                    });
                }
            } catch (IOException e) {
                if (!Thread.interrupted()) {
                    Log.e("Socket", "Error in listen thread", e);
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Connection error", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
        listenThread.start();
    }


    private void getUsernameById(String id, UsernameCallback callback) {
        String url = BASE_URL + "/getUsernameById.php?id=" + id;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            String username = response.getJSONObject(0).getString("username");
                            callback.onUsernameReceived(username);
                        } else {
                            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error fetching username", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
    private void getCurrentUsernameFromId(String id, UsernameCallback callback) {
        String url = BASE_URL + "/getUsernameById.php?id=" + id;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            String username = response.getJSONObject(0).getString("username");
                            callback.onUsernameReceived(username);
                        } else {
                            Toast.makeText(requireContext(), "Current user not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error fetching current username", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }
    private void saveMessageToDatabase(String senderId, String recipientId, String message) {
        String url = BASE_URL + "/saveMessage.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("Chat", "Message saved to DB"),
                error -> Log.e("Chat", "Failed to save message: " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("sender_id", senderId);
                params.put("recipient_id", recipientId);
                params.put("message", message);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }


    private void loadStoredMessages(String myId, String otherId) {
        String url = BASE_URL + "/getMessages.php?user_id=" + myId + "&other_id=" + otherId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    messages.clear();

                    getUsernameById(otherId, otherUsername -> {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                String senderId = obj.getString("sender_id");
                                String content = obj.getString("message");

                                if (senderId.equals(myId)) {
                                    messages.add("Me: " + content);
                                } else {
                                    messages.add(otherUsername + ": " + content);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                    });
                },
                error -> Log.e("Chat", "Error loading chat history")
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
    private void markMessagesAsRead(String senderId, String recipientId) {
        String url = BASE_URL + "/markAsRead.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("Chat", "Marked as read"),
                error -> Log.e("Chat", "Failed to mark as read")
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("sender_id", senderId);
                params.put("recipient_id", recipientId);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showNotification(String title, String message) {
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "chat_channel",
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Incoming chat messages");
            channel.enableLights(true);
            channel.setLightColor(ContextCompat.getColor(requireContext(), R.color.chat_message_sent));            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Create intent to open chat when notification is tapped
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("fragment", "notifications");
        intent.putExtra("chat_with", chatWith);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "chat_channel")
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(requireContext(), R.color.chat_message_sent))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        // Show notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(requireContext());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(new Random().nextInt(1000), builder.build());
        }
    }

    interface UsernameCallback {
        void onUsernameReceived(String username);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenThread != null && listenThread.isAlive()) {
            listenThread.interrupt();
        }
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (chatViewLayout.getVisibility() == View.VISIBLE) {
                    chatViewLayout.setVisibility(View.GONE);
                    chatListLayout.setVisibility(View.VISIBLE);
                } else {
                    remove();
                    requireActivity().onBackPressed();
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        fetchStaffUsers();
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    private static class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.ViewHolder> {
        private final List<String> staffList;
        private final OnStaffClickListener listener;

        interface OnStaffClickListener {
            void onStaffClick(int position);
        }

        StaffAdapter(List<String> staffList, OnStaffClickListener listener) {
            this.staffList = staffList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.tvStaffName.setText(staffList.get(position));
            holder.itemView.setOnClickListener(v -> listener.onStaffClick(position));
        }

        @Override
        public int getItemCount() {
            return staffList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStaffName;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStaffName = itemView.findViewById(R.id.tv_staff_name);
            }
        }
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private final List<String> messages;
        private final String currentUserId;

        ChatAdapter(List<String> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String message = messages.get(position);
            String[] parts = message.split(": ", 2);
            if (parts.length == 2) {
                holder.tvSender.setText(parts[0]);
                holder.tvMessage.setText(parts[1]);

                GradientDrawable bubbleDrawable = (GradientDrawable) holder.bubble.getBackground().mutate();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.bubble.getLayoutParams();

                if (parts[0].equals("Me")) {
                    // Sent message (right aligned)
                    params.gravity = Gravity.END;
                    bubbleDrawable.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.chat_message_sent));
                    holder.tvSender.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                    holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
                } else {
                    // Received message (left aligned)
                    params.gravity = Gravity.START;
                    bubbleDrawable.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.chat_message_received));
                    holder.tvSender.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
                    holder.tvMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
                }
                holder.bubble.setLayoutParams(params);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSender, tvMessage, tvTime;
            LinearLayout bubble;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvSender = itemView.findViewById(R.id.tv_sender);
                tvMessage = itemView.findViewById(R.id.tv_message);
                tvTime = itemView.findViewById(R.id.tv_time);
                bubble = itemView.findViewById(R.id.message_bubble);
            }
        }
    }
}
