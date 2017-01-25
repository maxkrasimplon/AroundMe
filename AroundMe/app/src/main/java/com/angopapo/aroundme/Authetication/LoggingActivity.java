package com.angopapo.aroundme.Authetication;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import com.facebook.accountkit.AccountKit;
import com.angopapo.aroundme.Aplication.DispatchActivity;
import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Timer;
import java.util.TimerTask;

public class LoggingActivity extends Activity {

    public final static String EXTRA_USERNAME_ID = "username2";


    User mCurrentUser;

    ParseUser parseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logging);

        mCurrentUser = (User)User.getCurrentUser();

        parseUser = ParseUser.getCurrentUser();




        final Handler handler = new Handler();

        String username = "";
        if(getIntent() != null){
            username = getIntent().getStringExtra(EXTRA_USERNAME_ID);
        }


        //showProgressBar(getString(R.string.login_auth2));

        final String finalUsername = username;
        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {


                        if (User.getUser() == null) {

                            AccountKit.logOut();
                            ParseUser.logOut();

                            Intent intent = new Intent(LoggingActivity.this, DispatchActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                            // UpdateUserData();
                        } else {

                            //dismissProgressBar();

                            if (User.getUser().isNew()){


                                parseUser = ParseUser.getCurrentUser();

                                //showProgressBar(getString(R.string.login_auth2));

                                ParseInstallation.getCurrentInstallation().put("user", parseUser);
                                ParseInstallation.getCurrentInstallation().saveInBackground();

                                parseUser.setUsername(finalUsername);
                                //parseUser.setEmail(email);
                                parseUser.put("nickname", "Private Profile");
                                parseUser.put("desc", "yh now i'm around you!");
                                parseUser.put("membervip", "novip");
                                parseUser.put("user_private", "no");
                                parseUser.put("dist", 100);
                                parseUser.put("points", 0);
                                //parseUser.put("isMale", genderSelected);

                                //Finally save all the user details
                                parseUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {




                                        Intent intent = new Intent(LoggingActivity.this, AroundMeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                        //dismissProgressBar();

                                        Toast.makeText(LoggingActivity.this,getString(R.string.wecome), Toast.LENGTH_LONG).show();



                                    }
                                });



                            } else {



                                parseUser = ParseUser.getCurrentUser();

                                ParseInstallation.getCurrentInstallation().put("user", parseUser);
                                ParseInstallation.getCurrentInstallation().saveInBackground();

                                Intent intent = new Intent(LoggingActivity.this, AroundMeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                                Toast.makeText(LoggingActivity.this, R.string.weback + " " + User.getUser().getNickname(), Toast.LENGTH_LONG).show();

                            }



                        }

                    }
                });
            }
        };
        Timer timer = new Timer();
        timer.schedule(timertask, 5000);
    }

}
