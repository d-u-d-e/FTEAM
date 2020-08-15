package com.es.findsoccerplayers.models;

public class SettingsElement {
    private int image;
    private String text, textDetailed;

    public SettingsElement(int image, String text, String textDetailed) {
        this.image = image;
        this.text = text;
        this.textDetailed = textDetailed;
    }

    public int getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public String getTextDetailed() {
        return textDetailed;
    }
}
