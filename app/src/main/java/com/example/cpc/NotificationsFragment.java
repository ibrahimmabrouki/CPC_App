package com.example.cpc;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;


public class NotificationsFragment extends Fragment {

    String currentUserId = "8"; // You can make this dynamic later
    private final String BASE_URL = "http://10.21.166.221/clinic";
    private ArrayList<String> staffList = new ArrayList<>();
    private ArrayAdapter<String> staffAdapter;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayAdapter<String> chatAdapter;

    private LinearLayout chatListLayout, chatViewLayout;
    private ListView lvStaff, lvChat;
    private EditText etMessage;
    private Button btnSend;
    private String chatWith;

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

        lvStaff.setOnItemClickListener((parent, view, position, id) -> {
            chatWith = staffList.get(position);
            chatListLayout.setVisibility(View.GONE);
            chatViewLayout.setVisibility(View.VISIBLE);
            messages.clear();
            chatAdapter.notifyDataSetChanged();
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                messages.add("Me: " + message);
                chatAdapter.notifyDataSetChanged();
                etMessage.setText("");
                // TODO: send message via socket later
            }
        });

        return rootView;
    }

    private void fetchStaffUsers() {
        String url = BASE_URL + "/getStaffUsers.php?current_id=" + currentUserId;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    staffList.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String username = obj.getString("username");
                            staffList.add(username);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    staffAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(requireContext(), "Failed to load staff", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }
}
