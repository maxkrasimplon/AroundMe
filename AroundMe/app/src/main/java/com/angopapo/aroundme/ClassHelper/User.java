package com.angopapo.aroundme.ClassHelper;

import android.location.Location;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MSinga Pro on 12.08.15.
 */
@ParseClassName("_User")
public class User extends ParseUser {
    public static final String COL_PHOTO = "photo";
    public static final String COL_LAST_ONLINE = "lastActive";
    public static final String COL_COVER = "CoverPhoto";
    public static final String COL_PHOTO_THUMB = "photo_thumb";
    public static final String COL_AGE = "age";
    public static final String COL_CREDIT = "points";
    private static final String COL_PHONE = "phone";
    public static final String COL_EMAIL_VERIFIED = "emailVerified";
    public static final String COL_EMAIL = "email";
    public static final String COL_INSTALLATION = "installation";
    public static final String COL_IS_MALE = "isMale";
    public static final String COL_GEO_POINT = "geoPoint";
    public static final String COL_VIP = "membervip";
    public static final String COL_ID = "objectId";
    public static final String COL_ONLINE = "online";
    public static final String COL_NICKNAME = "nickname";
    public static final String COL_DIST = "dist";

    // Dates ends

    public static final String COL_VIP1_END = "dateVip1End";
    public static final String COL_VIP2_END = "dateVip2End";

    public static final String COL_TRAVEL_END = "dateTravelEnd";
    public static final String COL_VISITOR_END = "dateVisitorEnd";

    public static final String COL_ADS_ACTIVE = "ads_active";
    public static final String COL_ADS_END = "dateAdsEnd";

    private static final String COL_PRIVATE_END = "datePrivateEnd";


    // here we set the app returns
    public static final String COL_ADS = "user_ads";
    public static final String COL_TRAVEL = "user_travel";
    public static final String COL_VISITOR = "user_visitor";
    public static final String COL_PRIVATE = "user_private";

    public static final String COL_PRIVATE_ACTIVE = "user_is_private";


    // profile adcionals

    public static final String COL_DESCRIPTION = "desc";
    public static final String COL_BIRTHDAY = "birthday";

    public static final String COL_COUNTRY = "country";
    public static final String COL_ATUALCITY = "actualcity";

    public static final String COL_SEXUALITY = "sexuality";
    public static final String COL_ORIENTATION = "orientation";
    public static final String COL_STATUS = "status";

    // Gender Selector
    public static final String GENDER_MALE = "true";
    public static final String GENDER_FEMALE = "false";

    // Phone number
    public String getPhone(){
        return getString(COL_PHONE);
    }

    public void setPhone(String phone){
        put(COL_PHONE, phone);
    }



    public Date getBithdate() {

        Date date = getDate(COL_BIRTHDAY);

        return date;
    }

    public String getBirthDate(){
        if (this.getBithdate() != null){

            Date messageDate = this.getBithdate();
            SimpleDateFormat sdf = null;
            sdf = new SimpleDateFormat("dd/MM/yyyy");

            String time = sdf.format(messageDate);

            return time;
        } else return null;

    }

    public void setStilOnline(Date StileOnline){
        put(COL_LAST_ONLINE, StileOnline);
    }

    public String getOnlineTime(){

        Date messageDate = getDate(COL_LAST_ONLINE);
        //SimpleDateFormat sdf = null;

        OnlineAgo onlineAgo = new OnlineAgo();

        return onlineAgo.getOnlineLast(messageDate);
    }

    public Date getLastActive() {

        Date date = getDate(COL_LAST_ONLINE);

        return date;
    }

    public String getColAdsActive(){
        if (this.getLastActive() != null){

            Date messageDate = this.getLastActive();
            SimpleDateFormat sdf = null;
            sdf = new SimpleDateFormat("dd/MM/yyyy");

            String time = sdf.format(messageDate);

            return time;
        } else return null;

    }

    public void setBirthDay(Date birthday){
        put(COL_BIRTHDAY, birthday);
    }

    /*@Override
    public int hashCode() {
        return this.getObjectId().hashCode();
    }*/

    public String getPhotoUrl(){
        try {
            //String getPhotoUrl = fetchIfNeeded().getString("photo");
            fetchIfNeeded().getString("photo");
        } catch (ParseException e) {
            //Log.e(TAG, "Something has gone terribly wrong with Parse", e);
        }

        ParseFile photoFile = getParseFile("photo");
        if(photoFile != null)
            return getParseFile("photo").getUrl();
        else
            return "";
    }

    public String getCoverUrl(){
        ParseFile photoFile = getParseFile("CoverPhoto");
        if(photoFile != null)
            return getParseFile("CoverPhoto").getUrl();
        else
            return "";
    }

    public boolean isMale(){
        return TextUtils.equals(getString(COL_IS_MALE), "true");
    }

