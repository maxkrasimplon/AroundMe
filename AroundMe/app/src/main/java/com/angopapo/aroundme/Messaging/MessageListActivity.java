package com.angopapo.aroundme.Messaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.ClassHelper.AroundMeMessage;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.InternetHelper.WaitForInternetConnectionView;
import com.angopapo.aroundme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryClient;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;

public class MessageListActivity extends AppCompatActivity implements ActivityWithToolbar {

    private WaitForInternetConnectionView mWaitForInternetConnectionView;

    ListView mMessageListView;
    Toolbar mToolbar;
    String mUserFromId;
    User mUserFrom;
    List<User> mUsers;

    private LinearLayout mMessagesNoFound;
    private LinearLayout mLoadingMessages;

    //Ad
    private AdView mBannerAd;

    private LinearLayout mInternet;

    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        mUserFrom = (User) ParseUser.getCurrentUser();
        mUserFromId = mUserFrom.getObjectId();
        mMessageListView = (ListView) findViewById(R.id.message_list);
        mWaitForInternetConnectionView = (WaitForInternetConnectionView) findViewById(R.id.wait_for_internet_connection);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mMessagesNoFound = (LinearLayout)findViewById(R.id.layout_no_messages);
        mLoadingMessages = (LinearLayout)findViewById(R.id.layout_loading_messages);
        mInternet = (LinearLayout)findViewById(R.id.linearLayout22);

