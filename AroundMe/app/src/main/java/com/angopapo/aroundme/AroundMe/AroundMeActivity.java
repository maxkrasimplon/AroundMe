package com.angopapo.aroundme.AroundMe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.Authetication.LoginActivity;
import com.angopapo.aroundme.Authetication.SaveLocationActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.InternetHelper.WaitForInternetConnectionView;
import com.angopapo.aroundme.Profile.ActivityProfileEdit;
import com.angopapo.aroundme.Profile.MyProfile;
import com.angopapo.aroundme.Profile.ProfileUerActivity;
import com.angopapo.aroundme.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import com.google.android.gms.ads.AdRequest;

public class AroundMeActivity extends AppCompatActivity implements ActivityWithToolbar, View.OnClickListener {

    private GridView mUserGrid;
    private Toolbar mToolbar;
    private Drawer Drawer;
    private WaitForInternetConnectionView mWaitForInternetConnectionView;
    private User mCurrentUser;
    private TextView mMaleButton, mFemaleButton, mBothButton;

    private Button mRetryButton;

    private AoundMeAdapter maleAdapter, femaleAdapter, bothAdapter, currentAdapter;

    private AdView mAdBanner;

    private LinearLayout mInternet;

    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aroundme);

        mCurrentUser = (User)User.getCurrentUser();

        mMaleButton = (TextView) findViewById(R.id.button_male);
        mFemaleButton = (TextView) findViewById(R.id.button_female);
        mBothButton = (TextView) findViewById(R.id.button_both);
        mRetryButton = (Button) findViewById(R.id.button_retry);
        mInternet = (LinearLayout)findViewById(R.id.linearLayout22);

        layout = (RelativeLayout) findViewById(R.id.aroundme);

        final TelephonyManager tm =(TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
//        Config.log(tm.getDeviceId());

        mAdBanner = (AdView)findViewById(R.id.ad_banner);




        CheckAll();

        requestOnlineUser();

        VerifyInternet();

        ParsePush.subscribeInBackground("global", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

        mCurrentUser.setOnline(true);
        mCurrentUser.saveInBackground();
        ///

        if(mCurrentUser.isVip()){

            mAdBanner.setVisibility(View.GONE);

        } else if (User.getUser().isAds()) {

            mAdBanner.setVisibility(View.GONE);

        } else {

            String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            String deviceId = md5(android_id).toUpperCase();
            Log.i("device id=",deviceId);

            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    //.addTestDevice(deviceId)
                    .build();


            mAdBanner.loadAd(adRequest);


        }

        if (User.getUser().getNickname().equals("Private Profile")){


            Snackbar.make(layout, R.string.need, Snackbar.LENGTH_INDEFINITE).setAction(R.string.CHANGE, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent loginIntent = new Intent(AroundMeActivity.this, ActivityProfileEdit.class);
                    AroundMeActivity.this.startActivity(loginIntent);
                    AroundMeActivity.this.finish();

                }
            }).setActionTextColor(Color.WHITE).show();
        }


        //  Verify if the current user has valid location
        if (mCurrentUser.getGeoPoint() == null)

        {
            // current user has invalid location, ask him to update
            Intent loginIntent = new Intent(AroundMeActivity.this, SaveLocationActivity.class);
            AroundMeActivity.this.startActivity(loginIntent);
            AroundMeActivity.this.finish();

        }

        mMaleButton.setOnClickListener(this);
        mFemaleButton.setOnClickListener(this);
        mBothButton.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mUserGrid = (GridView) findViewById(R.id.user_near_me_grid);
        mWaitForInternetConnectionView = (WaitForInternetConnectionView)findViewById(R.id.wait_for_internet_connection);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.drawer_item_around_me);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawer = NavigationDrawer.createDrawer(this);
        mUserGrid.setOnItemClickListener(new OnItemClickListener());

        setBothButtonSelected(true);
        getBothUsers();
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

    protected void getMaleUsers(){
       // mWaitForInternetConnectionView.checkInternetConnection(new WaitForInternetConnectionView.OnConnectionIsAvailableListener() {
           // @Override
           // public void onConnectionIsAvailable() {
                if(maleAdapter == null){
                    maleAdapter = new AoundMeAdapter(AroundMeActivity.this, AoundMeAdapter.TYPE_MALE);
                }
                currentAdapter = maleAdapter;
                mUserGrid.setAdapter(currentAdapter);
                currentAdapter.loadObjects();
                mWaitForInternetConnectionView.close();
          //  }
     //   });
    }

    protected void getFemaleUsers(){
      //  mWaitForInternetConnectionView.checkInternetConnection(new WaitForInternetConnectionView.OnConnectionIsAvailableListener() {
       //     @Override
       //     public void onConnectionIsAvailable() {
                if(femaleAdapter == null){
                    femaleAdapter = new AoundMeAdapter(AroundMeActivity.this, AoundMeAdapter.TYPE_FEMALE);
                }
                currentAdapter = femaleAdapter;
                mUserGrid.setAdapter(currentAdapter);
                currentAdapter.loadObjects();
                mWaitForInternetConnectionView.close();
        //    }
       // });
    }

    protected void getBothUsers(){
        //mWaitForInternetConnectionView.checkInternetConnection(new WaitForInternetConnectionView.OnConnectionIsAvailableListener() {
          //  @Override
          //  public void onConnectionIsAvailable() {
                if(bothAdapter == null){
                    bothAdapter = new AoundMeAdapter(AroundMeActivity.this, AoundMeAdapter.TYPE_BOTH);
                }
                currentAdapter = bothAdapter;
                mUserGrid.setAdapter(currentAdapter);
                currentAdapter.loadObjects();
                mWaitForInternetConnectionView.close();
        //    }
       // });
    }

    @Override
    protected void onResume() {
        super.onResume();

        VerifyInternet();

        currentAdapter.notifyDataSetChanged();

        if (ParseUser.getCurrentUser()!= null) {
            // Sync data to Parse
            CheckAll();
            getBothUsers();
        } else {
            GotoLogin();
        }


    }

    public void onBackPressed() {
        //super.onBackPressed();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_male:
                setMaleButtonSelected(true);
                getMaleUsers();
                break;
            case R.id.button_female:
                setFemaleButtonSelected(true);
                getFemaleUsers();
                break;
            case R.id.button_both:
                setBothButtonSelected(true);
                getBothUsers();
                break;
            case  R.id.button_retry:
                getBothUsers();
                break;
        }
    }

    protected void setMaleButtonSelected(boolean selected){
        if(selected){
            mMaleButton.setTextColor(getResources().getColor(R.color.alizarin));
            mMaleButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            mMaleButton.setSelected(true);
            setFemaleButtonSelected(false);
            setBothButtonSelected(false);
        } else {
            mMaleButton.setTextColor(getResources().getColor(R.color.white));
            mMaleButton.setBackgroundColor(getResources().getColor(R.color.alizarin));
            mMaleButton.setSelected(false);
        }
    }

    protected void setFemaleButtonSelected(boolean selected){
        if(selected){
            mFemaleButton.setTextColor(getResources().getColor(R.color.alizarin));
            mFemaleButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            mFemaleButton.setSelected(true);
            setMaleButtonSelected(false);
            setBothButtonSelected(false);
        } else {
            mFemaleButton.setTextColor(getResources().getColor(R.color.white));
            mFemaleButton.setBackgroundColor(getResources().getColor(R.color.alizarin));
            mFemaleButton.setSelected(false);
        }
    }

    protected void setBothButtonSelected(boolean selected){
        if(selected){
            mBothButton.setTextColor(getResources().getColor(R.color.alizarin));
            mBothButton.setBackgroundColor(getResources().getColor(R.color.transparent));
            mBothButton.setSelected(true);
            setFemaleButtonSelected(false);
            setMaleButtonSelected(false);
        } else {
            mBothButton.setTextColor(getResources().getColor(R.color.white));
            mBothButton.setBackgroundColor(getResources().getColor(R.color.alizarin));
            mBothButton.setSelected(false);
        }
    }


    private class OnItemClickListener implements GridView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent profileIntent = new Intent(AroundMeActivity.this, ProfileUerActivity.class);
            profileIntent.putExtra(ProfileUerActivity.EXTRA_USER_ID, currentAdapter.getItem(position).getObjectId());
            AroundMeActivity.this.startActivity(profileIntent);
        }
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
        return 1;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

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

                        if (mCurrentUser!= null){

                            Calendar calendar = Calendar.getInstance();
                            //calendar.add(Calendar.DATE, 30);
                            Date now = calendar.getTime();

                            mCurrentUser.setStilOnline(now);
                            mCurrentUser.saveInBackground();

                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timertask, 0, 45000);

    }

    private void loadFromServer() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereWithinKilometers(User.COL_GEO_POINT, mCurrentUser.getGeoPoint(), 100);
        query.whereExists(User.COL_GEO_POINT);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> todos, ParseException e) {
                if (e == null) {
                    ParseObject.pinAllInBackground((List<ParseUser>) todos,
                            new SaveCallback() {
                                public void done(ParseException e) {
                                    if (e == null) {
                                       // if (!isFinishing()) {

                                                currentAdapter.loadObjects();
                                      //  }
                                    } else {
                                        Log.i("TodoListActivity",
                                                "Error pinning todos: "
                                                        + e.getMessage());
                                    }
                                }
                            });
                } else {
                    Log.i("TodoListActivity",
                            "loadFromServer: Error finding pinned todos: "
                                    + e.getMessage());
                }
            }
        });
    }

    private void CheckAll() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            if (ParseUser.getCurrentUser() != null) {

                // If we have a network connection and a current logged in user

                loadFromServer();


            } else {

                // If we have a network connection but no logged in user, direct
                // the person to log in or sign up.


                GotoLogin();
            }
        } else {

            // If there is no connection, let the user know the sync didn't

            //Toast.makeText(getApplicationContext(), "Your device appears to be offline. Some todos may not have been synced to Parse.", Toast.LENGTH_LONG).show();
            VerifyInternet();
        }

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


    public void GotoLogin(){

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