    public boolean isFemale(){
        return TextUtils.equals(getString(COL_IS_MALE), "false");
    }

    public boolean isAdsActive(){
        return TextUtils.equals(getString(COL_ADS_ACTIVE), "true");
    }

    public String getNickname(){
        String nickname = getString(COL_NICKNAME);
        if(nickname != null){
            return nickname;
        } else {
            return "";
        }
    }

    public void setNickname(String nickname){
        put(COL_NICKNAME, nickname);
    }


    public Integer getAge(){
        if(getNumber(COL_AGE) == null)
            return -1;
        else
            return this.getInt(COL_AGE);
    }

    public Integer getCredits(){
        if(getNumber(COL_CREDIT) == null)
            return 0;
        else
            return this.getInt(COL_CREDIT);
    }

    public Integer getDist(){
        if(getNumber(COL_DIST) == null)
            return -1;
        else
            return this.getInt(COL_DIST);
    }

    public String getDescription(){
        if(get(COL_DESCRIPTION) == null) return "";
        else return get(COL_DESCRIPTION).toString();
    }

    public void setGeoPoint(double lat, double lon) {
        put(COL_GEO_POINT, new ParseGeoPoint(lat,lon));
    }

    public void setGeoPoint(Location location){
        put(COL_GEO_POINT, new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
    }

    public void setGeoPoint(ParseGeoPoint location){
        put(COL_GEO_POINT, location);
    }

    public void setDescriptionToTextView(TextView textView){
        if(!TextUtils.isEmpty(getDescription())){
            textView.setVisibility(View.VISIBLE);
            textView.setText(getDescription());
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    public ParseGeoPoint getGeoPoint(){
        return (ParseGeoPoint)get(COL_GEO_POINT);
    }

    public String getGeoPointString(){
        ParseGeoPoint geoPoint = (ParseGeoPoint)get(COL_GEO_POINT);
        if(geoPoint == null)
            return "";
        else {
            //String geoPointString = String.format("%.4f",geoPoint.getLatitude()) + " : " + String.format("%.4f",geoPoint.getLongitude());
            return String.format("%.4f : %.4f", geoPoint.getLongitude(), geoPoint.getLatitude());
        }
    }

    public void setNoVip(){
        put(COL_VIP, "novip");
    }

    public void setVip(){
        put(COL_VIP, "vip");
    }


    public String getEmail(){
        return getString(COL_EMAIL);
    }


    public void setEmail(String email){
        put(COL_EMAIL, email);
    }

    // VIP ends dates here


    public void setVip1End(Date vipEnd1){
        put(COL_VIP1_END, vipEnd1);
    }
    public String getVip1End(){
        return getString(COL_VIP1_END);
    }

    public void setVip2End(Date vipEnd2){
        put(COL_VIP2_END, vipEnd2);
    }
    public String getVip2End(){
        return getString(COL_VIP2_END);
    }


    public void setTravelEnd(Date travelEnd){
        put(COL_TRAVEL_END, travelEnd);
    }

    public void setPrivateEnd(Date travelEnd){
        put(COL_PRIVATE_END, travelEnd);
    }
    public String getTravelEnd(){
        return getString(COL_TRAVEL_END);
    }

    public String getPrivateEnd(){
        return getString(COL_PRIVATE_END);
    }

    public void setVisitorEnd(Date visitorEnd){
        put(COL_VISITOR_END, visitorEnd);
    }
    public String getVisitorEnd(){
        return getString(COL_VISITOR_END);
    }

    public void setAdsEnd(Date adsEnd){
        put(COL_ADS_END, adsEnd);
    }
    public String getAdsEnd(){
        return getString(COL_ADS_END);
    }

    // Vis dates ends here


    public void getPhoto(GetDataCallback callback){
        getParseFile(COL_PHOTO).getDataInBackground(callback);
    }

    public void getCoverPhoto(GetDataCallback callback){
        getParseFile(COL_COVER).getDataInBackground(callback);
    }

    public void setAge(int age) {
        put(COL_AGE, age);
    }


    public void setCredit(int points) {
        put(COL_CREDIT, points);
    }

    public void setDist(int dist) {

        // prev was COL_AGE //
        put(COL_DIST, dist);
    }

    public void setProfilePhoto(ParseFile file) {
        put(COL_PHOTO, file);
    }

    public void setCoverPhoto(ParseFile file) {
        put(COL_COVER, file);
    }

    public void setProfilePhotoThumb(ParseFile file) {
        put(COL_PHOTO_THUMB, file);
    }

    public void setDescription(String description){
        put(COL_DESCRIPTION, description);
    }

    public String getOnlineStatus(){
        if(getString("online") == null) {
            put("online", "no");
            saveInBackground();
            return "offline";
        }
        if(getString("online").equals("yes"))
            return "online";
        else
            return "offline";
    }

    public String getGenderString(){
        if(TextUtils.equals(this.getString(COL_IS_MALE), "true")){
            return "male";
        } else if(TextUtils.equals(this.getString(COL_IS_MALE), "false")){
            return "female";
        } else {
            return "not_defined";
        }
    }

    public String getAdsString(){
        if(TextUtils.equals(this.getString(COL_ADS_ACTIVE), "true")){
            return "yes";
        } else if(TextUtils.equals(this.getString(COL_ADS_ACTIVE), "false")){
            return "no";
        } else {
            return "yes";
        }
    }

    public String getPrivateActive(){
        if(TextUtils.equals(this.getString(COL_PRIVATE_ACTIVE), "true")){
            return "true";
        } else if(TextUtils.equals(this.getString(COL_PRIVATE_ACTIVE), "false")){
            return "false";
        } else {
            return "false";
        }
    }

    public void setAdsString(boolean isAdsActive){
        if(isAdsActive) put(COL_ADS_ACTIVE, "true");
        else if(!isAdsActive)put(COL_ADS_ACTIVE, "false");
    }

    public void setPrivateActive(boolean isPrivateActive){
        if(isPrivateActive) put(COL_PRIVATE_ACTIVE, "true");
        else if(!isPrivateActive)put(COL_PRIVATE_ACTIVE, "false");
    }

    public void setGenderIsMale(boolean isMale){
        if(isMale) put(COL_IS_MALE, "true");
        else if(!isMale)put(COL_IS_MALE, "false");
    }

    public boolean isVip(){
        String vipSting = getString(COL_VIP);
        //if(vipSting == null) return false;
        return TextUtils.equals(vipSting, "vip");
    }

    // set vip features returns here

    public boolean isAds(){
        String vipSting = getString(COL_ADS);
        if(vipSting == null) return false;
        return TextUtils.equals(vipSting, "yes");
    }

    public boolean isTravel(){
        String vipSting = getString(COL_TRAVEL);
        if(vipSting == null) return false;
        return TextUtils.equals(vipSting, "yes");
    }

    public boolean isPrivate(){

        String vipSting = getString(COL_PRIVATE);
        if(vipSting == null) return false;
        return TextUtils.equals(vipSting, "yes");

    }

    public boolean isVisitor(){
        String vipSting = getString(COL_VISITOR);
        if(vipSting == null) return false;
        return TextUtils.equals(vipSting, "yes");
    }


    public void setAds(){
        put(COL_ADS, "no");
    }

    public void setNoTravel(){
        put(COL_TRAVEL, "no");
    }

    public void setNoVisitor(){
        put(COL_VISITOR, "no");
    }

    public void setNoAds(){
        put(COL_ADS, "yes");
    }

    public void setTravel(){
        put(COL_TRAVEL, "yes");
    }

    public void setPrivate(){
        put(COL_PRIVATE, "yes");
    }

    public void setNoPrivate(){
        put(COL_PRIVATE, "no");
    }

    public void setVisitor(){
        put(COL_VISITOR, "yes");
    }


    // Ends here //

    public static User getUser(){
        return (User) ParseUser.getCurrentUser();
    }



    public void setInstallation(ParseInstallation installation){
        put(COL_INSTALLATION,installation);
    }

    public ParseInstallation getInstallation(){
        return (ParseInstallation)get("installation");
    }

    public void setOnline(boolean online){
        if(online){
            put(COL_ONLINE, "yes");
        } else {
            put(COL_ONLINE, "no");
        }
    }

    public boolean isOnline(){
        return TextUtils.equals(getString(COL_ONLINE), "yes");
    }



    // Online Ago



    public void setSexuality(int sexuality) {
        put(COL_SEXUALITY, sexuality);
    }

    public void setStatus(int status) {
        put(COL_STATUS, status);
    }

    public void setOrientation(int orientation) {
        put(COL_ORIENTATION, orientation);
    }



    public void setAtualCity(String atualCity){
        put(COL_ATUALCITY, atualCity);
    }

    public void setCountry(String country){
        put(COL_COUNTRY, country);
    }


    // now we have to retrieve user info

    public Integer getSexuality(){
        if(getNumber(COL_SEXUALITY) == null)
            return -1;
        else
            return this.getInt(COL_SEXUALITY);
    }

    public Integer getStatus(){
        if(getNumber(COL_STATUS) == null)
            return -1;
        else
            return this.getInt(COL_STATUS);
    }

    public Integer getOrientation(){
        if(getNumber(COL_ORIENTATION) == null)
            return -1;
        else
            return this.getInt(COL_ORIENTATION);
    }

    private static void unsubscribeToPush(String userId) {
        ParsePush.unsubscribeInBackground("user_" + userId);
    }

    public String getAtualCity(){
        return getString(COL_ATUALCITY);
    }

    public String getCountry(){
        return getString(COL_COUNTRY);
    }

    public static ParseQuery<User> getUserQuery(){
        return ParseQuery.getQuery(User.class);
    }


}
