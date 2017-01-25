package com.angopapo.aroundme.Messaging;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Authetication.LoginActivity;
import com.angopapo.aroundme.ClassHelper.AroundMeMessage;
import com.angopapo.aroundme.ClassHelper.AroundMeOnPushReceiveListener;
import com.angopapo.aroundme.ClassHelper.AroundMeParsePushReceiver;
import com.angopapo.aroundme.ClassHelper.AroundMeUploader;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.Imagehelper.ImageViewerActivity;
import com.angopapo.aroundme.InternetHelper.WaitForInternetConnectionView;
import com.angopapo.aroundme.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
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


public class MessengerActivity extends AppCompatActivity implements View.OnClickListener, ActivityWithToolbar {

    public static String EXTRA_USER_TO_ID = "user_to_id";

    private ImageView mSendMsgBtn,mAttachImageButton;
    private AroundMeMessenger mAroundMeMessenger;
    private SimpleDraweeView userToPhoto;
    private ListView mMessageList;
    private Toolbar mToolbar;
    private EditText mMsgText;
    private RelativeLayout mImageWithProgressLayout;
    private ImageWithProgress mImageForSend;
    private AroundMeUploader mAroundMeUploader;
    private ParseFile mImageForSendFile = null;
    private WaitForInternetConnectionView mWaitForConnectionView;
    private ImageView back, info;
    private TextView nickname, useronline;
    private com.mikepenz.materialdrawer.view.BezelImageView profilePhoto;

    View rootView;

    private WindowManager.LayoutParams windowLayoutParams;

    private LinearLayout mInternet;

    private User mUser;
    //private User mCurrentUser;

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

    private class ImageWithProgress implements View.OnClickListener{

        private RelativeLayout layout;
        private SimpleDraweeView image;
        private ProgressBar progress;
        private FrameLayout shadow;
        private TextView progressText;
        private ImageView doneImage;
        private ImageView removeImageForSendButton;
        private Timer timer;
        private Handler uiHandler;
        private ValueAnimator doneFadeIn, doneFadeOut, progressFadeIn, progressFadeOut, shadowFadeOut, shadowFadeIn, progressTextFadeOut, progressTextFadeIn;
        private AnimatorSet animatorSet, progressEffectsFadeIn, progressEffectsFadeOut, layoutOut, layoutIn;


        public ImageWithProgress (RelativeLayout imageWithProgressLayout) {
            timer = new Timer();
            layout = imageWithProgressLayout;
            image = (SimpleDraweeView) imageWithProgressLayout.findViewById(R.id.image_for_send);
            progress = (ProgressBar) imageWithProgressLayout.findViewById(R.id.progress_upload);
            progress.setMax(100);
            shadow = (FrameLayout) imageWithProgressLayout.findViewById(R.id.layout_shadow);
            progressText = (TextView)findViewById(R.id.text_progress);
            doneImage = (ImageView)findViewById(R.id.image_done);
            removeImageForSendButton = (ImageView)findViewById(R.id.image_remove_image);
            uiHandler = new Handler();

            //Set onClickListeners
            removeImageForSendButton.setOnClickListener(this);
//            userToPhoto = (ImageView) mProfileContentLayout.findViewById(R.id.profile_photo);
            userToPhoto.setOnClickListener(this);

            //Animation setup
            shadowFadeIn= ObjectAnimator.ofFloat(shadow,"alpha",0f, 1f);
            shadowFadeIn.setDuration(0);
            progressFadeIn = ObjectAnimator.ofFloat(progress,"alpha",0f,1f);
            progressFadeIn.setDuration(0);
            progressTextFadeIn = ObjectAnimator.ofFloat(progressText,"alpha",0f, 1f);
            progressTextFadeIn.setDuration(500);
            doneFadeIn = ObjectAnimator.ofFloat(doneImage,"alpha",0f, 1f);
            doneFadeIn.setDuration(500);
            doneFadeOut = ObjectAnimator.ofFloat(doneImage,"alpha",1f, 0f);
            doneFadeOut.setDuration(500);
            progressFadeOut = ObjectAnimator.ofFloat(progress,"alpha",1f, 0f);
            progressFadeOut.setDuration(500);
            shadowFadeOut = ObjectAnimator.ofFloat(shadow,"alpha",1f, 0f);
            shadowFadeOut.setDuration(500);
            progressTextFadeOut = ObjectAnimator.ofFloat(progressText, "alpha", 1f, 0f);
            progressTextFadeOut.setDuration(500);
            animatorSet = new AnimatorSet();
            progressEffectsFadeIn = new AnimatorSet();
            progressEffectsFadeIn.playTogether(progressFadeIn, shadowFadeIn);
            progressEffectsFadeOut = new AnimatorSet();
            progressEffectsFadeOut.playTogether(doneFadeOut, progressFadeOut, shadowFadeOut);
            layoutOut = (AnimatorSet) AnimatorInflater.loadAnimator(MessengerActivity.this,R.animator.image_for_send_out);
            layoutOut.setTarget(layout);
            layoutOut.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    layout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            layoutIn = (AnimatorSet) AnimatorInflater.loadAnimator(MessengerActivity.this,R.animator.image_for_send_in);
            layoutIn.setTarget(layout);
        }


