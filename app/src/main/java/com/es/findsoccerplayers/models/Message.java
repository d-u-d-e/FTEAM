package com.es.findsoccerplayers.models;

public class Message {
    private String sender;
    private String text;
    private int timestamp;

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public Message(){}

    public Message(String sender, String text, int timestamp) {
        this.sender = sender;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
