package com.angopapo.aroundme.MyVisitores;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.Profile.MyProfile;
import com.angopapo.aroundme.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;


public  class VisitorActivity extends AppCompatActivity implements ActivityWithToolbar, View.OnClickListener, InterstitialAdListener {

    final Context context = this;

    TextView  mGetVipButton;

    Button mVositorButton, mButtonBack;

    Toolbar mToolbar;

    OnClick onClickListener;

    User mCurrentUser;
    Typeface fonts1;

    private Dialog progressDialog;
    private Date startDate;
    private Date expiate;
    private RelativeLayout mLocationLayout;
    private Drawer drawer;

    private InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor);


        //mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mVositorButton = (Button) findViewById(R.id.button_travel);
        mButtonBack = (Button) findViewById(R.id.button_back);
        mGetVipButton = (TextView) findViewById(R.id.button_get_vip);
        mLocationLayout = (RelativeLayout) findViewById(R.id.layout_location);

        mVositorButton.setOnClickListener(this);
        mGetVipButton.setOnClickListener(this);


        // Ads

        if (interstitialAd != null) {
            interstitialAd.destroy();
           interstitialAd = null;
        }
            // Create the interstitial unit with a placement ID (generate your own on the Facebook app settings).
            // Use different ID for each ad placement in your app.
            interstitialAd = new InterstitialAd(VisitorActivity.this, getString(R.string.fb_audiance_visitor));

            // Set a listener to get notified on changes or when the user interact with the ad.
            interstitialAd.setAdListener(VisitorActivity.this);


        if (!User.getUser().isAds()) {

            // Load a new interstitial.
            interstitialAd.loadAd();

        }


            if (interstitialAd == null || !interstitialAd.isAdLoaded()) {
                // Ad not ready to show.
                //setLabel("Ad not loaded. Click load to request an ad.");
            } else {
                // Ad was loaded, show it!
                interstitialAd.show();
                //setLabel("");
            }


        mCurrentUser = (User) User.getCurrentUser();


        mButtonBack.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // change the color to signify a click
                onBackPressed();

            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.button_get_vip:

                if (mCurrentUser.isVip()) {

                    //Intent mapIntent = new Intent( this, MyVisitorsActivity.class);
                    //startActivity(mapIntent);
                    startVisitorActivity();

                }  else if (mCurrentUser.isVisitor()){

                    startVisitorActivity();
                }

                else if (mCurrentUser.getCredits() < 100){

                    // ask the user to confirm a deduction and to activate service

                    android.support.v7.app.AlertDialog.Builder notifyLocationServices = new android.support.v7.app.AlertDialog.Builder(VisitorActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.sorry_vip));
                    notifyLocationServices.setMessage(getString(R.string.cost_vip) + (mCurrentUser.getCredits().toString() + " " + getString(R.string.vip_act_expl)));
                    notifyLocationServices.setPositiveButton(getString(R.string.recg_vip), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // buy the service and deduct 100 Credits here

                            Intent intent = new Intent(getApplicationContext(), MyProfile.class);
                            startActivity(intent);
                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();


                }

                else {

                    android.support.v7.app.AlertDialog.Builder notifyLocationServices = new android.support.v7.app.AlertDialog.Builder(VisitorActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.conf_1));
                    notifyLocationServices.setMessage(getString(R.string.conf_2));
                    notifyLocationServices.setPositiveButton(getString(R.string.conf_3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // deduct 100 credits in the user account and activated a service for 30 days
                            showProgressBar(getString(R.string.active_vip));

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, 30);
                            Date expDate = calendar.getTime();

                            mCurrentUser.setVisitorEnd(expDate);
                            mCurrentUser.increment("points", -100);

                            mCurrentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        switch (e.getCode()) {
                                            case ParseException.CONNECTION_FAILED:
                                                Log.d("MyApp", "connection failed");

                                                dismissProgressBar();

                                                Snackbar.make(mLocationLayout, R.string.loc_cant, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();


                                                break;
                                            case ParseException.INTERNAL_SERVER_ERROR:
                                                Log.d("MyApp", "internal server error");

                                                dismissProgressBar();

                                                Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();


                                                break;

                                            default:
                                                Log.d("MyApp", e.toString());

                                                //dismissProgressBar();

                                                break;
                                        }
                                    } else {

                                        dismissProgressBar();
                                        showProgressBar(getString(R.string.active_vip));

                                        mCurrentUser.setVisitor();
                                        mCurrentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                dismissProgressBar();


                                                Snackbar.make(mLocationLayout, R.string.cong_vip, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        startVisitorActivity();

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();

                                                mCurrentUser.fetchInBackground();
                                                //finish();
                                                mVositorButton.setText(getString(R.string.vip_activated));
                                                mVositorButton.setBackgroundResource(R.drawable.buy_button_bg_green);
                                                mVositorButton.setEnabled(false);
                                                mCurrentUser.fetchInBackground();

                                            }
                                        });

                                        // now we have to deduct 100 creduts from current user account
                                        mCurrentUser.fetchInBackground();

                                        dismissProgressBar();

                                    }
                                }
                            });

                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();

                }
                break;


            case R.id.button_travel:
            {


                startVisitorActivity();

            }
            break;

        }

    }

    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {


        }

    }

    protected void startVisitorActivity() {
        Intent whoSeeIntent = new Intent(this, MyVisitorsActivity.class);
        startActivity(whoSeeIntent);
    }


    protected void startVipActivity() {
        Intent vipIntent = new Intent(this, MyProfile.class);
        startActivity(vipIntent);
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
        return 4;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }

    @Override
    public void onBackPressed()
    {

        //finish();
        //android.os.Process.killProcess(android.os.Process.myPid());
        super.onBackPressed(); // Comment this super call to avoid calling finish()
    }

    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        super.onDestroy();
    }

    @Override
    public void onError(Ad ad, AdError error) {
        if (ad == interstitialAd) {
            //Toast.makeText(this, "Interstitial ad failed to load: " + error.getErrorMessage()Toast.LENGTH_LONG).show());
            //Toast.makeText(getBaseContext(), "Interstitial ad failed to load: " + error.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (ad == interstitialAd) {
            //setLabel("Ad loaded. Click show to present!");
            //Toast.makeText(getBaseContext(), "Ad loaded. Click show to present! ", Toast.LENGTH_LONG).show();
            interstitialAd.show();

        }
    }

    @Override
    public void onInterstitialDisplayed(Ad ad) {
        //Toast.makeText(this.getActivity(), "Interstitial Displayed", Toast.LENGTH_SHORT).show();
       // Toast.makeText(getBaseContext(), "Interstitial Displayed" , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInterstitialDismissed(Ad ad) {
        //Toast.makeText(this.getActivity(), "Interstitial Dismissed", Toast.LENGTH_SHORT).show();
       // Toast.makeText(getBaseContext(), "Interstitial Dismissed", Toast.LENGTH_LONG).show();

        // Cleanup.
        interstitialAd.destroy();
        interstitialAd = null;
    }

    @Override
    public void onAdClicked(Ad ad) {
        //Toast.makeText(this.getActivity(), "Interstitial Clicked", Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), "Interstitial Clicked", Toast.LENGTH_LONG).show();
    }



}