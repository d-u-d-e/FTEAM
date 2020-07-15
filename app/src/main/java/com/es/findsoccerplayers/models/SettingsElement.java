package com.es.findsoccerplayers.models;

public class SettingsElement {
    private int image;
    private String text1, text2;

    public SettingsElement(int image, String text1, String text2) {
        this.image = image;
        this.text1 = text1;
        this.text2 = text2;
    }

    public int getImage() {
        return image;
    }

    public String getText1() {
        return text1;
    }

    public String getText2() {
        return text2;
    }
}
