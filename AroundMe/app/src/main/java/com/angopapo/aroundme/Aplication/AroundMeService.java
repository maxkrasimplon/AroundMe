package com.angopapo.aroundme.Aplication;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.util.Log;

import com.angopapo.aroundme.LocationHelper.GPSTracker.GPSTracker;

import java.util.ArrayList;

/**
 * Created by Angopapo, LDA on 14.08.16.
 */
public class AroundMeService extends Service {

    ArrayList<Messenger> mClients;
    private final Messenger mMessenger = new Messenger(new ServiceMessageHandler());
    private NotificationManager mNotificationManager;
    private GPSTracker gpsTracker;
    private Activity mBoundActivity = null;
    private boolean bound = false;

    private static boolean isRunning = false;

    public static boolean isRunning()
    {
        return  isRunning;
    }

    private class ServiceMessageHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle args = new Bundle();
            switch(msg.what){
                case Constants.MSG_NEW_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        Log.d("myapp", "service create");
        super.onCreate();
        isRunning = true;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mClients = new ArrayList<Messenger>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("myapp", "service binded");
        bound = true;
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBoundActivity = null;
        bound = false;
        return super.onUnbind(intent);
    }
}
