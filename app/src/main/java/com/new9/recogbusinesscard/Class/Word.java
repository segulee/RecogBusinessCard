package com.new9.recogbusinesscard.Class;

/**
 * Created by SEGU on 2017-05-30.
 */

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

public class Word{
    ArrayList<String> arr;
    public Word(){
        arr = new ArrayList<>();
    }
    public int getSize(){
        return arr.size();
    }
    public String get(int i){
        return arr.get(i);
    }
    public void addString(String temp){
        arr.add(temp);
    }
    public ArrayList<String> getList(){return arr;}
}