        //Initial ad
        mBannerAd = (AdView)findViewById(R.id.ad_banner);
        if(User.getUser().isVip()){

            mBannerAd.setVisibility(View.GONE);

        }
        else if (User.getUser().isAds()) {

            mBannerAd.setVisibility(View.GONE);

        } else {

            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            Log.i("device id=", deviceId);

            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)  // All emulators
                    //.addTestDevice(deviceId)  // My Galaxy Nexus test phone
                    .build();
            mBannerAd.loadAd(adRequest);
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.drawer_item_messaging);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer = NavigationDrawer.createDrawer(this);


        findUserDialogs();
        findUserDialogs2();
        VerifyInternet();

        // Live query



        LiveQueryClient.on(LiveQueryEvent.CONNECTED, new OnListener() {
            @Override
            public void on(final JSONObject object) {
                //  Subscribe to any event if you need as soon as connect to server
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        VerifyInternet();



                    }
                });

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        final Subscription sub = new BaseQuery.Builder("Messaging")
                .where(AroundMeMessage.COL_RECEIVED_TO, mUserFrom.getObjectId())
                .addField(AroundMeMessage.COL_TEXT)
                .build()
                .subscribe();

        sub.on(LiveQueryEvent.CREATE, new OnListener() {
            @Override
            public void on(JSONObject object) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        findUserDialogs();
                        findUserDialogs3();

                    }
                });
            }
        });

        requestOnlineUser();



    }



    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void findUserDialogs(){

        // Show loading progress
       // showLodingMessaes();


        ParseQuery<AroundMeMessage> getOutMessagesQuery = AroundMeMessage.getParseMessageQuery();
        getOutMessagesQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, mUserFrom);
        ParseQuery<AroundMeMessage> getInMessagesQuery = AroundMeMessage.getParseMessageQuery();
        getInMessagesQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, mUserFrom);
        List<ParseQuery<AroundMeMessage>> messageQueries = new ArrayList<ParseQuery<AroundMeMessage>>();
        messageQueries.add(getOutMessagesQuery);
        messageQueries.add(getInMessagesQuery);
        ParseQuery<AroundMeMessage> getMessagesQuery = ParseQuery.or(messageQueries);
        getMessagesQuery.include("fromUser");
        getMessagesQuery.include("toUser");
        getMessagesQuery.orderByDescending(AroundMeMessage.COL_CREATED_AT);
        getMessagesQuery.fromLocalDatastore();
        getMessagesQuery.findInBackground(new FindCallback<AroundMeMessage>() {
            @Override
            public void done(final List<AroundMeMessage> messages, ParseException e) {

                if (messages != null) {

                    if (messages.size() > 0) {
                        MessagesFound();
                        hideLodingMessaes();

                        mUsers = new ArrayList<User>();
                        for (AroundMeMessage msg : messages) {

                            if (TextUtils.equals(msg.getUserFromId(), mUserFromId) && !mUsers.contains(msg.getUserTo())) {
                                User user = msg.getUserTo();

                                mUsers.add(user);
                            } else if (TextUtils.equals(msg.getUserToId(), mUserFromId) && !mUsers.contains(msg.getUserFrom())){
                                User user = msg.getUserFrom();

                                mUsers.add(user);
                            }
                        }
                        mMessageListView.setAdapter(new MessageListAdapter(MessageListActivity.this, mUsers, mUserFromId));
                        mMessageListView.setOnItemClickListener(new OnDialogItemClickListener());


                    } else {

                       //showNoMessage();
                       // hideLodingMessaes();
                    }
                } else if (e != null) {

                    Log.d("myapp:findUsers", e.toString());
                    //showNoMessage();
                }
                //mWaitForInternetConnectionView.close();
            }
        });

    }

    public void findUserDialogs2(){


        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            // If we have a network connection and a current


            showLodingMessaes();

            ParseQuery<AroundMeMessage> getOutMessagesQuery = AroundMeMessage.getParseMessageQuery();
            getOutMessagesQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, mUserFrom);


            ParseQuery<AroundMeMessage> getInMessagesQuery = AroundMeMessage.getParseMessageQuery();
            getInMessagesQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, mUserFrom);


            List<ParseQuery<AroundMeMessage>> messageQueries = new ArrayList<ParseQuery<AroundMeMessage>>();
            messageQueries.add(getOutMessagesQuery);
            messageQueries.add(getInMessagesQuery);
            ParseQuery<AroundMeMessage> getMessagesQuery = ParseQuery.or(messageQueries);
            getMessagesQuery.include("fromUser");
            getMessagesQuery.include("toUser");
            getMessagesQuery.orderByDescending(AroundMeMessage.COL_CREATED_AT);
            getMessagesQuery.findInBackground(new FindCallback<AroundMeMessage>() {
                @Override
                public void done(List<AroundMeMessage> messages, ParseException e) {


                    if (messages != null) {

                        if (messages.size() > 0) {
                            //MessagesFound();

                            ParseObject.pinAllInBackground(messages, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {


                                    if (e== null){

                                        findUserDialogs();
                                    }
                                }
                            });

                        } else {
                            showNoMessage();
                            hideLodingMessaes();
                        }
                    } else if (e != null) {

                        Log.d("myapp:findUsers", e.toString());
                        hideLodingMessaes();
                        showNoMessage();
                    }
                }
            });


        } else {
            // If there is no connection, let the user know the sync didn't happen

            //mInternet.setVisibility(View.VISIBLE);
            //Toast.makeText(getActivity(), R.string.offline_mode, Toast.LENGTH_LONG).show();
        }


    }

    public void findUserDialogs3(){


        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            // If we have a network connection and a current


            //showLodingMessaes();

            ParseQuery<AroundMeMessage> getOutMessagesQuery = AroundMeMessage.getParseMessageQuery();
            getOutMessagesQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, mUserFrom);


            ParseQuery<AroundMeMessage> getInMessagesQuery = AroundMeMessage.getParseMessageQuery();
            getInMessagesQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, mUserFrom);


            List<ParseQuery<AroundMeMessage>> messageQueries = new ArrayList<ParseQuery<AroundMeMessage>>();
            messageQueries.add(getOutMessagesQuery);
            messageQueries.add(getInMessagesQuery);
            ParseQuery<AroundMeMessage> getMessagesQuery = ParseQuery.or(messageQueries);
            getMessagesQuery.include("fromUser");
            getMessagesQuery.include("toUser");
            getMessagesQuery.orderByDescending(AroundMeMessage.COL_CREATED_AT);
            getMessagesQuery.findInBackground(new FindCallback<AroundMeMessage>() {
                @Override
                public void done(List<AroundMeMessage> messages, ParseException e) {


                    if (messages != null) {

                        if (messages.size() > 0) {
                            //MessagesFound();

                            ParseObject.pinAllInBackground(messages, new SaveCallback() {
                                @Override
                                public void done(ParseException e) {


                                    if (e== null){

                                        findUserDialogs();
                                    }
                                }
                            });

                        } else {
                            //showNoMessage();
                        }
                    } else if (e != null) {

                        Log.d("myapp:findUsers", e.toString());
                        //showNoMessage();
                    }
                }
            });


        } else {
            // If there is no connection, let the user know the sync didn't happen

            //mInternet.setVisibility(View.VISIBLE);
            //Toast.makeText(getActivity(), R.string.offline_mode, Toast.LENGTH_LONG).show();
        }


    }

    public void showNoMessage(){
        mMessagesNoFound.setVisibility(View.VISIBLE);
    }

    public void MessagesFound(){
        mMessagesNoFound.setVisibility(View.GONE);
    }

    public void showLodingMessaes(){
        mLoadingMessages.setVisibility(View.VISIBLE);
    }

    public void hideLodingMessaes(){
        mLoadingMessages.setVisibility(View.GONE);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public int getDriwerId() {
        return 3;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }

    private class OnDialogItemClickListener implements  ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent messengerIntent = new Intent(MessageListActivity.this, MessengerActivity.class);
            messengerIntent.putExtra(MessengerActivity.EXTRA_USER_TO_ID, mUsers.get(position).getObjectId());
            startActivity(messengerIntent);
        }
    }

    public void onPause(){
        super.onPause();


    }

    public void onResume(){
        super.onResume();

        findUserDialogs();
        findUserDialogs2();
    }

// Verify every 45s if user still online tell the server.


    final public void  requestOnlineUser() {


        final Handler handler = new Handler();

        final TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //<some task>

                        VerifyInternet();

                        if (mUserFrom!= null){

                            Calendar calendar = Calendar.getInstance();
                            //calendar.add(Calendar.DATE, 30);
                            Date now = calendar.getTime();

                            mUserFrom.setStilOnline(now);
                            mUserFrom.saveInBackground();

                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timertask, 0, 45000);

    }

    private void VerifyInternet() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            // If we have a network connection and a current
            // logged in user, sync the todos

            mInternet.setVisibility(View.GONE);
            //Toast.makeText(getActivity(),"You are connected", Toast.LENGTH_LONG).show();


        } else {
            // If there is no connection, let the user know the sync didn't happen

            mInternet.setVisibility(View.VISIBLE);
            //Toast.makeText(getActivity(), R.string.offline_mode, Toast.LENGTH_LONG).show();
        }
    }

}
