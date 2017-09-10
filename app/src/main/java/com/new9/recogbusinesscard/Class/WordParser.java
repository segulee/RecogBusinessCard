package com.new9.recogbusinesscard.Class;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by SEGU on 2017-05-30.
 */

public class WordParser implements dataset{
    String email = "";
    String name = "";
    String phone = "";
    String address = "";
    String position = "";
    String company = "";

    ArrayList<Word> strList;

    public WordParser(ArrayList<Word> strList){
        this.strList = strList;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPosition() {
        return position;
    }

    public String getCompany(){return company;}

    public String isName(){
        for(int m=0;m<strList.size();m++){
            ArrayList<String> arr = strList.get(m).getList();
            for(int i=0;i<dataName.length;i++){
                if(arr.size() > 0){
                    if(arr.get(0).contains(dataName[i])) {
                        String returnText = "";
                        for (int j = 0; j < arr.size(); j++) {
                            returnText += arr.get(j);
                        }
                        return returnText;
                    }
                }
            }
        }
        return "";
    }

    public String isPosition(){
        for(int m=0;m<strList.size();m++) {
            ArrayList<String> arr = strList.get(m).getList();
            for (int i = 0; i < dataPostion.length; i++) {
                for (int j = 0; j < arr.size(); j++) {
                    if (arr.get(j).contains(dataPostion[i])) {
                        String temp = arr.get(j);
                        arr.remove(j);
                        return temp;
                    }
                }
            }
        }
        return "";
    }

    public String isAddress(){
        for(int m=0;m<strList.size();m++) {
            ArrayList<String> arr = strList.get(m).getList();
            for (int i = 0; i < dataAddress.length; i++) {
                for (int j = 0; j < arr.size(); j++) {
                    if (arr.get(j).contains(dataAddress[i])) {
                        String returnText = "";
                        for (int k = 0; k < arr.size(); k++) {
                            if (k != 0) returnText += " ";
                            returnText += arr.get(k);
                        }
                        arr.clear();
                        return returnText;
                    }
                }
            }
        }
        return "";
    }

    public String isPhone(){
        for(int m=0;m<strList.size();m++) {
            ArrayList<String> arr = strList.get(m).getList();
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).contains("010")) {
                    String returnText = "";
                    for (int k = 0; k < arr.size(); k++) {
                        returnText += onlyNumeric(arr.get(k));
                    }
                    arr.clear();
                    return returnText;
                }
            }
        }
        for(int m=0;m<strList.size();m++) {
            ArrayList<String> arr = strList.get(m).getList();
            for (int i = 0; i < arr.size(); i++) {
                for (int j = 0; j < dataNumber.length; j++) {
                    if (arr.get(i).contains(dataNumber[j])) {
                        String returnText = "";
                        for (int k = 0; k < arr.size(); k++) {
                            returnText += onlyNumeric(arr.get(k));
                        }
                        arr.clear();
                        return returnText;
                    }
                }
            }
        }
        return "";
    }

    public String isEmail(){
        for(int m=0;m<strList.size();m++) {
            ArrayList<String> arr = strList.get(m).getList();
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).contains("@")) {
                    String returnText = "";
                    returnText += arr.get(i);
                    arr.clear();
                    return returnText;
                }
            }
        }
        return "";
    }

    public String isCompany(){
        for(int m=0;m<strList.size();m++) {
            ArrayList<String> arr = strList.get(m).getList();
            for (int i = 0; i < arr.size(); i++) {
                for (int j = 0; j < dataCompany.length; j++) {
                    if (arr.get(i).contains(dataCompany[j])) {
                        String returnText = "";
                        for (int k = 0; k < arr.size(); k++) {
                            returnText += arr.get(k);
                        }
                        arr.clear();
                        return returnText;
                    }
                }
            }
        }
        return "";
    }

    public String onlyNumeric(String str)
    {
        String temp = "";
        for(int i=0;i<str.length();i++){
            temp += Character.isDigit(str.charAt(i)) ? str.charAt(i) : "";
        }
        return temp;
    }
    public void bcr(){
        email = isEmail();
        address = isAddress();
        position = isPosition();
        company = isCompany();
        phone = isPhone();
        name = isName();
    }
}

interface dataset{
    String dataName[] = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오",
            "서", "신", "권", "황", "안", "송", "류", "전", "홍", "고", "문", "양", "손", "배",
            "백", "유", "남", "심", "노", "성", "주", "우", "구", "나", "전", "진"};
    String dataPostion[] = {"상무", "팀장", "과장", "매니저", "전무", "부사장", "사장", "대리",
            "부장", "사원", "차장", "CEO", "계장", "주임", "연구원", "책임", "수석", "선임", "박사", "교수"};
    String dataAddress[] = {"서울시","서을시", "서울특별시", "서을특별시", "경기도", "인천광역시", "부산광역시", "대구광역시",
            "대전광역시", "광주광역시", "울산광역시","을산광역시", "경상북도", "경상남도", "충청북도", "충청남도","층청북도", "층청남도",
            "전라북도", "전라남도", "강원도"};
    String dataNumber[] = {"010","018", "031"};
    String dataCompany[] = {"학교", "회사", "은행", "(주)", "학과"};
}

