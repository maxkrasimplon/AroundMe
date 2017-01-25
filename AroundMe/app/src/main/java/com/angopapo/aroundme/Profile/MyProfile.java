package com.angopapo.aroundme.Profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.AroundMeService;
import com.angopapo.aroundme.Aplication.Constants;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.Imagehelper.ImageViewerActivity;
import com.angopapo.aroundme.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MyProfile extends AppCompatActivity implements ActivityWithToolbar, View.OnClickListener {



    //layouts
    CoordinatorLayout mProfileContentLayout;
    LinearLayout mErrorLayout;
    RelativeLayout mWaitLayout;
    User mCurrentUser;
    private Toolbar mToolbar;
   private Drawer drawer;
    private Dialog progressDialog;

    private Uri picUri;
    private Uri picUri2;


    private static final int REQUEST_EXTERNAL_STORAGE = 173;
    private static final int REQUEST_EXTERNAL_STORAGE_COVER = 164 ;
    private static final int REQUEST_CAMERA = 105 ;
    private static final int REQUEST_CAMERA_COVER = 126 ;

    //boolean
    boolean binded = false;

    final Context context = this;

    private TextView mUsernameText, mDescriptionText, mAgeText, mGenderText, mUsername, mBirthday, mlocation, mStatus, mOriantation, msexuality;
    private SimpleDraweeView mProfilePhotoImage;
    private SimpleDraweeView mCoverPhotoImage;
    private Button mChangeProfile, mChangeCover;

    //Intents
    Intent mLoginIntent;

    public Intent resultIntent;

    //Callback
    private final FetchCallback fetchUserCallback = new FetchCallback();

    private ProgressDialog mProgressDialog;

    private Messenger mServiceMessenger;

    public void updateInfo(){
        if(fetchUserCallback != null)
            mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);
    }

    private class MainActivityMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FILE_UPLOADED:
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMainActivityMessenger = new Messenger(new MainActivityMessageHandler());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        if(!AroundMeService.isRunning()){
            Intent serviceIntent = new Intent(this, AroundMeService.class);
            startService(serviceIntent);
        }


        mProfileContentLayout = (CoordinatorLayout) findViewById(R.id.profile_content_layout);
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

        mChangeProfile = (Button) findViewById(R.id.button_change_profile);
        mChangeCover = (Button) findViewById(R.id.button_change_cover);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //Initialize toolbar

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.profile_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer = NavigationDrawer.createDrawer(this);


        mProfilePhotoImage.setOnClickListener(this);
        mCoverPhotoImage.setOnClickListener(this);

        mCurrentUser = (User) User.getCurrentUser();
        mCurrentUser.setOnline(true);
        mCurrentUser.saveInBackground();
        mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);



        //updateProfilePhotoThumb();


        mChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                ChangePhoto();
            }
        });

        mChangeCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                ChangeCover();
            }
        });

    }

    public void updateProfilePhotoThumb(){

        mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);

    }

    public void ChangePhoto (){

        final CharSequence[] items = {getString(R.string.camera), getString(R.string.gallery)};

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MyProfile.this);

        builder.setTitle(R.string.change);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(getString(R.string.camera))) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestCamera();



                    } else {


                        Camera();
                    }

                } else if (items[item].equals(getString(R.string.gallery))) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestStorage();


                    } else {

                        Gallery();
                    }



                }

            }
        });
        builder.show();

    }

    public void Camera(){

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, 1);

    }

    public void Gallery(){

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 2);

    }

    public void ChangeCover (){

        final CharSequence[] items = {getString(R.string.camera), getString(R.string.gallery)};

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MyProfile.this);

        builder.setTitle(R.string.change_cover);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(getString(R.string.camera))) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestCameraCover();



                    } else {


                        CameraCover();
                    }

                } else if (items[item].equals(getString(R.string.gallery))) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestStorageCover();


                    } else {

                        GalleryCover();
                    }



                }

            }
        });
        builder.show();

    }

    public void CameraCover(){

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, 11);

    }

    public void GalleryCover(){

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 22);

    }



    public void cropCapturedImage(Uri picUri){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(picUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, 3);
    }

    private void performCrop() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri2, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, 4);
        }
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    // Crop cover

    public void cropCapturedImageCover(Uri picUri){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(picUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 256);
        cropIntent.putExtra("outputY", 256);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, 33);
    }

    private void performCropCover() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri2, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, 44);
        }
        catch (ActivityNotFoundException anfe) {
            Toast toast = Toast
                    .makeText(this, "This device doesn't support the crop action!", Toast.LENGTH_SHORT);
            toast.show();
        }


    }


    private class FetchCallback implements GetCallback<ParseUser>{

        @Override
        public void done(ParseUser parseUser, ParseException e) {
            if (e != null) {
                mErrorLayout.setVisibility(View.VISIBLE);
                mWaitLayout.setVisibility(View.GONE);
                mProfileContentLayout.setVisibility(View.GONE);
            } else if (parseUser != null) {
                mCurrentUser = (User) parseUser;

                mUsernameText.setText(mCurrentUser.getNickname());


                if (mCurrentUser.getAge() > 0){
                    mAgeText.setText(mCurrentUser.getAge().toString() + " " + getString(R.string.prof_age));

                    } else mAgeText.setText("Ask me");



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

                mProfilePhotoImage.setController(newControllerProfle);


                // Load cover image
                Uri uriCover = Uri.parse(mCurrentUser.getCoverUrl());

                ImageRequest requestCover = ImageRequestBuilder.newBuilderWithSource(uriCover)
                        .setResizeOptions(new ResizeOptions(150, 240))
                        .setProgressiveRenderingEnabled(true)
                        .setLocalThumbnailPreviewsEnabled(true)
                        .build();

                AbstractDraweeController newControllerCover = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(requestCover)
                        .build();

                mCoverPhotoImage.setController(newControllerCover);



                mDescriptionText.setText(mCurrentUser.getDescription());
                mGenderText.setText(mCurrentUser.getGenderString());
                mUsername.setText("@"+ mCurrentUser.getUsername());


                if (mCurrentUser.getLastActive() != null){

                    if (mCurrentUser.getOnlineTime().equals("Online")){



                    } else if (mCurrentUser.getOnlineTime().equals("1 min ago")){

                        mUsernameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    } else  mUsernameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                } else {


                    if (mCurrentUser.getOnlineStatus().equals("online")) {



                    } mUsernameText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                }

                if (mCurrentUser.getBirthDate() != null){

                    mBirthday.setText(mCurrentUser.getBirthDate());
                } else mBirthday.setText("18+");




                // Country and City
                if (mCurrentUser.getCountry() != null && mCurrentUser.getAtualCity() != null ){

                    mlocation.setText(mCurrentUser.getCountry() + ", " + mCurrentUser.getAtualCity());
                }

                // Only country
                else if (mCurrentUser.getCountry() != null && mCurrentUser.getAtualCity() == null ) {

                    mlocation.setText(mCurrentUser.getCountry());

                    // Only city
                } else if (mCurrentUser.getCountry() == null && mCurrentUser.getAtualCity() != null ){

                    mlocation.setText(mCurrentUser.getAtualCity());

                    // Both are null
                } else mlocation.setText("18+");


                if(mCurrentUser.getStatus() == 0){

                    mStatus.setText(R.string.married);

                } else if(mCurrentUser.getStatus() == 1)

                {
                    mStatus.setText(R.string.dating);

                }
                else if(mCurrentUser.getStatus() == 2)

                {
                    mStatus.setText(R.string.sigle);

                }
                // Orientation

                if(mCurrentUser.getOrientation() == 0){

                    mOriantation.setText(R.string.heterosexual);

                } else if(mCurrentUser.getOrientation() == 1)

                {
                    mOriantation.setText(R.string.homosexual);

                }
                else if(mCurrentUser.getOrientation() == 2)

                {
                    mOriantation.setText(R.string.bisexual);

                }
                else {

                    mOriantation.setText(R.string.indefined);


                }

                // Sexuality

                if(mCurrentUser.getSexuality() == 0){

                    msexuality.setText(R.string.mans);

                } else if(mCurrentUser.getSexuality() == 1)

                {
                    msexuality.setText(R.string.girls);

                }
                else if(mCurrentUser.getSexuality() == 2)

                {
                    msexuality.setText(R.string.both);

                }
                else {

                    msexuality.setText(R.string.indefined);


                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.image_profile_photo:

                showPhoto();


            case R.id.profileCover:

                showCover();

                break;

        }
    }

    public void showPhoto(){

        if (!mCurrentUser.getPhotoUrl().isEmpty()){

            Intent imageViewerActivity = new Intent(this, ImageViewerActivity.class);
            imageViewerActivity.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, mCurrentUser.getPhotoUrl());
            startActivity(imageViewerActivity);

        } else {

            Toast.makeText(getBaseContext(), mCurrentUser.getNickname() + " " + getString(R.string.noprofile), Toast.LENGTH_LONG).show();
        }
    }

    public void showCover(){

        if (!mCurrentUser.getCoverUrl().isEmpty()){

            Intent imageViewerActivity = new Intent(this, ImageViewerActivity.class);
            imageViewerActivity.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, mCurrentUser.getCoverUrl());
            startActivity(imageViewerActivity);

        } else {

            Toast.makeText(getBaseContext(), mCurrentUser.getNickname() + " " + getString(R.string.no_cover), Toast.LENGTH_LONG).show();
        }

    }



    private class MainActivitySaveCallback implements SaveCallback{

        @Override
        public void done(ParseException e) {
            Toast.makeText(MyProfile.this,"save compleate",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){

         super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("main111", "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("main111", "onPause");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1){
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
            try {
                cropCapturedImage(Uri.fromFile(file));
            }
            catch(ActivityNotFoundException aNFE){
                String errorMessage = getString(R.string.no_support);
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(requestCode==2){


            if (data != null){

                picUri2 = data.getData();
                performCrop();
            }

        }
        if(requestCode==3){




            Bundle extras = data.getExtras();

            if (extras != null){
                Bitmap bitmap = extras.getParcelable("data");

                showProgressBar(getString(R.string.updating));

            // Convert it to byte
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Compress image to lower quality scale 1 - 100
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();

            // Create the ParseFile
            final ParseFile file = new ParseFile("profile.png", image);
            // Upload the image into Server

            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){

                        mCurrentUser.setProfilePhoto(file);
                        mCurrentUser.setProfilePhotoThumb(file);
                        mCurrentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {

                                if (e == null){

                                    dismissProgressBar();

                                    mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);
                                } else{


                                    dismissProgressBar();
                                }


                            }
                        });
                         }


                    }
                });

            }
        }

        if(requestCode==4){



            Bundle extras = data.getExtras();

            if (extras != null) {
                Bitmap bitmap = extras.getParcelable("data");
                showProgressBar(getString(R.string.upf));
                // Convert it to byte
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress image to lower quality scale 1 - 100
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();

                // Create the ParseFile
                final ParseFile file = new ParseFile("profile.png", image);
                // Upload the image into Server

                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {


                            mCurrentUser.setProfilePhoto(file);
                            mCurrentUser.setProfilePhotoThumb(file);
                            mCurrentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e == null) {

                                        dismissProgressBar();

                                        mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);
                                    } else {


                                        dismissProgressBar();
                                    }


                                }
                            });
                        }


                    }
                });

            }
        }

        // Cover photo

        if(requestCode==11){
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
            try {
                cropCapturedImageCover(Uri.fromFile(file));
            }
            catch(ActivityNotFoundException aNFE){
                String errorMessage = getString(R.string.sorry_crop);
                Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        if(requestCode==22){

            if (data != null){

                picUri2 = data.getData();
                performCropCover();
            }


        }
        if(requestCode==33){



            Bundle extras = data.getExtras();

            if (extras != null) {

                Bitmap bitmap = extras.getParcelable("data");

                showProgressBar("Updating cover...");
                // Convert it to byte
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress image to lower quality scale 1 - 100
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();

                // Create the ParseFile
                final ParseFile file = new ParseFile("profile.png", image);
                // Upload the image into Server

                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {


                            mCurrentUser.setCoverPhoto(file);
                            mCurrentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e == null) {

                                        dismissProgressBar();

                                        mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);
                                    } else {


                                        dismissProgressBar();
                                    }


                                }
                            });
                        }


                    }
                });
            }
        }

        if(requestCode==44){



            Bundle extras = data.getExtras();

            if (extras != null) {
                Bitmap bitmap = extras.getParcelable("data");

                showProgressBar("Updating cover...");
                // Convert it to byte
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress image to lower quality scale 1 - 100
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] image = stream.toByteArray();

                // Create the ParseFile
                final ParseFile file = new ParseFile("profile.png", image);
                // Upload the image into Server

                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {


                            mCurrentUser.setCoverPhoto(file);
                            mCurrentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if (e == null) {

                                        dismissProgressBar();

                                        mCurrentUser.fetchIfNeededInBackground(fetchUserCallback);
                                    } else {


                                        dismissProgressBar();
                                    }


                                }
                            });
                        }


                    }
                });
            }
        }


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Take appropriate action for each action item click
        switch (item.getItemId()) {
            case R.id.action_edit_profile:

                Intent loginIntent = new Intent(MyProfile.this, ActivityProfileEdit.class);
                MyProfile.this.startActivity(loginIntent);
                MyProfile.this.finish();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void showInternetConnectionLostMessage(){
        Snackbar.make(mProfileContentLayout, R.string.settings_no_inte, Snackbar.LENGTH_SHORT).show();

    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
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
        return 7;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }

    public void PermissionRequestStorage(){

        if (ContextCompat.checkSelfPermission(MyProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MyProfile.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(MyProfile.this)
                        .setTitle(R.string.per)
                        .setMessage(R.string.st)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        } else {

            Gallery() ;
        }
    }

    public void PermissionRequestStorageCover(){

        if (ContextCompat.checkSelfPermission(MyProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MyProfile.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(MyProfile.this)
                        .setTitle(R.string.per2)
                        .setMessage(R.string.pers)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE_COVER);
            }
        } else {

            GalleryCover();
        }
    }

    public void PermissionRequestCamera(){
        if (ContextCompat.checkSelfPermission(MyProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MyProfile.this,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(MyProfile.this)
                        .setTitle(R.string.perss)
                        .setMessage(R.string.percm)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);

            }
        } else{

            Camera();
        }
    }

    public void PermissionRequestCameraCover(){
        if (ContextCompat.checkSelfPermission(MyProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MyProfile.this,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(MyProfile.this)
                        .setTitle(R.string.prmii)
                        .setMessage(R.string.camer)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(MyProfile.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_COVER);

            }
        } else{

            CameraCover();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Gallery();

                } else {

                    Context context = MyProfile.this;
                    CharSequence text = getString(R.string.deny);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return;
            }

            case REQUEST_EXTERNAL_STORAGE_COVER: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    GalleryCover();

                } else {

                    Context context = MyProfile.this;
                    CharSequence text = getString(R.string.day2);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return;
            }

            case REQUEST_CAMERA:{

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Camera();


                } else {

                    Context context = MyProfile.this;
                    CharSequence text = getString(R.string.dany3);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }
                return;

            }

            case REQUEST_CAMERA_COVER:{

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    CameraCover();

                } else {

                    Context context = MyProfile.this;
                    CharSequence text = getString(R.string.danyr);
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }
                return;

            }

        }
    }
}