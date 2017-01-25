package com.angopapo.aroundme.Aplication;

import android.graphics.Bitmap;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.angopapo.aroundme.ClassHelper.AroundMeHotOrNot;
import com.angopapo.aroundme.ClassHelper.AroundMeMessage;
import com.angopapo.aroundme.ClassHelper.AroundMeVisitors;
import com.angopapo.aroundme.ClassHelper.User;
import com.facebook.FacebookSdk;
import com.facebook.accountkit.AccountKit;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;
import tgio.parselivequery.LiveQueryClient;

//import com.facebook.stetho.Stetho;
//import com.parse.interceptors.ParseLogInterceptor;
//import com.parse.interceptors.ParseStethoInterceptor;

/**
 * Created by Angopapo, LDA on 07.08.16.
 */
public class Application extends MultiDexApplication {


    protected static final String PARSE_APP_ID_TEST = "3fDRtCScpeir3rgUpHmKPpGGOv3oPTpjYPQVqSv22";  // App ID
    protected static final String PARSE_CLIENT_TEST = "kEcwixh6Eiyt2D4FRqdwQ1uL7xH72BE6uLP1rXns2"; // Client ID
    protected static final String PARSE_SERVER_URL_TEST = "https://api.angopapo.com/";  // Parse Server URL
    protected static final String WS_URL_TEST = "ws://162.249.2.42:1338/";  // Live Query Server URL
    protected static final String GCM_TEST = "842306250514";  // Sender ID for Push notifications


    public static final String LOCAL_MESSAGES = "ALL_MESSAGES";


    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(getBaseContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AccountKit.initialize(getApplicationContext());

        //Picaso image improvements

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        // Initialize image loader

        OkHttpClient client = new OkHttpClient.Builder().build();
        ImagePipelineConfig configPip = OkHttpImagePipelineConfigFactory
                .newBuilder(this, client)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, configPip);


        // Register subclasses here
        ParseObject.registerSubclass(User.class);
        ParseObject.registerSubclass(AroundMeMessage.class);
        ParseObject.registerSubclass(AroundMeHotOrNot.class);
        ParseObject.registerSubclass(AroundMeVisitors.class);

        //Stetho.initializeWithDefaults(this);


        Parse.Configuration config = new Parse.Configuration.Builder(this)
                .applicationId(PARSE_APP_ID_TEST)    // Required APP ID
                .clientKey(PARSE_CLIENT_TEST)        // Optional CLIENT ID
                .server(PARSE_SERVER_URL_TEST)       // Required PARSE SERVER URL
                .enableLocalDataStore()         // Required to store your data for offline use.
                //.addNetworkInterceptor(new ParseLogInterceptor())
                //.addNetworkInterceptor(new ParseStethoInterceptor())
                .build();

        Parse.initialize(config);  // Inicialize your server connection

        ParseFacebookUtils.initialize(this); // Initialize facebook

        // Parse Server Live Query

        LiveQueryClient.init(WS_URL_TEST, PARSE_APP_ID_TEST, true);
        LiveQueryClient.connect();


        final ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("GCMSenderId",GCM_TEST);
        parseInstallation.saveInBackground();


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
    }


}
