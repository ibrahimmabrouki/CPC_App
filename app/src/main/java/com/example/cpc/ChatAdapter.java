package com.example.cpc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ChatAdapter that supports:
 * - Sent Messages
 * - Received Messages
 * - Date Headers (Today, Yesterday, etc)
 */
public class ChatAdapter extends ArrayAdapter<Object> {

    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;
    private static final int TYPE_DATE = 2;

    private final Context context;
    private final List<Object> items;
    private final String currentUsername;

    public ChatAdapter(Context context, List<Object> items, String currentUsername) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.currentUsername = currentUsername;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof ChatMessage) {
            ChatMessage message = (ChatMessage) item;
            if (message.isMe()) {
                return TYPE_SENT;
            } else {
                return TYPE_RECEIVED;
            }
        } else {
            return TYPE_DATE;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3; // sent, received, date
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object item = items.get(position);
        int viewType = getItemViewType(position);
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_DATE) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_date_header, parent, false);
            }
            TextView dateText = convertView.findViewById(R.id.date_header_text);
            String dateString = (String) item;
            dateText.setText(dateString);

        } else if (viewType == TYPE_SENT) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_message_sent, parent, false);
            }
            ChatMessage message = (ChatMessage) item;
            TextView messageText = convertView.findViewById(R.id.message_text);
            TextView timeText = convertView.findViewById(R.id.message_time);

            messageText.setText(message.getContent());
            timeText.setText(message.getFormattedTime());

        } else if (viewType == TYPE_RECEIVED) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_message_received, parent, false);
            }
            ChatMessage message = (ChatMessage) item;
            TextView messageText = convertView.findViewById(R.id.message_text);
            TextView timeText = convertView.findViewById(R.id.message_time);

            messageText.setText(message.getContent());
            timeText.setText(message.getFormattedTime());
        }

        return convertView;
    }

}
