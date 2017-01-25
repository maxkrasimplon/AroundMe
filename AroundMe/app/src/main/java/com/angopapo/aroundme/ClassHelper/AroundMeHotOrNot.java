package com.angopapo.aroundme.ClassHelper;

import android.text.TextUtils;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Angopapo, LDA on 01.09.16.
 */
@ParseClassName("HotOrNot")
public class AroundMeHotOrNot extends ParseObject {
    public static String COL_FROM_USER = "fromUser";
    public static String COL_TO_USER = "toUser";
    public static String COL_MATCH = "match";

    public User getFromUser(){
        return (User)this.getParseObject(COL_FROM_USER);
    }

    public User getToUser(){
        return (User)this.getParseObject(COL_TO_USER);
    }

    public void setMatch(boolean match){
        if(match){
            this.put(COL_MATCH, "YES");
        } else {
            this.put(COL_MATCH, "NO");
        }
    }

    public static AroundMeHotOrNot newMatch(User fromUser, User toUser, boolean match){
        AroundMeHotOrNot newMatch = new AroundMeHotOrNot();
        newMatch.setFromUser(fromUser);
        newMatch.setToUser(toUser);
        newMatch.setMatch(match);
        return newMatch;
    }

    public static void newMatch(final User fromUser, User toUser, boolean match, SaveCallback callback){
        AroundMeHotOrNot newMatch = new AroundMeHotOrNot();
        newMatch.setFromUser(fromUser);
        newMatch.setToUser(toUser);
        newMatch.setMatch(match);
        newMatch.saveInBackground(callback);


        if(match == true){
            ParseInstallation toUserInstallation = toUser.getInstallation();
            final ParseQuery<ParseInstallation> installationQuery = ParseInstallation.getQuery();
            if(toUserInstallation == null)
                return;
            installationQuery.getInBackground(toUser.getInstallation().getObjectId(), new GetCallback<ParseInstallation>(){
                @Override
                public void done(ParseInstallation parseInstallation, ParseException e) {
                    ParsePush push = new ParsePush();
                    try {
                        StringBuilder sb = new StringBuilder();
                        sb.append("{\"alert\":\"User ");
                        sb.append(fromUser.getUsername());
                        sb.append(" like you\",\"userId\":\"");
                        sb.append(fromUser.getObjectId());
                        sb.append("\",\"type\":\"");
                        sb.append(AroundMeParsePushReceiver.PUSH_TYPE_HOT);
                        sb.append("\"}");
                        JSONObject jsonObject = new JSONObject(sb.toString());
                        push.setData(jsonObject);
                        push.setQuery(installationQuery);
                        push.sendInBackground();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }
            });
        }
    }

    public static void queryMatch(User user, final QueryMatchFinished callback){
        ParseQuery<AroundMeHotOrNot> isMatchedQuery = getQuery();
        User currentUser = (User)User.getCurrentUser();

        isMatchedQuery.whereEqualTo(COL_FROM_USER, currentUser);
        isMatchedQuery.whereEqualTo(COL_TO_USER, user);
        isMatchedQuery.getFirstInBackground(new GetCallback<AroundMeHotOrNot>() {
            @Override
            public void done(AroundMeHotOrNot aroundMeHotOrNot, ParseException e) {
                if (aroundMeHotOrNot != null) {
                    Log.d("myapp",String.format("aroundMeHotOrNot %s", aroundMeHotOrNot.isMatch()));
                    callback.onQueryMatchFinished(aroundMeHotOrNot.isMatch());
                } else {
                    Log.d("myapp","aroundMeHotOrNot is null");
                    callback.onQueryMatchFinished(null);
                }
            }
        });
    }

    public interface QueryMatchFinished{
        void onQueryMatchFinished(Boolean result);
    }

    public static ParseQuery<AroundMeHotOrNot> getQuery(){
        return new ParseQuery<AroundMeHotOrNot>(AroundMeHotOrNot.class);
    }


    public void setFromUser(User fromUser){
        this.put(COL_FROM_USER, fromUser);
    }

    public void setToUser(User toUser){
        this.put(COL_TO_USER, toUser);
    }

    public String getMatch(){
        return this.getString(COL_MATCH);
    }

    public boolean isMatch(){
        return TextUtils.equals(getMatch(), "YES");
    }

    public static void userMatches(User user, FindCallback<AroundMeHotOrNot> callback){
        ParseQuery<AroundMeHotOrNot> userMatchesQuery = getQuery();
        userMatchesQuery.whereEqualTo(COL_FROM_USER, user);
        userMatchesQuery.include(COL_TO_USER);
        userMatchesQuery.findInBackground(callback);
    }


}