        public void clear(){
            layout.setVisibility(View.GONE);
        }


        public void layoutOut(){
            layoutOut.start();
        }


        public void setImage(Uri imageUri){
            layout.setVisibility(View.VISIBLE);
            setProgress(0);
            layoutIn.start();
            progressEffectsFadeIn.start();
            progressTextFadeIn.start();
            Log.d("myapp:setImage", "shadowAlpha: " + String.valueOf(shadow.getAlpha()));


            // Load profile image
            //Uri uriProfle = Uri.parse(imageUri);


            ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(imageUri)
                    .setResizeOptions(new ResizeOptions(55, 55))
                    .setProgressiveRenderingEnabled(true)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(requestProfle)
                    .build();

            image.setController(newControllerProfle);

        }

        public void done(){
            doneImage.setVisibility(View.VISIBLE);
            progressTextFadeOut.start();
            doneFadeIn.start();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d("myapp", "done");
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressEffectsFadeOut.start();
                        }
                    });
                }
            }, 1000);
        }

        public void setBitmap (Bitmap bitmap) {
            layout.setVisibility(View.VISIBLE);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);


            // Load profile image
            Uri uriProfle = Uri.parse(String.valueOf(stream.toByteArray()));


            ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                    .setProgressiveRenderingEnabled(true)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(requestProfle)
                    .build();

            image.setController(newControllerProfle);

        }

        public ImageView getImageView(){
            return image;
        }

        public void setProgress(int progressInt){
            progress.setProgress(progressInt);
            progressText.setText(String.valueOf(progressInt)+"%");
        }

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.image_remove_image:
                    layoutOut();
                    mImageForSendFile = null;
                    break;
            }
        }
    }


    private List<AroundMeMessage> mMessages;
    private MessageAdapter mMessageAdapter;

    private Intent mProfileIntent;

    private User userTo, userFrom;
    private String userToId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        // try to see an modification in messages

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mProfileIntent = getIntent();
        userToId = mProfileIntent.getStringExtra(EXTRA_USER_TO_ID);

        mSendMsgBtn = (ImageView) findViewById(R.id.btn_send_msg);
        mAttachImageButton = (ImageView) findViewById(R.id.button_attach_image);
        mImageWithProgressLayout = (RelativeLayout) findViewById(R.id.layout_image_with_progress);
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        userToPhoto = (SimpleDraweeView) findViewById(R.id.user_to_photo);
        nickname = (TextView) findViewById(R.id.user_name);
        useronline = (TextView) findViewById(R.id.user_online);
        back = (ImageView) findViewById(R.id.user_back);
        mMsgText = (EditText) findViewById(R.id.txt_msg);
        mMessageList = (ListView) findViewById(R.id.message_list);

        rootView = findViewById(R.id.root_view);

        mMessageList.setOnItemClickListener(new OnMessageItemClickListener());

        mImageForSend = new ImageWithProgress(mImageWithProgressLayout);

        mInternet = (LinearLayout)findViewById(R.id.linearLayout22);
        VerifyInternet();

        mMsgText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                switch (v.getId()) {
                    case R.id.txt_msg:
                        Log.d("myapp", "txt_msg focus is " + String.valueOf(hasFocus));
                        break;
                }
            }
        });

        mMessages = new ArrayList<AroundMeMessage>();
        mMessageAdapter = new MessageAdapter(this, mMessages, userToId);
        MessageAdapter.setMessageImageListener(new OnMessageImageClicked());

        mSendMsgBtn.setEnabled(false);
        mSendMsgBtn.setOnClickListener(this);
        mAttachImageButton.setOnClickListener(this);
        mMessageList.setAdapter(mMessageAdapter);

        userFrom = (User)User.getCurrentUser();

        readUserInfo();

        AroundMeParsePushReceiver.setOnPushReceiveListener(new OnPushReceiveListener());

        back.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // change the color to signify a click
                onBackPressed();
                finish();




            }
        });

        requestOnlineUser();

        //syncMessagesToServer();

        // Live Query for realtime messaging

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




        // Subscription
        final Subscription sub = new BaseQuery.Builder("Messaging")
                .where(AroundMeMessage.COL_RECEIVED_TO, userFrom.getObjectId())
                .addField(AroundMeMessage.COL_TEXT)
                .build()
                .subscribe();

        sub.on(LiveQueryEvent.CREATE, new OnListener() {
            @Override
            public void on(JSONObject object) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {



                        //refreshMessages();
                        refreshMessages2();
                    }
                });
            }
        });


        userToPhoto.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Intent imageViewerIntent = new Intent(MessengerActivity.this, ImageViewerActivity.class);
                imageViewerIntent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, userTo.getPhotoUrl());
                MessengerActivity.this.startActivity(imageViewerIntent);

            }
        });


        mWaitForConnectionView = (WaitForInternetConnectionView) findViewById(R.id.wait_for_internet_connection);
        //mWaitForConnectionView.checkInternetConnection(new WaitForInternetConnectionView.OnConnectionIsAvailableListener() {
           // @Override
           // public void onConnectionIsAvailable() {

                mWaitForConnectionView.close();

           // }




             //   });


    }

    final public void  readUserInfo(){

                        ParseQuery<User> getUserTo = User.getUserQuery();
                        getUserTo.fromLocalDatastore();
                        getUserTo.getInBackground(userToId, new GetCallback<User>() {
                            @Override
                            public void done(User user, ParseException e) {
                                if (user == null) {

                                } else {
                                    userTo = user;
                                    nickname.setText(user.getNickname());



                                    if (userTo.getLastActive() != null){

                                        useronline.setText(userTo.getOnlineTime());

                                    } else {


                                        if (userTo.getOnlineStatus().equals("online")) {
                                            useronline.setText("Online ");
                                            useronline.setTextColor(Color.WHITE);
                                        } else {

                                            useronline.setText("Offline");
                                            useronline.setTextColor(Color.WHITE);
                                        }

                                    }



                                    if (userTo.getPhotoUrl().isEmpty()) {
                                        if (!userTo.isMale()) {
                                            userToPhoto.setImageResource(R.drawable.profile_default_photo);
                                        } else {
                                            userToPhoto.setImageResource(R.drawable.profile_default_photo);
                                        }
                                    } else {

                                        // Load profile image
                                        Uri uriProfle = Uri.parse(userTo.getPhotoUrl());

                                        ImageRequest requestProfle = ImageRequestBuilder.newBuilderWithSource(uriProfle)
                                                .setResizeOptions(new ResizeOptions(40, 40))
                                                .setProgressiveRenderingEnabled(true)
                                                .setLocalThumbnailPreviewsEnabled(true)
                                                .build();

                                        AbstractDraweeController newControllerProfle = Fresco.newDraweeControllerBuilder()
                                                .setImageRequest(requestProfle)
                                                .build();

                                        userToPhoto.setController(newControllerProfle);

                                    }


                                    mAroundMeMessenger = new AroundMeMessenger(userTo, userFrom);
                                    mSendMsgBtn.setEnabled(true);
                                    refreshMessages();
                                    refreshMessages2();
                                }
                                //  mWaitForConnectionView.close();
                            }
                            //});
                            // }
                        });

        }

    private class OnMessageItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

        }
    }


    private class OnMessageImageClicked implements MessageAdapter.MessageImageListener{

        @Override
        public void onImageClickListener(int position, View view) {
            Intent imageViewerIntent = new Intent(MessengerActivity.this, ImageViewerActivity.class);
            imageViewerIntent.putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, mMessages.get(position).getMessageImageUrl());
            MessengerActivity.this.startActivity(imageViewerIntent);
        }
    }

    @Override
    public void onClick(final View v) {
        switch(v.getId()){
            case R.id.btn_send_msg:

                ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if ((ni != null) && (ni.isConnected())) {
                    // If we have a network connection and a current

                    Log.d("myapp", "send clicked");
                    final String msgText = mMsgText.getText().toString();
                    //String lastText = mMsgText.getText().toString();
                    if(TextUtils.isEmpty(msgText) && mImageForSendFile == null)
                        break;
                    mMsgText.setText("");


                    mImageForSend.clear();
                    AroundMeMessage msg = mAroundMeMessenger.SendMessage(msgText, mImageForSendFile, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            //refreshMessages();
                            refreshMessages2();

                            //syncMessagesToServer();
                            //mMessageAdapter.notifyDataSetChanged();

                        } }, new SendCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });


                    if(mMessages == null){
                        mMessages = new ArrayList<AroundMeMessage>();
                    }
                    mMessageAdapter.add(msg);

                    // set last message


                    Log.d("myapp", "msg text: " + msg.getText());


                } else {
                    // If there is no connection, let the user know the sync didn't happen

                    Toast.makeText(getActivity(), R.string.offline_mode, Toast.LENGTH_LONG).show();
                }




                break;


            case R.id.button_attach_image:


                ConnectivityManager cm2 = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni2 = cm2.getActiveNetworkInfo();
                if ((ni2 != null) && (ni2.isConnected())) {
                    // If we have a network connection and a current

                    mImageForSend.clear();
                    mAroundMeUploader = new AroundMeUploader(MessengerActivity.this);
                    mAroundMeUploader.SendPhotoDialog();


                } else {
                    // If there is no connection, let the user know the sync didn't happen

                    Toast.makeText(getActivity(), R.string.offline_mode, Toast.LENGTH_LONG).show();
                }



                break;
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("myapp", "onActivityResult");
        Uri imageUri = mAroundMeUploader.getImageUri(requestCode, resultCode, data);
        if(imageUri != null){
            mSendMsgBtn.setEnabled(false);
            mImageForSend.setImage(imageUri);
            new UriToByteArrayAsyncTask().execute(imageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class UriToByteArrayAsyncTask extends AsyncTask<Uri, Void, byte[]>{

        @Override
        protected byte[] doInBackground(Uri... params) {
            Uri imageUri = params[0];
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            // Get the cursor
            Cursor cursor = getContentResolver().query(imageUri,
                    filePathColumn, null, null, null);
            // Move to first row
            cursor.moveToFirst();


            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String imgDecodableString = cursor.getString(columnIndex);
            cursor.close();

            Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return stream.toByteArray();
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mImageForSendFile = new ParseFile(bytes);
            mSendMsgBtn.setEnabled(true);
            mAroundMeUploader.uploadParseFile(mImageForSendFile, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    mImageForSend.done();
                }
            }, new ProgressCallback() {
                @Override
                public void done(Integer integer) {
                    mImageForSend.setProgress(integer);
                }
            });
        }
    }

    public void refreshMessages(){
        ParseQuery<AroundMeMessage> messageFromQuery = AroundMeMessage.getParseMessageQuery();
        messageFromQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, userFrom);
        messageFromQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, userTo);


        ParseQuery<AroundMeMessage> messageToQuery = AroundMeMessage.getParseMessageQuery();
        messageToQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, userFrom);
        messageToQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, userTo);


        List<ParseQuery<AroundMeMessage>> messageQueries = new ArrayList<ParseQuery<AroundMeMessage>>();
        messageQueries.add(messageFromQuery);
        messageQueries.add(messageToQuery);
        //messageQueries.contains( "isDraft");
        ParseQuery<AroundMeMessage> messageQuery = ParseQuery.or(messageQueries);
        //messageQuery.fromPin(Application.LOCAL_MESSAGES);
        messageQuery.orderByAscending(AroundMeMessage.COL_CREATED_AT);
        messageQuery.fromLocalDatastore();

        messageQuery.findInBackground(new FindCallback<AroundMeMessage>() {
            @Override
            public void done(List<AroundMeMessage> messages, ParseException e) {
                if (messages != null) {

                    ParseObject.pinAllInBackground(messages);

                    mMessages.clear();
                    mMessages.addAll(messages);
                    mMessageAdapter.notifyDataSetChanged();

                } else {

                    Log.i("Messages", "loadServer: Error finding pinned messages: " + e.getMessage());
                }
            }
        });
    }

    public void refreshMessages2(){
        ParseQuery<AroundMeMessage> messageFromQuery = AroundMeMessage.getParseMessageQuery();
        messageFromQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, userFrom);
        messageFromQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, userTo);
        ParseQuery<AroundMeMessage> messageToQuery = AroundMeMessage.getParseMessageQuery();
        messageToQuery.whereEqualTo(AroundMeMessage.COL_USER_TO, userFrom);
        messageToQuery.whereEqualTo(AroundMeMessage.COL_USER_FROM, userTo);

        List<ParseQuery<AroundMeMessage>> messageQueries = new ArrayList<ParseQuery<AroundMeMessage>>();
        messageQueries.add(messageFromQuery);
        messageQueries.add(messageToQuery);
        ParseQuery<AroundMeMessage> messageQuery = ParseQuery.or(messageQueries);
        messageQuery.orderByAscending(AroundMeMessage.COL_CREATED_AT);
        messageQuery.findInBackground(new FindCallback<AroundMeMessage>() {
            @Override
            public void done(List<AroundMeMessage> messages, ParseException e) {
                if (messages != null) {

                    ParseObject.pinAllInBackground(messages);

                    mMessages.clear();
                    mMessages.addAll(messages);
                    mMessageAdapter.notifyDataSetChanged();

                } else {

                    Log.i("Messages", "loadServer: Error finding pinned messages: " + e.getMessage());
                }
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        //getBothUsers();

        VerifyInternet();
    }



    private class OnPushReceiveListener implements AroundMeOnPushReceiveListener {
        @Override
        public void onPushReceive(JSONObject jsonObject) {
            refreshMessages();
            refreshMessages2();
        }
    }


    private void syncMessagesToServer() {
        // We could use saveEventually here, but we want to have some UI
        // around whether or not the draft has been saved to Server
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if ((ni != null) && (ni.isConnected())) {
            if (ParseUser.getCurrentUser()!= null) {

                // If we have a network connection and a current logged in user,

                ParseQuery<AroundMeMessage> query = AroundMeMessage.getParseMessageQuery();
                query.fromPin(com.angopapo.aroundme.Aplication.Application.LOCAL_MESSAGES);
                query.whereEqualTo("isDraft", true);
                query.whereEqualTo(AroundMeMessage.COL_USER_FROM, userFrom);
                query.findInBackground(new FindCallback<AroundMeMessage>() {
                    public void done(List<AroundMeMessage> todos, ParseException e) {
                        if (e == null) {
                            for (final AroundMeMessage message : todos) {
                                // Set is draft flag to false before
                                // syncing to Parse
                                message.setDraft(false);
                                message.saveInBackground(new SaveCallback() {

                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            // Let adapter know to update view
                                            if (!isFinishing()) {

                                                //refreshMessages2();
                                                //refreshMessages();
                                                mMessageAdapter.notifyDataSetChanged();
                                            }
                                        } else {
                                            // Reset the is draft flag locally
                                            // to true
                                            message.setDraft(true);
                                        }
                                    }

                                });

                            }
                        } else {
                            Log.i("TodoListActivity",
                                    "syncTodosToParse: Error finding pinned todos: "
                                            + e.getMessage());
                        }
                    }
                });
            } else {
                // If we have a network connection but no logged in user, direct
                // the person to log in or sign up.

                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        } else {
            // If there is no connection, let the user know the sync didn't
            // happen
           // Toast.makeText(getApplicationContext(), "Your device appears to be offline. Some todos may not have been synced to Parse.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
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

        //LiveQueryClient.disconnect();
        //Glide.get(getActivity()).clearMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("main111", "onPause");
    }

    final public void  requestOnlineUser() {


        final Handler handler = new Handler();

        final TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        //<some task>

                        VerifyInternet();

                        if (userFrom!= null){

                            Calendar calendar = Calendar.getInstance();
                            //calendar.add(Calendar.DATE, 30);
                            Date now = calendar.getTime();

                            userFrom.setStilOnline(now);
                            userFrom.saveInBackground();

                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timertask, 0, 45000);

    }
}
