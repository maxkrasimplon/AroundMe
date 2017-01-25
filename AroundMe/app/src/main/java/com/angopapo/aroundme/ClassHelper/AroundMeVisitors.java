package com.angopapo.aroundme.ClassHelper;

import android.text.TextUtils;
import android.util.Log;

import com.angopapo.aroundme.AroundMeHelper.TimeAgo;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by Angopapo, LDA on 11.09.16.
 */
@ParseClassName("Visitors")
public class AroundMeVisitors extends ParseObject{

    public static final String COL_WHO_SEE_USER = "userSee";
    public static final String COL_SEEING_USER = "seeUser";

    private User mWhoSeeUser, mSeeingUser;

    public void setWhoSeeUser(User whoSeeUser){
        mWhoSeeUser = whoSeeUser;
        put(COL_WHO_SEE_USER, whoSeeUser.getObjectId());
    }

    public void setSeeingUser(User seeingUser){
        mSeeingUser = seeingUser;
        put(COL_SEEING_USER,seeingUser.getObjectId());
    }

    public String getSeeingUserId(){
        return getString(COL_SEEING_USER);
    }

    public String getWhoSeeUserId(){
        return getString(COL_WHO_SEE_USER);
    }


    public void sendPush(SendCallback callback){
        ParseQuery<ParseInstallation> parseInstallationQuery = ParseInstallation.getQuery();
        if(mSeeingUser.getInstallation() == null)
            return;
        parseInstallationQuery.whereEqualTo("objectId", mSeeingUser.getInstallation().getObjectId());
        ParsePush push = new ParsePush();

        StringBuilder pushDataStringBuilder = new StringBuilder();
        pushDataStringBuilder.append("{\"alert\":\"Your profile has been seeing\",");
        pushDataStringBuilder.append(String.format("\"type\":\"%d\",", AroundMeParsePushReceiver.PUSH_TYPE_SEEING));
        pushDataStringBuilder.append(String.format("\"userId\":\"%s\"", mWhoSeeUser.getObjectId()));
        pushDataStringBuilder.append("}");

        try {
            JSONObject pushData = new JSONObject(pushDataStringBuilder.toString());
            push.setData(pushData);
            push.setQuery(parseInstallationQuery);
            push.sendInBackground(callback);
        } catch (JSONException jsonException) {
            Log.d("myapp", String.format("JsonException: %s", jsonException.toString()));
        }
    }

    public static void getWhoSeeList(User seeingUser, final GetWhoSeeUsersCallback callback){
        final ParseQuery<AroundMeVisitors> whoSeeParseQuery = getWhoSeeQuery();
        whoSeeParseQuery.whereEqualTo(COL_SEEING_USER, seeingUser.getObjectId());
        whoSeeParseQuery.findInBackground(new FindCallback<AroundMeVisitors>() {
            @Override
            public void done(List<AroundMeVisitors> whoSeeList, ParseException e) {
                if (e == null) {
                    callback.done(whoSeeList);
                } else {
                    callback.done(null);
                }
            }
        });
    }

    public String getDateSting(){

        Date visitorData = this.getCreatedAt();
        TimeAgo timeAgo = new TimeAgo();

        return timeAgo.getTimeAgo(visitorData);
    }

    public interface GetWhoSeeUsersCallback{
        void done(List<AroundMeVisitors> whoSeeUsers);
    }

    public static ParseQuery<AroundMeVisitors> getWhoSeeQuery(){
        return new ParseQuery<AroundMeVisitors>(AroundMeVisitors.class);
    }

    public static AroundMeVisitors createWhoSee(User whoSeeUser, User seeingUser){

        if(!TextUtils.equals(whoSeeUser.getObjectId(),seeingUser.getObjectId())){
            AroundMeVisitors newWhoSee = new AroundMeVisitors();
            newWhoSee.setWhoSeeUser(whoSeeUser);
            newWhoSee.setSeeingUser(seeingUser);
            return newWhoSee;

        } else {

            return null;
        }
    }
}
