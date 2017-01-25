package com.angopapo.aroundme.Profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.ClassHelper.AroundMeHotOrNot;
import com.angopapo.aroundme.ClassHelper.AroundMeVisitors;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.Imagehelper.ImageViewerActivity;
import com.angopapo.aroundme.InternetHelper.WaitForInternetConnectionView;
import com.angopapo.aroundme.Messaging.MessengerActivity;
import com.angopapo.aroundme.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ProfileUerActivity extends AppCompatActivity implements View.OnClickListener, ActivityWithToolbar {

    public final static String EXTRA_USER_ID = "userId";

    private User mUser;
    private User mCurrentUser;

    private TextView mUsernameText, mDescriptionText, mAgeText, mGenderText, mUsername, mBirthday, mlocation, mStatus, mOriantation, msexuality;
    private FloatingActionButton mSendMessageButton;
    private SimpleDraweeView mProfilePhotoImage;
    private SimpleDraweeView mCoverPhotoImage;
    private Toolbar mToolbar;
    private ImageView mAbortImage, mHeartImage;
    private WaitForInternetConnectionView mWaitForInternetConnectionView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Get current user
        mCurrentUser = (User)User.getCurrentUser();
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mUsernameText = (TextView)findViewById(R.id.text_username);
        mUsername = (TextView)findViewById(R.id.profileUsername);
        mDescriptionText = (TextView)findViewById(R.id.desc);
        mAgeText = (TextView)findViewById(R.id.text_age);

        mBirthday = (TextView)findViewById(R.id.birthday);
        mlocation = (TextView)findViewById(R.id.location);
        mStatus = (TextView)findViewById(R.id.status);
        mOriantation = (TextView)findViewById(R.id.oriantation);
        msexuality = (TextView)findViewById(R.id.sexuality);

        mGenderText = (TextView)findViewById(R.id.text_gender);
        mProfilePhotoImage = (SimpleDraweeView)findViewById(R.id.image_profile_photo);
        mCoverPhotoImage = (SimpleDraweeView)findViewById(R.id.profileCover);
        mSendMessageButton = (FloatingActionButton) findViewById(R.id.button_send_message);

        mHeartImage = (ImageView)findViewById(R.id.image_heart);
        mAbortImage = (ImageView)findViewById(R.id.image_abort);

        mProfilePhotoImage.setOnClickListener(this);
        mCoverPhotoImage.setOnClickListener(this);

        mWaitForInternetConnectionView = (WaitForInternetConnectionView)findViewById(R.id.wait_for_internet_connection);
       // mWaitForInternetConnectionView.checkInternetConnection(new WaitForInternetConnectionView.OnConnectionIsAvailableListener() {
           // @Override
          //  public void onConnectionIsAvailable() {

                ParseQuery<User> userQuery = User.getUserQuery();
                Log.d("mypp:ProfileAct", getIntent().getStringExtra(EXTRA_USER_ID));
                userQuery.fromLocalDatastore();
                userQuery.getInBackground(getIntent().getStringExtra(EXTRA_USER_ID), new GetCallback<User>() {
                    @Override
                    public void done(User user, ParseException e) {
                        if (user != null) {

                            mUser = user;
                            mUsernameText.setText(user.getNickname());

                            if (user.getDist() != null){

                                getSupportActionBar().setTitle(String.format("%.2f km", user.getGeoPoint().distanceInKilometersTo(mCurrentUser.getGeoPoint())) + " between you");

                            } else getSupportActionBar().setTitle("Profile");


                            if (user.getLastActive() != null){

                                if (user.getOnlineTime().equals("Online")){



                                } else if (user.getOnlineTime().equals("1 min ago")){

                                    mUsernameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                                } else mUsernameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                            } else {


                                if (user.getOnlineStatus().equals("online")) {


                                } mUsernameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                            }

                            if (user.getAge() > 0) {
                                mAgeText.setText(user.getAge().toString() + " " + getString(R.string.prof_age));

                            } else mAgeText.setText("Ask me");

                            mDescriptionText.setText(user.getDescription());

                            if(user.getAge() != null){
                                String gender = user.getGenderString();
                                if(TextUtils.equals(gender, "male")) mGenderText.setText(R.string.male);
                                else if(TextUtils.equals(gender, "female")) mGenderText.setText(R.string.female);
                                else if(TextUtils.equals(gender, "not_defined")) mGenderText.setText(R.string.other);

                            }

                            mUsername.setText("@"+ user.getUsername());

                            if (user.getBirthDate() != null){

                                mBirthday.setText(user.getBirthDate());
                            } else mBirthday.setText("Ask me");




                            // Country and City
                            if (user.getCountry() != null && user.getAtualCity() != null ){

                                mlocation.setText(user.getCountry() + ", " +user.getAtualCity());
                            }

                            // Only country
                            else if (user.getCountry() != null && user.getAtualCity() == null ) {

                                mlocation.setText(user.getCountry());

                             // Only city
                            } else if (user.getCountry() == null && user.getAtualCity() != null ){

                                mlocation.setText(user.getAtualCity());

                             // Both are null
                            } else mlocation.setText("Ask me");


                            if(user.getStatus() == 0){

                                mStatus.setText(R.string.married);

                            } else if(user.getStatus() == 1)

                            {
                                mStatus.setText(R.string.dating);

                            }
                            else if(user.getStatus() == 2)

                            {
                                mStatus.setText(R.string.sigle);

                            }
                            // Orientation

                            if(user.getOrientation() == 0){

                                mOriantation.setText(R.string.heterosexual);

                            } else if(user.getOrientation() == 1)

                            {
                                mOriantation.setText(R.string.homosexual);

                            }
                            else if(user.getOrientation() == 2)

                            {
                                mOriantation.setText(R.string.bisexual);

                            }
                            else {

                                mOriantation.setText(R.string.indefined);


                            }

                            // Sexuality

                            if(user.getSexuality() == 0){

                                msexuality.setText(R.string.mans);

                            } else if(user.getSexuality() == 1)

                            {
                                msexuality.setText(R.string.girls);

                            }
                            else if(user.getSexuality() == 2)

                            {
                                msexuality.setText(R.string.both);

                            }
                            else {

                                msexuality.setText(R.string.indefined);


                            }

                            AroundMeHotOrNot.queryMatch(mUser, new AroundMeHotOrNot.QueryMatchFinished() {
                                @Override
                                public void onQueryMatchFinished(Boolean result) {
                                    if (result != null) {
                                        if (result) mHeartImage.setVisibility(View.VISIBLE);
                                        else mAbortImage.setVisibility(View.VISIBLE);
                                    } else {
                                        mHeartImage.setVisibility(View.GONE);
                                        mAbortImage.setVisibility(View.GONE);
                                    }

                                }
                            });


                            // Load profile image
                            Uri uriProfle = Uri.parse(user.getPhotoUrl());

                            ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                                    .setResizeOptions(new ResizeOptions(150, 150))
                                    .setProgressiveRenderingEnabled(true)
                                    .setLocalThumbnailPreviewsEnabled(true)
                                    .build();

                            AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                                    .setImageRequest(requestProfle)
                                    .build();

                            mProfilePhotoImage.setController(newControllerProfle);


                            // Load cover image
                            Uri uriCover = Uri.parse(user.getCoverUrl());

                            ImageRequest requestCover = ImageRequestBuilder.newBuilderWithSource(uriCover)
                                    .setResizeOptions(new ResizeOptions(150, 240))
                                    .setProgressiveRenderingEnabled(true)
                                    .setLocalThumbnailPreviewsEnabled(true)
                                    .build();

                            AbstractDraweeController newControllerCover = Fresco.newDraweeControllerBuilder()
                                    .setImageRequest(requestCover)
                                    .build();

                            mCoverPhotoImage.setController(newControllerCover);


                            mSendMessageButton.setEnabled(true);
                            mSendMessageButton.setOnClickListener(ProfileUerActivity.this);

                            // begin of pushs

                           final AroundMeVisitors visited = AroundMeVisitors.createWhoSee(mCurrentUser, mUser);

                            if(visited != null){
                                visited.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            visited.sendPush(new SendCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e == null){
                                                        Log.d("myapp", "who see push sended");
                                                    } else {
                                                        Log.d("myapp", "who see push error: " + e.toString());
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.d("myapp","WhoSee not save: " + e.toString());
                                        }
                                    }
                                });
                            } // Ends push

                                    mWaitForInternetConnectionView.close();
                        }

                    }
                });
         //   }
      //  });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button_send_message:
                Intent messengerIntent = new Intent(this, MessengerActivity.class);
                messengerIntent.putExtra(MessengerActivity.EXTRA_USER_TO_ID, mUser.getObjectId());
                startActivity(messengerIntent);
                break;
            case R.id.image_profile_photo:

                    showPhoto();

                break;

            case R.id.profileCover:

                showCover();

                break;

        }
    }

    public void showPhoto(){

        if (!mUser.getPhotoUrl().isEmpty()){

            Intent imageViewerActivity = new Intent(this, ImageViewerActivity.class);
            imageViewerActivity.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, mUser.getPhotoUrl());
            startActivity(imageViewerActivity);

        } else {

            Toast.makeText(getBaseContext(), mUser.getNickname() + " " + getString(R.string.has), Toast.LENGTH_LONG).show();
        }
    }

    public void showCover(){

        if (!mUser.getCoverUrl().isEmpty()){

            Intent imageViewerActivity = new Intent(this, ImageViewerActivity.class);
            imageViewerActivity.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, mUser.getCoverUrl());
            startActivity(imageViewerActivity);

        } else {

            Toast.makeText(getBaseContext(), mUser.getNickname() + " " + getString(R.string.has2), Toast.LENGTH_LONG).show();
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
        return -1;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }
}
