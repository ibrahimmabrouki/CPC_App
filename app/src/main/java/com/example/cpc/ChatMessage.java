package com.example.cpc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String senderUsername;
    private String content;
    private boolean isMe;
    private Date timestamp;

    public ChatMessage(String senderUsername, String content, boolean isMe, Date timestamp) {
        this.senderUsername = senderUsername;
        this.content = content;
        this.isMe = isMe;
        this.timestamp = timestamp;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getContent() {
        return content;
    }

    public boolean isMe() {
        return isMe;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(timestamp);
    }

    public String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp);
    }
}
