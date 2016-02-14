package com.example.antoine.testapp.classes;

/**
 * Created by guillaumebrosse on 14/02/16.
 */
public class Player {
    private String idClub;
    private String name;
    private String position;
    private String number;
    private String birth;

    public Player(String idClub, String name, String position, String number, String birth) {
        this.idClub = idClub;
        this.name = name;
        this.position = position;
        this.number = number;
        this.birth = birth;
    }

    public String getIdClub() {
        return idClub;
    }

    public void setIdClub(String idClub) {
        this.idClub = idClub;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }
}
