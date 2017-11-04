package com.hepthst.indoorclimbingband_ver20.Model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hepthSt on 2017/10/24.
 */

public class CalorieParser {
    private String string;
    private float height;
    private float Calorie;
    private float Speed;
    private String Result;

    public CalorieParser(String s){
        this.string = s;
        String regEX = "^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$";
        Pattern pattern = Pattern.compile(regEX);
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()){
            System.out.println(matcher.group(0));
            string = matcher.group(0);
//            System.out.println("String is :" + string);
        }
        parseCalorie();
    }

    private void parseCalorie() {
        Speed = 0.068f; // m/s
        height = Float.parseFloat(string);
        Calorie = ((height/Speed)/60f)*11;
    }

    public float getCalorie() {
        return Calorie;
    }
}
