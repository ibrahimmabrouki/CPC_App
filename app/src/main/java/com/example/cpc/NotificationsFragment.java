package com.example.cpc;

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
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.io.*;
import java.net.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;


public class NotificationsFragment extends Fragment implements RefreshableFragment{

    String currentUserId = "1"; // You can make this dynamic later
    String currentUsername = null;
    private final String BASE_URL = "http://10.21.166.221/clinic";
    private ArrayList<String> staffList = new ArrayList<>();
    private ArrayAdapter<String> staffAdapter;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayAdapter<String> chatAdapter;
    private final ArrayList<String> staffNames = new ArrayList<>();
    private final ArrayList<String> staffIds = new ArrayList<>();

    private LinearLayout chatListLayout, chatViewLayout;
    private ListView lvStaff, lvChat;
    private EditText etMessage;
    private Button btnSend;
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

        chatListLayout = rootView.findViewById(R.id.chat_list_layout);
        chatViewLayout = rootView.findViewById(R.id.chat_view_layout);
        lvStaff = rootView.findViewById(R.id.lv_staff);
        lvChat = rootView.findViewById(R.id.lv_chat);
        etMessage = rootView.findViewById(R.id.et_message);
        btnSend = rootView.findViewById(R.id.btn_send);

        staffAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, staffList);
        lvStaff.setAdapter(staffAdapter);

        chatAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, messages);
        lvChat.setAdapter(chatAdapter);

        fetchStaffUsers();
        getCurrentUsernameFromId(currentUserId, username -> {
            currentUsername = username;

            new Thread(() -> {
                try {
                    socket = new Socket("10.21.166.221", 8888);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(socket.getOutputStream(), true);

                    writer.println(currentUsername);  // ✅ send username now

                    listenForMessages();

                } catch (IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(requireContext(), "Socket connection failed", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });



        lvStaff.setOnItemClickListener((parent, view, position, id) -> {
            chatWith = staffIds.get(position);
            Log.d("Chat", "Selected staff ID: " + chatWith);
            chatListLayout.setVisibility(View.GONE);
            chatViewLayout.setVisibility(View.VISIBLE);
            messages.clear();
            chatAdapter.notifyDataSetChanged();
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

                        new Handler(Looper.getMainLooper()).post(() -> {
                            messages.add("Me: " + message);
                            chatAdapter.notifyDataSetChanged();
                            etMessage.setText("");
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
                    staffAdapter.clear();
                    staffAdapter.addAll(staffNames);
                    staffAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(requireContext(), "Failed to load staff", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void listenForMessages() {
        listenThread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        messages.add(finalLine);
                        chatAdapter.notifyDataSetChanged();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
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


    interface UsernameCallback {
        void onUsernameReceived(String username);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
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

        requireActivity().getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (chatViewLayout.getVisibility() == View.VISIBLE) {
                    chatViewLayout.setVisibility(View.GONE);
                    chatListLayout.setVisibility(View.VISIBLE);
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        fetchStaffUsers();
    }

}
