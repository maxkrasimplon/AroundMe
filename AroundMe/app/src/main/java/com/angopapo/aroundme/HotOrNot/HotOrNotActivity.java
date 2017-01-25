package com.angopapo.aroundme.HotOrNot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.Authetication.SaveLocationActivity;
import com.angopapo.aroundme.ClassHelper.AroundMeHotOrNot;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.MyVisitores.MyVisitorsActivity;
import com.angopapo.aroundme.Profile.MyProfile;
import com.angopapo.aroundme.Profile.ProfileUerActivity;
import com.angopapo.aroundme.R;
import com.angopapo.aroundme.VipAccount.VipActivationActivity;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;



public class HotOrNotActivity extends AppCompatActivity implements View.OnClickListener, ActivityWithToolbar {

    private List<User> mUsers;
    private int mCurrentUserPosition = 0;
    private Toolbar mToolbar;

    private ImageView mAbortImage, mMatchImage, mMatcthInfo;
    private LinearLayout mUserNotFoundLayout;
    private LinearLayout control_wrapper;
    private Drawer drawer;
    private Button mRetryButton, mRetryButton2;

    // new value for photo in midle
    User mCurrentUser;
    final Context context = this;

    private LinearLayout mInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_match);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.drawer_item_hot_or_not);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
        SimpleDraweeView imageView =(SimpleDraweeView) findViewById(R.id.profile_photo);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                rippleBackground.startRippleAnimation();
            }
        });

        rippleBackground.startRippleAnimation();

        mInternet = (LinearLayout)findViewById(R.id.linearLayout22);
        VerifyInternet();

       drawer = NavigationDrawer.createDrawer(this);

        requestOnlineUser();


        //mProfilePhoto = (ImageView) mProfileContentLayout.findViewById(R.id.profile_photo);

        mCurrentUser = (User)User.getCurrentUser();
        mAbortImage = (ImageView)findViewById(R.id.image_abort);
        mMatchImage = (ImageView)findViewById(R.id.image_match);
        mMatcthInfo = (ImageView) findViewById(R.id.image_info);
        mUserNotFoundLayout = (LinearLayout)findViewById(R.id.noUsersFound);
        control_wrapper = (LinearLayout)findViewById(R.id.control_wrapper);
        mRetryButton = (Button) findViewById(R.id.button_retry);
        mRetryButton2 = (Button) findViewById(R.id.button_retry2);
        //mProfilePhotoImage = (ImageView) findViewById(R.id.profile_photo);

        mCurrentUser = (User) User.getCurrentUser();
        //mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);


        getUsersForMatching();

        mMatchImage.setOnClickListener(this);
        mAbortImage.setOnClickListener(this);
        mMatcthInfo.setOnClickListener(this);
        mRetryButton.setOnClickListener(this);
        mRetryButton2.setOnClickListener(this);




        // Load profile image
        Uri uriProfle = Uri.parse(mCurrentUser.getPhotoUrl());

        ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                .setResizeOptions(new ResizeOptions(150, 150))
                .setProgressiveRenderingEnabled(true)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();

        AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                .setImageRequest(requestProfle)
                .build();

        imageView.setController(newControllerProfle);




    }


    public void getUsersForMatching(){

        final RippleBackground rippleBackground=(RippleBackground)findViewById(R.id.content);
        ImageView imageView = (ImageView) findViewById(R.id.profile_photo);
        assert imageView != null;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert rippleBackground != null;
                rippleBackground.startRippleAnimation();
            }
        });
                AroundMeHotOrNot.userMatches(mCurrentUser, new FindCallback<AroundMeHotOrNot>() {
                    @Override
                    public void done(List<AroundMeHotOrNot> matches, ParseException e) {
                        if (e == null) {
                            ParseQuery<User> userQuery = User.getUserQuery();
                            userQuery.whereExists(User.COL_NICKNAME);
                            userQuery.whereExists(User.COL_PHOTO);
                            userQuery.whereWithinKilometers(User.COL_GEO_POINT, mCurrentUser.getGeoPoint(), 100);
                            userQuery.whereNotEqualTo(User.COL_ID, mCurrentUser.getObjectId());
                            userQuery.whereExists(User.COL_ID);
                            List<String> matchedUserIdList = new ArrayList<String>();
                            if (matches != null && matches.size() > 0) {
                                for (AroundMeHotOrNot currentMatch : matches) {
                                    if (!matchedUserIdList.contains(currentMatch.getToUser().getObjectId())) {
                                        matchedUserIdList.add(currentMatch.getToUser().getObjectId());
                                    }
                                }
                                userQuery.whereNotContainedIn(User.COL_ID, matchedUserIdList);
                            }
                            userQuery.findInBackground(new FindCallback<User>() {
                                @Override
                                public void done(List<User> users, ParseException e) {
                                    if (users != null) {

                                        mUsers = users;
                                        if (mUsers.size() > 0) {
                                            hideUserNotFound();
                                            showControlWrapper();
                                            selectUser(0);
                                        } else {

                                            rippleBackground.stopRippleAnimation();
                                            showUserNotFound();
                                            hideControlWrapper();
                                        }
                                    } else if (e != null) {

                                        Log.d("myapp:findUsers", e.toString());
                                        showUserNotFound();
                                        hideControlWrapper();
                                        rippleBackground.stopRippleAnimation();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        //});
   // }

    public void switchUser(boolean isMatch){
        if(mUsers != null) {
            HotOrNotFragment hotOrNotFragment = new HotOrNotFragment();
            mUsers.remove(0);
            if(mUsers.size() < 1){
                showUserNotFound();
                hideControlWrapper();
                return;
            } else {
                User nextUser = mUsers.get(0);
                if (nextUser != null) {
                    Bundle args = new Bundle();
                    args.putString(HotOrNotFragment.ARG_USERNAME, nextUser.getNickname());
                    args.putString(HotOrNotFragment.ARG_CITY, nextUser.getAtualCity());
                    args.putString(HotOrNotFragment.ARG_DESCRIPTION, nextUser.getDescription());
                    args.putString(HotOrNotFragment.ARG_PROFILE_PHOTO_URL, nextUser.getPhotoUrl());
                    args.putString(HotOrNotFragment.ARG_AGE, nextUser.getAge().toString());
                    args.putString(HotOrNotFragment.ARG_GENDER, nextUser.getGenderString());
                    args.putString(HotOrNotFragment.ARG_DISTANCE, String.format("%.2f km", nextUser.getGeoPoint().distanceInKilometersTo(mCurrentUser.getGeoPoint())));
                    hotOrNotFragment.setArguments(args);
                }
                FragmentTransaction matchUserFragmentTransaction = getFragmentManager().beginTransaction();
                if (isMatch)
                    matchUserFragmentTransaction.setCustomAnimations(R.animator.match_user_enter, R.animator.match_user_leave);
                else
                    matchUserFragmentTransaction.setCustomAnimations(R.animator.abort_user_enter, R.animator.abort_user_leave);
                matchUserFragmentTransaction.replace(R.id.user_info_frame, hotOrNotFragment).commit();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUsersForMatching();
        VerifyInternet();
    }

    public void selectUser(int position){
        HotOrNotFragment hotOrNotFragment = new HotOrNotFragment();
        User selectedUser = mUsers.get(position);
        if(selectedUser != null) {
            Bundle args = new Bundle();
            args.putString(HotOrNotFragment.ARG_USERNAME, selectedUser.getNickname());
            args.putString(HotOrNotFragment.ARG_CITY, selectedUser.getAtualCity());
            args.putString(HotOrNotFragment.ARG_DESCRIPTION, selectedUser.getDescription());
            args.putString(HotOrNotFragment.ARG_PROFILE_PHOTO_URL, selectedUser.getPhotoUrl());
            args.putString(HotOrNotFragment.ARG_AGE, selectedUser.getAge().toString());
            args.putString(HotOrNotFragment.ARG_GENDER, selectedUser.getGenderString());
            args.putString(HotOrNotFragment.ARG_DISTANCE,String.format("%.2f km", selectedUser.getGeoPoint().distanceInKilometersTo(mCurrentUser.getGeoPoint())));
            hotOrNotFragment.setArguments(args);
        }
        FragmentTransaction matchUserFragmentTransaction =  getFragmentManager().beginTransaction();
        matchUserFragmentTransaction.replace(R.id.user_info_frame, hotOrNotFragment).commitAllowingStateLoss();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.image_abort:

                AroundMeHotOrNot.newMatch(mCurrentUser, mUsers.get(mCurrentUserPosition), false, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            Log.d("myapp", "saveMatch");
                        } else {
                            Log.d("myapp:saveMatch", e.toString());
                        }
                    }
                });
                switchUser(false);
                break;
            case R.id.image_match:

                AroundMeHotOrNot.newMatch(mCurrentUser, mUsers.get(mCurrentUserPosition), true, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("myapp", "saveMatch");

                        } else {

                            Log.d("myapp:saveMatch", e.toString());
                        }
                    }
                });
                switchUser(true);


                break;
            case  R.id.button_retry:

                Intent updateIntent = new Intent(HotOrNotActivity.this, SaveLocationActivity.class);
                HotOrNotActivity.this.startActivity(updateIntent);
                break;

            case  R.id.button_retry2:



                if (mCurrentUser.isVip()) {

                    VisitorActivity();

                } else {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            context);
                    alertDialogBuilder.setTitle(R.string.Main_vip);
                    alertDialogBuilder
                            .setMessage(R.string.Main_vipdo)
                            .setCancelable(false)
                            .setPositiveButton(R.string.Main_vipyes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    VipActivity();
                                }
                            })
                            .setNegativeButton(R.string.Main_vipno, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // if this button is clicked, just close
                                    // the dialog box and do nothing
                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();


                }

                break;
            case R.id.profile_photo:

                Intent loginIntent = new Intent(HotOrNotActivity.this, MyProfile.class);
                HotOrNotActivity.this.startActivity(loginIntent);
                HotOrNotActivity.this.finish();

                break;

            case R.id.image_info:

                Intent profileIntent = new Intent(HotOrNotActivity.this, ProfileUerActivity.class);
                profileIntent.putExtra(ProfileUerActivity.EXTRA_USER_ID, mUsers.get(mCurrentUserPosition).getObjectId());
                HotOrNotActivity.this.startActivity(profileIntent);
        }
    }

    public void showUserNotFound(){
        mUserNotFoundLayout.setVisibility(View.VISIBLE);
    }

    public void hideUserNotFound(){
        mUserNotFoundLayout.setVisibility(View.GONE);
    }

    public void showControlWrapper(){
        control_wrapper.setVisibility(View.VISIBLE);
    }

    public void hideControlWrapper(){
        control_wrapper.setVisibility(View.GONE);
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
        return 2;
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

        // Comment this super call to avoid calling finish()
        // super.onBackPressed();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    protected void VisitorActivity(){
        Intent whoSeeIntent = new Intent(this, MyVisitorsActivity.class);
        startActivity(whoSeeIntent);
    }

    protected void VipActivity(){
        Intent vipIntent = new Intent(this, VipActivationActivity.class);
        startActivity(vipIntent);
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
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("main111", "onPause");
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
}
