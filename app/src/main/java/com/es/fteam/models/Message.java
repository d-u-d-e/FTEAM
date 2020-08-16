package com.es.fteam.models;

public class Message {

    private String senderID;
    private String messageID;
    private String senderUsername;
    private String text;
    private long timestamp;

    public Message(){

    }

    public Message(String messageID, String sender, String senderUsername, String text, long timestamp) {
        this.senderID = sender;
        this.senderUsername = senderUsername;
        this.text = text;
        this.timestamp = timestamp;
        this.messageID = messageID;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

}
