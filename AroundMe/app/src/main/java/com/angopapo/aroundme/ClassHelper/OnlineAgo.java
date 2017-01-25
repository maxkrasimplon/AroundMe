package com.angopapo.aroundme.ClassHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Maravilho Singa on 07/08/16.
 */
public class OnlineAgo extends Activity {

    Context context;

    SimpleDateFormat simpleDateFormat, dateFormat;
    DateFormat timeFormat;
    Date dateTimeNow;
    String timeFromData;
    String pastDate;
    String sDateTimeNow;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEKS_MILLIS = 7 * DAY_MILLIS;
    private static final int MONTHS_MILLIS = 4 * WEEKS_MILLIS;
    //private static final int YEARS_MILLIS = 12 * MONTHS_MILLIS;

    public OnlineAgo() {

        simpleDateFormat = new SimpleDateFormat("dd/M/yyyy HH:mm:ss");
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        timeFormat = new SimpleDateFormat("HH:mm");

        Date now = new Date();
        sDateTimeNow = simpleDateFormat.format(now);

        try {
            dateTimeNow = simpleDateFormat.parse(sDateTimeNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String byIdName(Context context, String name) {
        Resources res = context.getResources();
        return res.getString(res.getIdentifier(name, "string", context.getPackageName()));
    }

    //time_ago_

    public String getOnlineLast(Date startDate) {

        //  date counting is done till todays date
        Date endDate = dateTimeNow;

        //  time difference in milli seconds
        long different = endDate.getTime() - startDate.getTime();

        if (different < MINUTE_MILLIS) {
            return "Online";
        } else if (different < 2 * MINUTE_MILLIS) {
            return "1 min ago";
        } else if (different < 50 * MINUTE_MILLIS) {
            return different / MINUTE_MILLIS + " mins ago";
        } else if (different < 90 * MINUTE_MILLIS) {
            return "1 hour ago";
        } else if (different < 24 * HOUR_MILLIS) {
            timeFromData = timeFormat.format(startDate);
            return timeFromData;
        } else if (different < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else if (different < 7 * DAY_MILLIS){
            return different / DAY_MILLIS + " days ago";
        } else if (different < 2 * WEEKS_MILLIS){
            return different / WEEKS_MILLIS + " week ago";
        } else if (different < 3.5 * WEEKS_MILLIS){
            return different / WEEKS_MILLIS + " weeks ago";
        } else {
            pastDate = dateFormat.format(startDate);
            return pastDate;
        }

    }
}
