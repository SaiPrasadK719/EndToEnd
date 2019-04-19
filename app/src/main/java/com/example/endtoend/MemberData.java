package com.example.endtoend;

import com.example.endtoend.ART.ARTKey;

import java.util.Random;

class MemberData {
    private String name;
    private String color;

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }


    public MemberData(String name) {
        this.name = name;
        this.color = getRandomColor();
    }


    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}