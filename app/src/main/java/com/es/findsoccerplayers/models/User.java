package com.es.findsoccerplayers.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class User {
    private String id;
    private String name;
    private String surname;
    private String dateOfBirth;
    private Set<String> matches = new HashSet<>();

    public User(){}

    public User(String id, String name, String surname, String dateOfBirth){
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public void addMatch(String match){
        matches.add(match);
    }

    public void addMatches(ArrayList<String> matches){
        this.matches.addAll(matches);
    }

    public void deleteMatch(String match){
        matches.remove(match);
    }

    public String getName(){
        return name;
    }

    public String getSurname(){
        return surname;
    }

    public String getDateOfBirth(){
        return dateOfBirth;
    }

    public ArrayList<String> getMatches(){
        return new ArrayList<>(matches);
    }
}
