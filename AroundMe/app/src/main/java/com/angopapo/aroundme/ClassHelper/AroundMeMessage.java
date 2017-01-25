package com.angopapo.aroundme.ClassHelper;

import com.angopapo.aroundme.AroundMeHelper.TimeAgo;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

/**
 * Created by Angopapo, LDA on 18.08.16.
 */
@ParseClassName("Messaging")
public class AroundMeMessage extends ParseObject {

    public static String COL_USER_FROM = "fromUser";
    public static String COL_USER_TO = "toUser";
    public static String COL_IMAGE = "image";
    public static String COL_TEXT = "text";
    public static String COL_READ = "read";
    public static String COL_SENT_FROM = "SendFrom";
    public static String COL_RECEIVED_TO = "ReceivedTo";
    public static String COL_CREATED_AT = "createdAt";
    public static String COL_DRAFT_DATE = "createdAt";


    public static void sendMessage(User userFrom, User userTo, String text){

    }

    public void setRead(boolean isRead){
        put("read", isRead);
    }


    public void setRead(String Read){
        this.put(COL_READ, Read);
    }

    public String getRead(){
        return this.getString(COL_READ);
    }


    public static ParseQuery<AroundMeMessage> getParseMessageQuery(){
        return new ParseQuery<AroundMeMessage>(AroundMeMessage.class);
    }

    public void setFrom(User userFrom){
        this.put(COL_USER_FROM, userFrom);
    }

    public String getUserFromId(){
        return ((User)this.get(COL_USER_FROM)).getObjectId();
    }

    public String getUserToId(){
        return ((User)this.get(COL_USER_TO)).getObjectId();
    }

    public void setTo(User userTo){
        this.put(COL_USER_TO, userTo);
    }



    public String getText(){
        return this.getString(COL_TEXT);
    }

    public void setText(String text){
        this.put(COL_TEXT, text);
    }

    public void setDraftDate(Date draftDate){
        put(COL_DRAFT_DATE, draftDate);
    }

    // Version 1.1
    public boolean isDraft() {
        return getBoolean("isDraft");
    }

    public void setDraft(boolean isDraft) {
        put("isDraft", isDraft);
    }

    public boolean isSeen() {
        return getBoolean("seen");
    }

    public void setSeen(boolean seen) {
        put("seen", seen);
    }

    // live query

    public void setSendFrom(String SendFrom){
        this.put(COL_SENT_FROM, SendFrom);
    }

    public String getSendTo(){
        return this.getString(COL_SENT_FROM);
    }

    public void setReceiveTo(String ReceivedTo){
        this.put(COL_RECEIVED_TO, ReceivedTo);
    }

    public String getReceiveTo(){
        return this.getString(COL_RECEIVED_TO);
    }

    // ends


    public User getUserTo(){
        return (User)this.getParseObject(COL_USER_TO);
    }

    public User getUserFrom(){
        return (User)this.getParseObject(COL_USER_FROM);
    }


    public String getMessageImageUrl(){
        String messageImageUrl = "";
        if(this.getParseFile(COL_IMAGE) != null){
            messageImageUrl = this.getParseFile(COL_IMAGE).getUrl();
        }
        return messageImageUrl;
    }

    public void setImage(ParseFile image){
        put(COL_IMAGE, image);
    }

    public String getTime(){

        Date messageDate = this.getCreatedAt();
        TimeAgo timeAgo = new TimeAgo();

        return timeAgo.getTimeAgo(messageDate);
    }


}
