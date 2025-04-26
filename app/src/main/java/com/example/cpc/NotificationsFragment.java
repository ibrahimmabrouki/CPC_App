package com.example.cpc;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private static final String CHANNEL_ID = "chat_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 101;
    private static final int SOCKET_RECONNECT_DELAY = 5000;

    // UI Components
    private LinearLayout chatListLayout, chatViewLayout;
    private RecyclerView rvStaff, rvChat;
    private TextView tvChatTitle;
    private EditText etMessage;
    private ImageButton btnSend, btnBack;

    // Adapters
    private StaffAdapter staffAdapter;
    private ChatAdapter chatAdapter;

    // Data
    private String currentUserId;
    private String currentUsername;
    private String chatWith;
    private final ArrayList<String> staffList = new ArrayList<>();
    private final ArrayList<String> staffIds = new ArrayList<>();
    private boolean isChatActive = false;

    // Network
    private final String BASE_URL = "http://10.21.134.17/clinic";
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread listenThread;
    private boolean shouldReconnect = true;
    private Handler typingHandler = new Handler();
    private boolean isTyping = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
        setupViews(rootView);
        setupAdapters();
        setupClickListeners();
        requestNotificationPermission();
        fetchStaffUsers();
        connectToSocket();
        return rootView;
    }

    private void setupViews(View rootView) {
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
    }

    private void setupAdapters() {
        staffAdapter = new StaffAdapter(staffList, position -> {
            chatWith = staffIds.get(position);
            isChatActive = true;
            tvChatTitle.setText(staffList.get(position));

            chatListLayout.setVisibility(View.GONE);
            chatViewLayout.setVisibility(View.VISIBLE);

            chatAdapter.updateMessages(new ArrayList<>());
            loadStoredMessages(currentUserId, chatWith);
            markMessagesAsRead(chatWith, currentUserId);
        });
        rvStaff.setAdapter(staffAdapter);

        chatAdapter = new ChatAdapter(currentUserId);
        rvChat.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            isChatActive = false;
            chatViewLayout.setVisibility(View.GONE);
            chatListLayout.setVisibility(View.VISIBLE);
        });

        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && chatWith != null) {
                sendTypingIndicator(true);
            }
        });

        etMessage.setOnKeyListener((v, keyCode, event) -> {
            if (chatWith != null) {
                if (!isTyping) {
                    isTyping = true;
                    sendTypingIndicator(true);
                }
                typingHandler.removeCallbacksAndMessages(null);
                typingHandler.postDelayed(() -> {
                    isTyping = false;
                    sendTypingIndicator(false);
                }, 2000);
            }
            return false;
        });
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (chatWith == null || writer == null || message.isEmpty()) return;

        String messageId = UUID.randomUUID().toString();

        getUsernameById(chatWith, recipientUsername -> {
            new Thread(() -> {
                // Send via socket (format: MSG|recipient|messageId|content)
                writer.println("MSG|" + chatWith + "|" + messageId + "|" + message);

                // Save to database
                saveMessageToDatabase(currentUserId, chatWith, message, new MessageCallback() {
                    @Override
                    public void onSuccess(String dbMessageId, Date timestamp) {
                        requireActivity().runOnUiThread(() -> {
                            ChatMessage chatMessage = new ChatMessage(
                                    messageId, currentUserId, "Me", chatWith,
                                    message, timestamp, true, false, true, false);

                            chatAdapter.addMessage(chatMessage);
                            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                            etMessage.setText("");
                        });
                    }
                });
            }).start();
        });
    }

    private void sendTypingIndicator(boolean isTyping) {
        if (writer != null && chatWith != null) {
            new Thread(() -> {
                writer.println("TYPING|" + chatWith + "|" + isTyping);
            }).start();
        }
    }

    private void connectToSocket() {
        getCurrentUsernameFromId(currentUserId, username -> {
            currentUsername = username;
            new Thread(() -> {
                while (shouldReconnect && !Thread.interrupted()) {
                    try {
                        Log.d(TAG, "Attempting socket connection...");
                        socket = new Socket("10.21.134.17", 8888);
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer = new PrintWriter(socket.getOutputStream(), true);

                        writer.println(currentUsername);
                        Log.d(TAG, "Socket connected successfully");

                        listenForMessages();
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "Socket connection failed", e);
                        try {
                            Thread.sleep(SOCKET_RECONNECT_DELAY);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }).start();
        });
    }

    private void reconnectSocket() {
        new Thread(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }

                socket = new Socket("10.21.134.17", 8888);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                writer.println(currentUsername);
                listenForMessages();

                Log.d(TAG, "Socket reconnected successfully");
            } catch (IOException e) {
                Log.e(TAG, "Reconnection failed", e);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (shouldReconnect) {
                        reconnectSocket();
                    }
                }, SOCKET_RECONNECT_DELAY);
            }
        }).start();
    }

    private void listenForMessages() {
        listenThread = new Thread(() -> {
            while (shouldReconnect && !Thread.interrupted()) {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String message = line;
                        requireActivity().runOnUiThread(() -> handleSocketMessage(message));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket read error", e);
                    if (shouldReconnect && !Thread.interrupted()) {
                        try {
                            Thread.sleep(SOCKET_RECONNECT_DELAY);
                            reconnectSocket();
                            break;
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        });
        listenThread.start();
    }

    private void handleSocketMessage(String message) {
        try {
            String[] parts = message.split("\\|", 4);
            if (parts.length >= 3) {
                switch (parts[0]) {
                    case "MSG":
                        // Format: MSG|senderId|messageId|content
                        String senderId = parts[1];
                        String messageId = parts[2];
                        String content = parts[3];

                        getUsernameById(senderId, senderName -> {
                            ChatMessage chatMessage = new ChatMessage(
                                    messageId, senderId, senderName, currentUserId,
                                    content, new Date(), true, isChatActive, false, false);

                            chatAdapter.addMessage(chatMessage);
                            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

                            if (isChatActive) {
                                markMessagesAsRead(senderId, messageId);
                            } else {
                                showNotification(senderName, content);
                            }
                        });
                        break;

                    case "READ":
                        // Format: READ|senderId|messageId
                        String readMessageId = parts[2];
                        chatAdapter.updateMessageStatus(readMessageId, true);
                        break;

                    case "TYPING":
                        // Format: TYPING|senderId|isTyping
                        boolean typing = Boolean.parseBoolean(parts[2]);
                        String typingSenderId = parts[1];

                        if (typing) {
                            getUsernameById(typingSenderId, senderName -> {
                                ChatMessage typingMessage = new ChatMessage(
                                        "", typingSenderId, senderName, currentUserId,
                                        "", new Date(), true, true, false, true);

                                chatAdapter.addTypingIndicator(typingMessage);
                            });
                        } else {
                            chatAdapter.removeTypingIndicator(typingSenderId);
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Message handling error", e);
        }
    }

    private void fetchStaffUsers() {
        String url = BASE_URL + "/getStaffUsers.php?exclude_id=" + currentUserId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    staffIds.clear();
                    staffList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.getString("id");
                            String username = obj.getString("username");

                            staffIds.add(id);
                            staffList.add(username);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing staff user", e);
                        }
                    }
                    staffAdapter.notifyDataSetChanged();
                },
                error -> Log.e(TAG, "Failed to load staff", error));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadStoredMessages(String myId, String otherId) {
        String url = BASE_URL + "/getMessages.php?user_id=" + myId + "&other_id=" + otherId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    List<ChatMessage> messages = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String id = obj.getString("id");
                            String senderId = obj.getString("sender_id");
                            String senderName = obj.getString("sender_username");
                            String recipientId = obj.getString("recipient_id");
                            String message = obj.getString("message");

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date timestamp = sdf.parse(obj.getString("timestamp"));

                            boolean delivered = obj.getInt("delivered") == 1;
                            boolean read = obj.getInt("read") == 1;
                            boolean isMe = senderId.equals(myId);

                            messages.add(new ChatMessage(id, senderId, senderName, recipientId,
                                    message, timestamp, delivered, read, isMe, false));
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing message", e);
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        chatAdapter.updateMessages(messages);
                        if (!messages.isEmpty()) {
                            rvChat.scrollToPosition(messages.size() - 1);
                        }
                    });
                },
                error -> Log.e(TAG, "Error loading messages", error));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void markMessagesAsRead(String senderId, String messageId) {
        // Update locally first
        chatAdapter.updateMessageStatus(messageId, true);

        // Notify server
        if (writer != null) {
            new Thread(() -> {
                writer.println("READ|" + senderId + "|" + messageId);
            }).start();
        }

        // Update database
        String url = BASE_URL + "/markAsRead.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Log.d(TAG, "Marked as read"),
                error -> Log.e(TAG, "Failed to mark as read", error)) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("sender_id", senderId);
                params.put("recipient_id", currentUserId);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showNotification(String title, String message) {
        createNotificationChannel();

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.putExtra("fragment", "notifications");
        intent.putExtra("chat_with", chatWith);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                chatWith.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(requireContext())
                    .notify(new Random().nextInt(1000), builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Incoming chat messages");
            channel.enableLights(true);
            channel.setLightColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            channel.enableVibration(true);

            NotificationManager manager = requireContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void getCurrentUsernameFromId(String id, UsernameCallback callback) {
        String url = BASE_URL + "/getUsernameById.php?id=" + id;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            callback.onUsernameReceived(response.getJSONObject(0).getString("username"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting username", e);
                    }
                },
                error -> Log.e(TAG, "Error fetching username", error));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void getUsernameById(String id, UsernameCallback callback) {
        String url = BASE_URL + "/getUsernameById.php?id=" + id;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            callback.onUsernameReceived(response.getJSONObject(0).getString("username"));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error getting username", e);
                    }
                },
                error -> Log.e(TAG, "Error fetching username", error));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    @Override
    public void onDestroy() {
        shouldReconnect = false;
        if (listenThread != null && listenThread.isAlive()) {
            listenThread.interrupt();
        }
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Error closing socket", e);
        }
        super.onDestroy();
    }

    // Interfaces
    interface MessageCallback {
        void onSuccess(String messageId, Date timestamp);
    }

    interface UsernameCallback {
        void onUsernameReceived(String username);
    }

    // Adapter classes
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
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_staff, parent, false);
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

    private static class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_MESSAGE = 0;
        private static final int TYPE_DATE = 1;
        private static final int TYPE_TYPING = 2;

        private final List<Object> items = new ArrayList<>();
        private final String currentUserId;
        private final Map<String, Integer> typingIndicators = new HashMap<>();

        ChatAdapter(String currentUserId) {
            this.currentUserId = currentUserId;
        }

        public void updateMessages(List<ChatMessage> messages) {
            items.clear();
            typingIndicators.clear();

            String lastDate = "";
            for (ChatMessage message : messages) {
                String currentDate = message.getDisplayDate();
                if (!currentDate.equals(lastDate)) {
                    items.add(currentDate);
                    lastDate = currentDate;
                }
                items.add(message);
            }
            notifyDataSetChanged();
        }

        public void addMessage(ChatMessage message) {
            String currentDate = message.getDisplayDate();
            if (items.isEmpty() || !items.get(items.size()-1).equals(currentDate)) {
                items.add(currentDate);
                notifyItemInserted(items.size() - 1);
            }
            items.add(message);
            notifyItemInserted(items.size() - 1);
        }

        public void addTypingIndicator(ChatMessage typingMessage) {
            String senderId = typingMessage.getSenderId();
            if (typingIndicators.containsKey(senderId)) {
                int position = typingIndicators.get(senderId);
                items.set(position, typingMessage);
                notifyItemChanged(position);
            } else {
                items.add(typingMessage);
                typingIndicators.put(senderId, items.size() - 1);
                notifyItemInserted(items.size() - 1);
            }
        }

        public void removeTypingIndicator(String senderId) {
            if (typingIndicators.containsKey(senderId)) {
                int position = typingIndicators.get(senderId);
                items.remove(position);
                typingIndicators.remove(senderId);
                notifyItemRemoved(position);

                Map<String, Integer> newMap = new HashMap<>();
                for (Map.Entry<String, Integer> entry : typingIndicators.entrySet()) {
                    String id = entry.getKey();
                    int pos = entry.getValue();
                    if (pos > position) pos--;
                    newMap.put(id, pos);
                }
                typingIndicators.clear();
                typingIndicators.putAll(newMap);
            }
        }

        public void updateMessageStatus(String messageId, boolean isRead) {
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item instanceof ChatMessage) {
                    ChatMessage message = (ChatMessage) item;
                    if (message.getId().equals(messageId)) {
                        message.setRead(isRead);
                        notifyItemChanged(i);
                        break;
                    }
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            Object item = items.get(position);
            if (item instanceof String) {
                return TYPE_DATE;
            } else if (item instanceof ChatMessage) {
                ChatMessage message = (ChatMessage) item;
                return message.isTyping() ? TYPE_TYPING : TYPE_MESSAGE;
            }
            return TYPE_MESSAGE;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            if (viewType == TYPE_DATE) {
                View view = inflater.inflate(R.layout.item_chat_date, parent, false);
                return new DateViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_chat_message, parent, false);
                return new MessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder.getItemViewType() == TYPE_DATE) {
                ((DateViewHolder)holder).bind((String)items.get(position));
            } else {
                MessageViewHolder messageHolder = (MessageViewHolder)holder;
                ChatMessage message = (ChatMessage)items.get(position);

                // Common setup
                messageHolder.timeText.setText(message.getFormattedTime());

                if (message.isTyping()) {
                    // Typing indicator setup
                    messageHolder.messageText.setVisibility(View.GONE);
                    messageHolder.typingIndicator.setVisibility(View.VISIBLE);
                    messageHolder.statusIndicator.setVisibility(View.GONE);
                } else {
                    // Normal message setup
                    messageHolder.messageText.setVisibility(View.VISIBLE);
                    messageHolder.typingIndicator.setVisibility(View.GONE);
                    messageHolder.messageText.setText(message.getMessage());

                    // Read status
                    if (message.isMe()) {
                        messageHolder.statusIndicator.setVisibility(View.VISIBLE);
                        messageHolder.statusIndicator.setText(
                                message.isRead() ? "✓✓" : (message.isDelivered() ? "✓" : "")
                        );
                        messageHolder.statusIndicator.setTextColor(
                                message.isRead() ? Color.BLUE : Color.GRAY
                        );
                    } else {
                        messageHolder.statusIndicator.setVisibility(View.GONE);
                    }
                }

                // Sender name (for received messages)
                if (message.isMe()) {
                    messageHolder.senderName.setVisibility(View.GONE);
                } else {
                    messageHolder.senderName.setVisibility(View.VISIBLE);
                    messageHolder.senderName.setText(message.getSenderName());
                }

                // Bubble styling
                GradientDrawable bubble = (GradientDrawable)messageHolder.bubble.getBackground().mutate();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)messageHolder.bubble.getLayoutParams();

                if (message.isMe()) {
                    params.gravity = Gravity.END;
                    bubble.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
                    messageHolder.messageText.setTextColor(Color.WHITE);
                    messageHolder.timeText.setTextColor(Color.WHITE);
                    messageHolder.senderName.setTextColor(Color.WHITE);
                } else {
                    params.gravity = Gravity.START;
                    bubble.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.chat_bubble_received));
                    messageHolder.messageText.setTextColor(Color.BLACK);
                    messageHolder.timeText.setTextColor(Color.BLACK);
                    messageHolder.senderName.setTextColor(Color.BLACK);
                }
                messageHolder.bubble.setLayoutParams(params);
            }
        }

        static class DateViewHolder extends RecyclerView.ViewHolder {
            TextView dateText;

            DateViewHolder(View view) {
                super(view);
                dateText = view.findViewById(R.id.date_text);
            }

            void bind(String date) {
                dateText.setText(date);
            }
        }

        static class MessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, senderName, statusIndicator;
            LinearLayout bubble;
            ProgressBar typingIndicator;

            MessageViewHolder(View view) {
                super(view);
                messageText = view.findViewById(R.id.message_text);
                timeText = view.findViewById(R.id.time_text);
                senderName = view.findViewById(R.id.sender_name);
                statusIndicator = view.findViewById(R.id.status_indicator);
                bubble = view.findViewById(R.id.message_container);
                typingIndicator = view.findViewById(R.id.typing_indicator);
            }
        }
    }
    private void saveMessageToDatabase(String senderId, String recipientId, String message, MessageCallback callback) {
        String url = BASE_URL + "/saveMessage.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String messageId = jsonObject.getString("id");
                        String timestampStr = jsonObject.getString("timestamp");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date timestamp = sdf.parse(timestampStr);

                        callback.onSuccess(messageId, timestamp);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse saved message response", e);
                    }
                },
                error -> Log.e(TAG, "Failed to save message", error)) {
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

}