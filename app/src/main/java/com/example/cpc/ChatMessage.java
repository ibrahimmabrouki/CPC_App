package com.example.cpc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ChatMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String message;
    private Date timestamp;
    private boolean isDelivered;
    private boolean isRead;
    private boolean isMe;
    private boolean isTyping;

    public ChatMessage(String id, String senderId, String senderName, String recipientId,
                       String message, Date timestamp, boolean isDelivered,
                       boolean isRead, boolean isMe, boolean isTyping) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.senderId = senderId;
        this.senderName = senderName;
        this.recipientId = recipientId;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : new Date();
        this.isDelivered = isDelivered;
        this.isRead = isRead;
        this.isMe = isMe;
        this.isTyping = isTyping;
    }

    // Thread-safe date formatting
    private static final ThreadLocal<SimpleDateFormat> timeFormat =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("h:mm a", Locale.getDefault()));
    private static final ThreadLocal<SimpleDateFormat> dateFormat =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()));

    public String getFormattedTime() {
        return timeFormat.get().format(timestamp);
    }

    public String getDisplayDate() {
        String today = dateFormat.get().format(new Date());
        String messageDate = dateFormat.get().format(timestamp);

        if (today.equals(messageDate)) return "Today";

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterday = dateFormat.get().format(cal.getTime());

        if (yesterday.equals(messageDate)) return "Yesterday";

        return new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(timestamp);
    }

    // Getters and setters
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getRecipientId() { return recipientId; }
    public String getMessage() { return message; }
    public Date getTimestamp() { return timestamp; }
    public boolean isMe() { return isMe; }
    public boolean isRead() { return isRead; }
    public boolean isDelivered() { return isDelivered; }
    public boolean isTyping() { return isTyping; }

    public void setRead(boolean read) { isRead = read; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    public void setTyping(boolean typing) { isTyping = typing; }
}