package com.timia2109.kristwallet.util;

import com.timia2109.kristwallet.KristAPI;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Tim on 14.02.2016.
 */
public class APIResult {
    protected boolean isVaild = true;
    protected Exception error;

    public boolean vaild() {return isVaild;}
    public Exception getError() {return error;}

    public Date parseDate(String date) {
        try {
            DateFormat format = new SimpleDateFormat(KristAPI.DATE_FORMAT, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            return format.parse(date);
        }
        catch (ParseException e) {
            isVaild = false;
            error = e;
            return new Date();
        }
        catch (IllegalArgumentException e) {
            isVaild = false;
            error = e;
            return new Date();
        }
    }

    public static String showDate(Date date, String pattern) {
        try {
            DateFormat format = new SimpleDateFormat(pattern);
            return format.format(date);
        }
        catch (IllegalArgumentException e) {
            return "IllegalArgumentException";
        }
    }
}
