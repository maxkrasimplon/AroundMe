package com.angopapo.aroundme.Messaging;

import android.content.Context;

import com.angopapo.aroundme.Aplication.Constants;
import com.angopapo.aroundme.ClassHelper.AroundMeMessage;
import com.angopapo.aroundme.ClassHelper.AroundMeParsePushReceiver;
import com.angopapo.aroundme.ClassHelper.User;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Angopapo, LDA on 20.08.16.
 */
public class AroundMeMessenger {
    private User userTo, userFrom;




    Context mContex;

    public AroundMeMessenger(User userTo, User userFrom){
        this.userTo = userTo;
        this.userFrom = userFrom;
    }


    public AroundMeMessage SendMessage(String text, ParseFile image, SaveCallback saveCallback, SendCallback sendCallback){
        AroundMeMessage msg = new AroundMeMessage();


        Calendar calendar = Calendar.getInstance();
        Date CreatAt = calendar.getTime();


        msg.setFrom(userFrom);
        msg.setTo(userTo);
        msg.setText(text);
        msg.setSendFrom(userFrom.getObjectId());
        msg.setReceiveTo(userTo.getObjectId());
        msg.setRead("no");
        //msg.setCreatAt(CreatAt);


        if(image != null) {
            msg.setImage(image);
        }


        //msg.setDraft(true);


        // We were saving like this. Now we have to pin
        msg.saveInBackground(saveCallback);
        if(userTo.getInstallation() == null)
            return msg;


        /*ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("objectId", userTo.getInstallation().getObjectId());
        ParsePush push = new ParsePush();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"alert\":\"");
        sb.append(userFrom.getNickname());
        sb.append(": ");
        sb.append(text);
        sb.append("\",\"type\":\"");
        sb.append(AroundMeParsePushReceiver.PUSH_TYPE_MESSAGE);
        //sb.append(String.format("\"userId\":\"%s\"", userFrom.getObjectId()));
        sb.append("\",\"userId\":\"");
        sb.append(userFrom.getObjectId());
        sb.append("\"}");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(sb.toString());
            push.setData(jsonObject);
            push.setChannel(Constants.GLOBAL_PUSH_CHANNEL);
            push.setQuery(query);
            push.sendInBackground(sendCallback);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        return msg;
    }

}
