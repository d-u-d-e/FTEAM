package com.es.findsoccerplayers.models;

public class Match {

    private String placeName;
    private String matchData;
    private String matchHour;
    private int playerNumber;
    private double latitude;
    private double longitude;
    private String description;
    private String matchID;

    public Match(){
    }

    public Match(String placeName, String matchData, String matchHour, int playerNumber, double latitude, double longitude, String description) {
        this.placeName = placeName;
        this.matchData = matchData;
        this.matchHour = matchHour;
        this.playerNumber = playerNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getMatchData() {
        return matchData;
    }

    public void setMatchData(String matchData) {
        this.matchData = matchData;
    }

    public String getMatchHour() {
        return matchHour;
    }

    public void setMatchHour(String matchHour) {
        this.matchHour = matchHour;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayersNumber(int playerNumber) {
        this.playerNumber = playerNumber;
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

    public void setID(String s){
        matchID = s;
    }

    public String getID(){
        return matchID;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
