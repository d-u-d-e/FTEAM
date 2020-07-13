package com.es.findsoccerplayers.models;

public class Match {

    private String placeName;
    private String matchDate;
    private String matchTime;
    private int playersNumber;
    private double latitude;
    private double longitude;
    private String description;
    private String matchID;
    private String creatorID;

    public Match(){
    }

    public Match(String placeName, String matchDate, String matchHour, int playerNumber, double latitude, double longitude, String description, String creatorID) {
        this.placeName = placeName;
        this.matchDate = matchDate;
        this.matchTime = matchHour;
        this.playersNumber = playerNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.creatorID = creatorID;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(String matchDate) {
        this.matchDate = matchDate;
    }

    public String getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(String matchHour) {
        this.matchTime = matchHour;
    }

    public int getPlayersNumber() {
        return playersNumber;
    }

    public void setPlayersNumber(int playerNumber) {
        this.playersNumber = playerNumber;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }
}
