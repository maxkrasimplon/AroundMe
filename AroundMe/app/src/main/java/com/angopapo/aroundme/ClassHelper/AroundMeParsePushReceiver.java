package com.angopapo.aroundme.ClassHelper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.angopapo.aroundme.Authetication.LoginActivity;
import com.angopapo.aroundme.Messaging.MessengerActivity;
import com.angopapo.aroundme.MyVisitores.VisitorActivity;
import com.angopapo.aroundme.Profile.MyProfile;
import com.angopapo.aroundme.Profile.ProfileUerActivity;
import com.angopapo.aroundme.R;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Angopapo, LDA on 17.12.16.
 */
public class AroundMeParsePushReceiver extends ParsePushBroadcastReceiver {

    public final static String PARSE_DATA_KEY = "com.parse.Data";
    public final static String USER_ID_KEY = "userId";
    public final static String TYPE_KEY = "type";
    public final static String MESSAGE_KEY = "alert";

    public final static int PUSH_TYPE_MESSAGE = 0;
    public final static int PUSH_TYPE_HOT = 1;
    public final static int PUSH_TYPE_SEEING = 3;
    public final static int PUSH_TYPE_DEFAULT = 2;


    private static AroundMeOnPushReceiveListener onPushReceiveListener = null;

    public static void setOnPushReceiveListener(AroundMeOnPushReceiveListener onPushReceiveListener1){
        onPushReceiveListener = onPushReceiveListener1;
    }



    private JSONObject getDataFromIntent(Intent intent) {
        JSONObject data = null;
        try {
            data = new JSONObject(intent.getExtras().getString(PARSE_DATA_KEY));
        } catch (JSONException e) {
            // Json was not readable...
        }
        return data;
    }

    private Integer getType(JSONObject jsonObject){
        Integer type = null;
        try{
            type = jsonObject.getInt(TYPE_KEY);
        } catch (JSONException e) {
            type = PUSH_TYPE_DEFAULT;
            Log.d("myapp:jsonParse", "error: " + e.toString());
        }
        return type;
    }

    private String getUserId(JSONObject jsonObject){
        String string = null;
        try{
            string = jsonObject.getString(USER_ID_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return string;
    }

    private String getMessage(JSONObject jsonObject){
        String message = null;
        try {
            message = jsonObject.getString(MESSAGE_KEY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        return null;
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject pushObject = getDataFromIntent(intent);
        if(pushObject != null){
            Log.d("myapp", "onPushReceive: pushObject");
            String message = getMessage(pushObject);
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle("AroundMe");
            builder.setContentText(message);

            builder.setSmallIcon(R.drawable.hearts_100);
            String userId = getUserId(pushObject);
            Integer type = getType(pushObject);

            //Vibration
            builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 }); // Vibration sequences you are free to modify these numbers

            //LED
            builder.setLights(Color.RED, 3000, 3000);

            //Sound
            // Uncomment the next line if you want to set an sound on push reception, R.raw.bg_alert is a directoty and audio file name.
            // builder.setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bg_alert));




            if(userId != null && type == PUSH_TYPE_HOT){
                Intent profileIntent = new Intent(context, ProfileUerActivity.class);
                profileIntent.putExtra(ProfileUerActivity.EXTRA_USER_ID, userId);
                PendingIntent profilePendingIntent = PendingIntent.getActivity(context, 0, profileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(profilePendingIntent);
                Log.d("myapp:onPushReceive", "user id: " + userId);

            } else if(userId != null && type == PUSH_TYPE_MESSAGE){

                User currentUser = (User)User.getCurrentUser();

                if(currentUser != null){

                    Intent messengerIntent = new Intent(context, MessengerActivity.class);
                    messengerIntent.putExtra(MessengerActivity.EXTRA_USER_TO_ID, userId);
                    PendingIntent messengerPendingIntent = PendingIntent.getActivity(context, 0, messengerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(messengerPendingIntent);
                }
                else {

                    Intent messengerIntent = new Intent(context, LoginActivity.class);
                    context.startActivity(messengerIntent);
                }



            } else if(userId != null && type == PUSH_TYPE_SEEING) {
                User currentUser = (User)User.getCurrentUser();
                if(currentUser != null){
                    if(currentUser.isVip()){
                        setContentIntentProfileActivity(userId,context,builder);
                    }

                    if(currentUser.isVisitor()){
                        setContentIntentProfileActivity(userId,context,builder);
                    }
                    else {
                        setContentIntentVipActivity(context, builder);
                    }
                } else {
                    setContentIntentMyProfileActivity(context, builder);
                }
            } else {
                Intent mainIntent = new Intent(context, MyProfile.class);
                PendingIntent mainPendingIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(mainPendingIntent);
                Log.d("myapp:onPushReceive", "normal push");
            }

            builder.setAutoCancel(true);
            notificationManager.notify(0, builder.build());


        }
        if(onPushReceiveListener != null && pushObject != null){
            Log.d("myapp", "onPushReceiveCall");
            onPushReceiveListener.onPushReceive(pushObject);
        }
    }

    protected void setContentIntentVipActivity(Context context, NotificationCompat.Builder builder){
        Intent vipIntent = new Intent(context, VisitorActivity.class);
        PendingIntent vipPendingIntent = PendingIntent.getActivity(context, 0, vipIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(vipPendingIntent);
    }

    protected void setContentIntentProfileActivity(String userId, Context context, NotificationCompat.Builder builder){
        Intent profileIntent = new Intent(context, ProfileUerActivity.class);
        profileIntent.putExtra(ProfileUerActivity.EXTRA_USER_ID, userId);
        PendingIntent profilePendingIntent = PendingIntent.getActivity(context, 0, profileIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(profilePendingIntent);
    }

    protected void setContentIntentMyProfileActivity(Context context, NotificationCompat.Builder builder){
        Intent chaIntent = new Intent(context, MyProfile.class);
        PendingIntent chatPendingIntent = PendingIntent.getActivity(context, 0, chaIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(chatPendingIntent);
    }
}
