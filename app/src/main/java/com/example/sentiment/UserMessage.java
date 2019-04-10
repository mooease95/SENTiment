package com.example.sentiment;

import com.google.firebase.firestore.FieldValue;

class UserMessage {

    String sender;
    String recipient;
    String message;
    String timestamp;
    String emotion;
    FieldValue serverTimestamp;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public FieldValue getServerTimestamp() {
        return serverTimestamp;
    }

    public void setServerTimestamp(FieldValue serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
}
