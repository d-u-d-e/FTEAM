package com.es.findsoccerplayers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class User {
    private String name;
    private String surname;
    private Date dateOfBirth;
    private Set<String> matches = new HashSet<>();

    public User(){}

    public User(String name, String surname, Date dateOfBirth){
        this.name = name;
        this.surname = surname;
        this.dateOfBirth = dateOfBirth;
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

    public Date getDateOfBirth(){
        return dateOfBirth;
    }

    public ArrayList<String> getMatches(){
        return new ArrayList<>(matches);
    }
}
