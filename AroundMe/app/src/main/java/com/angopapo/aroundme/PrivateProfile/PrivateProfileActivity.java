package com.angopapo.aroundme.PrivateProfile;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.angopapo.aroundme.VipAccount.StoreActivity;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;


public  class PrivateProfileActivity extends AppCompatActivity implements ActivityWithToolbar, View.OnClickListener, InterstitialAdListener {

    final Context context = this;

    TextView  mGetVipButton;

    Button mPrivateButton, mButtonBack;

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

    TextView mActivated, mFirst, mCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_mode);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.private_prof);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawer = NavigationDrawer.createDrawer(this);

        mPrivateButton = (Button) findViewById(R.id.button_travel);
        mButtonBack = (Button) findViewById(R.id.button_back);
        mGetVipButton = (TextView) findViewById(R.id.button_get_vip);
        mLocationLayout = (RelativeLayout) findViewById(R.id.layout_location);

        mActivated = (TextView)findViewById(R.id.textView32);
        mFirst = (TextView)findViewById(R.id.textView41);
        mCost = (TextView)findViewById(R.id.cost);

        mPrivateButton.setOnClickListener(this);
        mGetVipButton.setOnClickListener(this);


            if (User.getUser().isPrivate()){


                mActivated.setText(R.string.you_have);
                mFirst.setText(R.string.you_can);

                if (User.getUser().getPrivateActive().equals("true")){

                    mPrivateButton.setText(R.string.private_active);
                    mCost.setText(R.string.clickprivate);

                } else {

                    mPrivateButton.setText(R.string.publicmode);
                    mCost.setText(R.string.clickpublic);
                    mPrivateButton.setBackgroundResource(R.drawable.buy_button_bg_green);

            }


        } else if (User.getUser().isVip()) {

                mActivated.setText(R.string.you_have);
                mFirst.setText(R.string.you_can);

                if (User.getUser().getPrivateActive().equals("true")) {

                    mPrivateButton.setText(R.string.private_active);
                    mCost.setText(R.string.clickprivate);

                } else {

                    mPrivateButton.setText(R.string.publicmode);
                    mCost.setText(R.string.clickpublic);
                    mPrivateButton.setBackgroundResource(R.drawable.buy_button_bg_green);
                }
            }



        // Ads

        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        // Create the interstitial unit with a placement ID (generate your own on the Facebook app settings).
        // Use different ID for each ad placement in your app.
        interstitialAd = new InterstitialAd(PrivateProfileActivity.this, getString(R.string.fb_audiance_private));

        // Set a listener to get notified on changes or when the user interact with the ad.
        interstitialAd.setAdListener(PrivateProfileActivity.this);





        if (User.getUser().isAds()) {

            // Load a new interstitial.
            //interstitialAd.loadAd();

        } else if (User.getUser().isVip()){

            // Load a new interstitial.
            //interstitialAd.loadAd();
        } else {

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


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.button_travel:


                if (User.getUser().isPrivate() && User.getUser().getPrivateActive().equals("true")) {

                    setPublicmode();

                } else if (User.getUser().isPrivate() && User.getUser().getPrivateActive().equals("false")) {

                    setGhostmode();

                } else if (User.getUser().isVip() && User.getUser().getPrivateActive().equals("true")) {

                    setPublicmode();

                } else if ((User.getUser().isVip() && User.getUser().getPrivateActive().equals("false"))){

                    setGhostmode();

                }  else if  (User.getUser().getCredits() < 100) {

                        // ask the user to confirm a deduction and to activate service

                        android.support.v7.app.AlertDialog.Builder notifyLocationServices = new android.support.v7.app.AlertDialog.Builder(PrivateProfileActivity.this);
                        notifyLocationServices.setTitle(getString(R.string.sorry_vip));
                        notifyLocationServices.setMessage(getString(R.string.cost_vip) + (mCurrentUser.getCredits().toString() + " " + getString(R.string.vip_act_expl)));
                        notifyLocationServices.setPositiveButton(getString(R.string.recg_vip), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // buy the service and deduct 100 Credits here

                                Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
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


                    } else {

                        android.support.v7.app.AlertDialog.Builder notifyLocationServices = new android.support.v7.app.AlertDialog.Builder(PrivateProfileActivity.this);
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

                                mCurrentUser.setPrivateEnd(expDate);
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

                                            mCurrentUser.setPrivate();
                                            mCurrentUser.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {


                                                    mPrivateButton.setText(R.string.private_active2);
                                                    mPrivateButton.setBackgroundResource(R.drawable.buy_button_bg_green);
                                                    mPrivateButton.setEnabled(false);
                                                    mCurrentUser.fetchInBackground();
                                                    dismissProgressBar();
                                                }
                                            });


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


                    /*case R.id.button_get_vip: {


                        startVipActivity();

                    }
                    break;*/

        }

    }

    private class OnClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {


        }

    }

    protected void setGhostmode() {

        showProgressBar("PRIVATE MODE...");


        mCurrentUser.setPrivateActive(true);
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

                    mPrivateButton.setText(R.string.publicmode);
                    mCost.setText(R.string.clickpublic);
                    mActivated.setText(R.string.you_have);
                    mFirst.setText(R.string.you_can);
                    mPrivateButton.setBackgroundResource(R.drawable.buy_button_bg_red);
                    mPrivateButton.setEnabled(true);
                    mCurrentUser.fetchInBackground();

                    dismissProgressBar();

                }
            }
        });
    }

    protected void setPublicmode() {

        showProgressBar("PUBLIC MODE...");

        mCurrentUser.setPrivateActive(false);
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

                    mPrivateButton.setText(R.string.pu);
                    mActivated.setText(R.string.you);
                    mCost.setText(R.string.click1);
                    mCost.setText(R.string.click2);
                    mPrivateButton.setBackgroundResource(R.drawable.buy_button_bg_green);
                    mPrivateButton.setEnabled(true);
                    mCurrentUser.fetchInBackground();

                    dismissProgressBar();


                }
            }
        });
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
        return 6;
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
           // Toast.makeText(getBaseContext(), "Interstitial ad failed to load: " + error.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (ad == interstitialAd) {
            //setLabel("Ad loaded. Click show to present!");
          //  Toast.makeText(getBaseContext(), "Ad loaded. Click show to present! ", Toast.LENGTH_LONG).show();
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
        //Toast.makeText(getBaseContext(), "Interstitial Dismissed", Toast.LENGTH_LONG).show();

        // Cleanup.
        interstitialAd.destroy();
        interstitialAd = null;
    }

    @Override
    public void onAdClicked(Ad ad) {
        //Toast.makeText(this.getActivity(), "Interstitial Clicked", Toast.LENGTH_SHORT).show();
        //Toast.makeText(getBaseContext(), "Interstitial Clicked", Toast.LENGTH_LONG).show();
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }*/
}