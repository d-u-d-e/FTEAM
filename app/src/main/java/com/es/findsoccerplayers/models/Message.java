package com.es.findsoccerplayers.models;

public class Message {
    private String senderID;

    private String senderNick;
    private String text;
    private long timestamp;

    public Message(){

    }

    public Message(String sender, String senderNick, String text, long timestamp) {
        this.senderID = sender;
        this.senderNick = senderNick;
        this.text = text;
        this.timestamp = timestamp;
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

    public String getSenderNick() {
        return senderNick;
    }

    public void setSenderNick(String senderNick) {
        this.senderNick = senderNick;
    }

}
