package com.new9.recogbusinesscard.Class;

import java.io.Serializable;

/**
 * Created by SEGU on 2017-05-25.
 */

public class Card implements Serializable{
    String name;
    String engname;
    String phone;
    String company;
    String position;
    String email;
    String address;

    public void setName(String name) {
        this.name = name;
    }

    public void setEngname(String engname) {
        this.engname = engname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEngname() {
        return engname;
    }

    public String getPhone() {
        return phone;
    }

    public String getCompany() {
        return company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPosition() {
        return position;
    }

    public String getEmail() {
        return email;
    }

    public Card(){};


    public Card(String name, String engname, String address, String phone, String company, String position, String email) {
        this.name = name;
        this.address = address;
        this.engname = engname;
        this.phone = phone;
        this.company = company;
        this.position = position;
        this.email = email;
    }
}
