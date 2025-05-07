package com.example.cpc;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class NotificationsFragment extends Fragment implements RefreshableFragment {

    private final String BASE_URL = "http://10.21.139.29/clinic";
    private String currentUserId;
    private String currentUsername;
    private String chatWith;

    private ArrayList<StaffItem> staffList = new ArrayList<>();
    private StaffAdapter staffAdapter;
    private List<Object> chatItems = new ArrayList<>();

    private LinearLayout chatListLayout, chatViewLayout;
    private ListView lvStaff, lvChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvChatUsername;

    private ChatAdapter chatAdapter;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread listenThread;

    private String lastMessageDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);

        currentUserId = getArguments().getString("user_id");

        chatListLayout = rootView.findViewById(R.id.chat_list_layout);
        chatViewLayout = rootView.findViewById(R.id.chat_view_layout);
        lvStaff = rootView.findViewById(R.id.lv_staff);
        lvChat = rootView.findViewById(R.id.lv_chat);
        etMessage = rootView.findViewById(R.id.et_message);
        btnSend = rootView.findViewById(R.id.btn_send);
        tvChatUsername = rootView.findViewById(R.id.tv_chat_username);

        staffAdapter = new StaffAdapter(requireContext(), staffList);
        lvStaff.setAdapter(staffAdapter);

        chatAdapter = new ChatAdapter(requireContext(), chatItems, currentUsername);
        lvChat.setAdapter(chatAdapter);

        fetchStaffUsers();
        setupSocketConnection();

        lvStaff.setOnItemClickListener((parent, view, position, id) -> {
            chatWith = staffList.get(position).getId();
            tvChatUsername.setText(staffList.get(position).getName());
            staffList.get(position).setHasUnread(false);
            staffAdapter.notifyDataSetChanged();

            chatListLayout.setVisibility(View.GONE);
            chatViewLayout.setVisibility(View.VISIBLE);

            chatItems.clear();
            chatAdapter.notifyDataSetChanged();
            lastMessageDate = "";

            loadStoredMessages(currentUserId, chatWith);
            markMessagesAsRead(chatWith, currentUserId);
        });

        btnSend.setOnClickListener(v -> sendMessage());

        setupBackButtonHandling();

        return rootView;
    }

    private void setupSocketConnection() {
        getCurrentUsernameFromId(currentUserId, username -> {
            currentUsername = username;
            new Thread(() -> {
                try {
                    socket = new Socket("10.21.139.29", 8888);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println(currentUsername);

                    listenForMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Socket connection failed");
                }
            }).start();
        });
    }

    private void listenForMessages() {
        listenThread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    handleIncomingMessage(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    private void handleIncomingMessage(String messageLine) {
        new Handler(Looper.getMainLooper()).post(() -> {
            String[] parts = messageLine.split(": ", 2);
            if (parts.length == 2) {
                String senderName = parts[0].trim();
                String content = parts[1].trim();

                boolean isMe = senderName.equals(currentUsername);

                ChatMessage message = new ChatMessage(
                        senderName,
                        content,
                        isMe,
                        new Date()
                );

                addMessageWithDate(message);

                boolean chattingWithSender = false;
                if (chatWith != null) {
                    for (StaffItem staff : staffList) {
                        if (staff.getName().trim().equalsIgnoreCase(senderName.trim()) && staff.getId().equals(chatWith)) {
                            chattingWithSender = true;
                            break;
                        }
                    }
                }

                if (chattingWithSender) {
                    markMessagesAsRead(chatWith, currentUserId);
                } else {
                    for (StaffItem staff : staffList) {
                        if (staff.getName().trim().equalsIgnoreCase(senderName.trim())) {
                            staff.setHasUnread(true);
                            staffAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    showIncomingMessageNotification(senderName, content);
                }
            }
        });
    }

    private void showIncomingMessageNotification(String senderName, String messageContent) {
        String CHANNEL_ID = "chat_messages";
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(senderName)
                .setContentText(messageContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(new Random().nextInt(), builder.build());
        } else {
            Log.w("NotificationsFragment", "Notification permission not granted");
        }
    }



    private StaffItem findStaffByName(String name) {
        for (StaffItem staff : staffList) {
            if (staff.getName().trim().equalsIgnoreCase(name.trim())) {
                return staff;
            }
        }
        return null;
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();

        if (chatWith == null || writer == null || message.isEmpty()) {
            showToast("Please select a chat first.");
            return;
        }

        getUsernameById(chatWith, recipientUsername -> {
            new Thread(() -> {
                writer.println(recipientUsername + "|" + message);
                saveMessageToDatabase(currentUserId, chatWith, message);

                ChatMessage myMessage = new ChatMessage(
                        "Me",
                        message,
                        true,
                        new Date()
                );
                new Handler(Looper.getMainLooper()).post(() -> {
                    addMessageWithDate(myMessage);
                    etMessage.setText("");
                });
            }).start();
        });
    }

    private void addMessageWithDate(ChatMessage message) {
        String messageDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(message.getTimestamp());

        if (!messageDate.equals(lastMessageDate)) {
            chatItems.add(getFriendlyDate(messageDate));
            lastMessageDate = messageDate;
        }
        chatItems.add(message);
        chatAdapter.notifyDataSetChanged();
        lvChat.post(() -> lvChat.setSelection(chatAdapter.getCount() - 1));
    }

    private String getFriendlyDate(String rawDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(rawDate);

            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(date);

            Calendar todayCal = Calendar.getInstance();

            Calendar yesterdayCal = Calendar.getInstance();
            yesterdayCal.add(Calendar.DATE, -1);

            if (isSameDay(messageCal, todayCal)) {
                return "Today";
            } else if (isSameDay(messageCal, yesterdayCal)) {
                return "Yesterday";
            } else {
                return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return rawDate;
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }


    private void fetchStaffUsers() {
        String url = BASE_URL + "/getStaffUsers.php?exclude_id=" + currentUserId;
        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    staffList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.getString("id");
                            String name = obj.getString("username");
                            String type = obj.getString("type");

                            StaffItem staffItem = new StaffItem(id, name, type);
                            staffList.add(staffItem);

                            fetchUnreadStatusForStaff(id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    staffAdapter.notifyDataSetChanged();
                },
                error -> showToast("Failed to load staff"));

        queue.add(request);
    }
    private void fetchUnreadStatusForStaff(String otherUserId) {
        String url = BASE_URL + "/checkUnread.php?my_id=" + currentUserId + "&other_id=" + otherUserId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        int unread = response.getInt("unread");

                        for (StaffItem staff : staffList) {
                            if (staff.getId().equals(otherUserId)) {
                                staff.setHasUnread(unread == 1);
                                staffAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Log.e("Chat", "Error fetching unread status"));
        queue.add(request);
    }



    private void loadStoredMessages(String myId, String otherId) {
        String url = BASE_URL + "/getMessages.php?user_id=" + myId + "&other_id=" + otherId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    chatItems.clear();
                    lastMessageDate = "";

                    getUsernameById(otherId, otherUsername -> {
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                String senderId = obj.getString("sender_id");
                                String content = obj.getString("message");

                                boolean isMe = senderId.equals(myId);
                                String timestampString = obj.getString("timestamp");

                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                Date timestamp = new Date();
                                try {
                                    timestamp = sdf.parse(timestampString);
                                } catch (Exception e) {
                                    e.printStackTrace(); // fallback in case parsing fails
                                }

                                ChatMessage message = new ChatMessage(
                                        isMe ? "Me" : otherUsername,
                                        content,
                                        isMe,
                                        timestamp
                                );
                                addMessageWithDate(message);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        lvChat.post(() -> lvChat.setSelection(chatAdapter.getCount() - 1));
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

    private void saveMessageToDatabase(String senderId, String recipientId, String message) {
        String url = BASE_URL + "/saveMessage.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d("Chat", "Message saved"),
                error -> Log.e("Chat", "Failed to save message")
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

    private void getUsernameById(String id, UsernameCallback callback) {
        String url = BASE_URL + "/getUsernameById.php?id=" + id;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            callback.onUsernameReceived(response.getJSONObject(0).getString("username"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> showToast("Error fetching username")
        );
        queue.add(request);
    }

    private void getCurrentUsernameFromId(String id, UsernameCallback callback) {
        getUsernameById(id, callback);
    }

    private void setupBackButtonHandling() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (chatViewLayout.getVisibility() == View.VISIBLE) {
                    chatWith = null;
                    chatViewLayout.setVisibility(View.GONE);
                    chatListLayout.setVisibility(View.VISIBLE);
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    private void showToast(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        fetchStaffUsers();
    }

    interface UsernameCallback {
        void onUsernameReceived(String username);
    }
}
