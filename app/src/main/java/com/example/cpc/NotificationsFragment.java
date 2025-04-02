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

public class NotificationsFragment extends Fragment {

    String currentUser = "abdullah"; // Temporary mock user
    String[] staffUsers = { "ibrahim", "ranim", "jana", "ahmad" };

    private LinearLayout chatListLayout, chatViewLayout;
    private ListView lvStaff, lvChat;
    private EditText etMessage;
    private Button btnSend;

    private ArrayList<String> messages = new ArrayList<>();
    private ArrayAdapter<String> chatAdapter;

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

        ArrayAdapter<String> staffAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, staffUsers);
        lvStaff.setAdapter(staffAdapter);

        chatAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, messages);
        lvChat.setAdapter(chatAdapter);

        lvStaff.setOnItemClickListener((parent, view, position, id) -> {
            chatWith = staffUsers[position];
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
                    // Let system handle it (exit app)
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        });
    }

}
