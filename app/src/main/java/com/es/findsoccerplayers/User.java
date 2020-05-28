package com.es.findsoccerplayers;

public class User {

    String name, surname, birthday,id;

    public User(String id, String name, String surname, String birthday) {
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getId() {
        return id;
    }

}
