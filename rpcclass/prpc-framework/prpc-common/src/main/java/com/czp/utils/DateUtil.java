package com.czp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static Date get(String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = sdf.parse(pattern);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return date;
    }
}
