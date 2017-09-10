package com.new9.recogbusinesscard.Class;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by SEGU on 2017-05-25.
 */

public class User implements Serializable {
    public User(){};


    private String id;
    private String pw;
    private HashMap<String,Card> userlist;
    private Card card;

    public User(String id, String pw, HashMap<String, Card> userlist, Card card) {
        this.id = id;
        this.pw = pw;
        this.userlist = userlist;
        this.card = card;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public String getId() {
        return id;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public HashMap<String, Card> getUserlist() {
        return userlist;
    }

    public void setUserlist(HashMap<String, Card> userlist) {
        this.userlist = userlist;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
