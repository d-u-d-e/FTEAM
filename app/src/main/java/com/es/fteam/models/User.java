package com.es.fteam.models;

public class User {

    private String id;
    private String username;
    private String dateOfBirth;

    public User(){}

    public User(String id, String username, String dateOfBirth){
        this.id = id;
        this.username = username;
        this.dateOfBirth = dateOfBirth;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUsername(){return username;}

    public void setUsername(String username){this.username = username;}

    public String getDateOfBirth(){
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth){this.dateOfBirth = dateOfBirth;}
}
