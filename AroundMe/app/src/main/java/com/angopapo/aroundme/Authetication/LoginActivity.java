package com.angopapo.aroundme.Authetication;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.Aplication.Constants;
import com.angopapo.aroundme.Aplication.DispatchActivity;
import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.parse.AuthenticationCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.facebook.accountkit.AccountKit;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    public static int APP_REQUEST_CODE = 99;

    @Bind(R.id.btn_fb) Button facebookLogin;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.btn_pn) Button _loginButton;
    @Bind(R.id.btn_signup) TextView _SignUpButton;
    @Bind(R.id.btn_phone) Button _loginPhone;
    @Bind(R.id.link_forgot) TextView _forgotLink;

    public static final List<String> mPermissions = new ArrayList<String>() {{
        add("public_profile");
        add("email");

    }};


    User mCurrentUser;

    Profile mFbProfile;
    ParseUser parseUser;

    private RelativeLayout mLoginLayout;

    private Dialog progressDialog;

    private boolean doubleBackToExitPressedOnce;

    String name = null, email = null, usernamee = null, gender = null, genderSelected = null, emailSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mCurrentUser = (User) User.getCurrentUser();

        mLoginLayout = (RelativeLayout) findViewById(R.id.layout_login);

        ButterKnife.bind(this);

        if (mCurrentUser != null) {
            Log.d("myapp:LoginActivity", "currentUser exist");
            Intent mainIntent = new Intent(LoginActivity.this, AroundMeActivity.class);
            startActivity(mainIntent);


        } else {

            AccountKit.logOut();
        }


        //getKeyHash();

        // Delete all offline data
        ParseObject.unpinAllInBackground();

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _loginPhone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               // onLoginPhone(v);
            }
        });

        _SignUpButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(mainIntent);
            }
        });
        _forgotLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the forgot password activity
                Intent intent = new Intent(getApplicationContext(), ForgotActivity.class);
                startActivity(intent);
            }
        });


        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isInternetAvailable()) {

                    showProgressBar(getString(R.string.login_auth2));

                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, mPermissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {

                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");

                            Snackbar.make(mLoginLayout, R.string.fb_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setActionTextColor(Color.WHITE).show();

                            dismissProgressBar();
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            getUserDetailsFromFB();
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            getUserDetailsFromParse();
                        }
                    }
                });

                } else showInternetConnectionLostMessage();

            }

        });

    }

    private void saveNewUser() {

        parseUser = ParseUser.getCurrentUser();

        ParseInstallation.getCurrentInstallation().put("user", parseUser);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        parseUser.setUsername(usernamee.toLowerCase());

        if (emailSelected!=null){
            parseUser.setEmail(email);
        }
        parseUser.put("nickname", name);
        parseUser.put("desc", "yh now i'm around you!");
        parseUser.put("membervip", "novip");
        parseUser.put("user_private", "no");
        parseUser.put("dist", 100);
        parseUser.put("points", 0);
        parseUser.put("isMale", genderSelected);

//        Saving profile photo as a ParseFile
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Bitmap bitmap = BitmapFactory.decodeResource(LoginActivity.this.getResources(), R.drawable.profile_default_photo);

        if (bitmap != null) {
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();
            String thumbName = parseUser.getUsername().replaceAll("\\s+", "");
            final ParseFile parseFile = new ParseFile(thumbName + "_thumb.jpg", data);

            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    parseUser.put("photo", parseFile);
                    parseUser.put("photo_thumb", parseFile);

                    //Finally save all the user details
                    parseUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                            parseUser = ParseUser.getCurrentUser();

                            ParseInstallation.getCurrentInstallation().put("user", parseUser);
                            ParseInstallation.getCurrentInstallation().saveInBackground();

                            Intent mainIntent = new Intent(LoginActivity.this, AroundMeActivity.class);
                           // mainIntent.putExtra(Constants.ARG_AUTH_METHOD, Constants.AUTH_FACEBOOK);
                           // mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                            LoginActivity.this.finish();


                                dismissProgressBar();

                                Toast.makeText(LoginActivity.this, getString(R.string.wecome) + " " + name , Toast.LENGTH_LONG).show();


                        }
                    });

                }
            });
        }

    }




    private void getUserDetailsFromFB() {

        // Suggested by https://disqus.com/by/dominiquecanlas/
        Bundle parameters = new Bundle();
        parameters.putString("fields", "email,name,picture,first_name,last_name,gender");


        new GraphRequest(
                com.facebook.AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                             /* handle the result */
                        try {

                            Log.d("Response", response.getRawResponse());


                            email = response.getJSONObject().getString("email");
                            name = response.getJSONObject().getString("name");
                            usernamee = response.getJSONObject().getString("first_name") + response.getJSONObject().getString("last_name") ;

                            gender = response.getJSONObject().getString("gender");


                            if (email != null)
                            {
                                emailSelected = email;
                            } else {

                                return;
                            }

                            if (gender.equals("male")){

                                 genderSelected = "true";

                            } else {

                                 genderSelected = "false";
                            }

                            JSONObject picture = response.getJSONObject().getJSONObject("picture");
                            JSONObject data = picture.getJSONObject("data");

                            //  Returns a 50x50 profile picture
                            String pictureUrl = data.getString("url");

                            Log.d("Profile pic", "url: " + pictureUrl);

                            new LoginActivity.ProfilePhotoAsync(pictureUrl).execute();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

    }


    private void getUserDetailsFromParse() {

        parseUser = ParseUser.getCurrentUser();

        ParseInstallation.getCurrentInstallation().put("user", parseUser);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        Intent mainIntent = new Intent(LoginActivity.this, AroundMeActivity.class);
        mainIntent.putExtra(Constants.ARG_AUTH_METHOD, Constants.AUTH_FACEBOOK);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        LoginActivity.this.finish();

        dismissProgressBar();


        Toast.makeText(LoginActivity.this, R.string.weback, Toast.LENGTH_LONG).show();

    }

    class ProfilePhotoAsync extends AsyncTask<String, String, String> {
        Bitmap bitmap;
        String url;

        ProfilePhotoAsync(String url) {
            this.url = url;
        }

        @Override
        protected String doInBackground(String... params) {
            // Fetching data from URI and storing in bitmap
            bitmap = DownloadImageBitmap(url);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //mProfileImage.setImageBitmap(bitmap);

            saveNewUser();
        }
    }

    public static Bitmap DownloadImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("IMAGE", "Error getting bitmap", e);
        }
        return bm;
    }


    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        if (isInternetAvailable()) {
        _loginButton.setEnabled(true);


            showProgressBar(getString(R.string.login_auth2));

        // Login credecials by user

        Log.d("OnClick", "SignInStart");
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();


            ParseUser.logInInBackground(email, password, new LogInCallback() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    if (parseUser != null) {
                        Log.d("OnClick", "User");



                        // Start an intent for the dispatch activity
                        Intent intent = new Intent(LoginActivity.this, AroundMeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);

                        ParseInstallation.getCurrentInstallation().put("user", parseUser);
                        ParseInstallation.getCurrentInstallation().saveInBackground();
                        ((User) parseUser).setInstallation(ParseInstallation.getCurrentInstallation());
                        parseUser.saveInBackground();
                        ((User) parseUser).setOnline(true);

                        dismissProgressBar();


                    } else {
                        Log.d("OnClick", e.toString());



                        switch (e.getCode()) {

                            case ParseException.OBJECT_NOT_FOUND:
                                Log.d("Invalid" ,"Login");

                                Snackbar.make(mLoginLayout, R.string.login_inc, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_verify, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;

                            case ParseException.USERNAME_MISSING:
                                Log.d("MyApp", "time out");

                                Snackbar.make(mLoginLayout, R.string.login_incor, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_verify, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;

                            case ParseException.INVALID_EMAIL_ADDRESS:
                                Log.d("MyApp", "time out");

                                Snackbar.make(mLoginLayout, R.string.login_inco_email, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_inc_veri, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;
                            case ParseException.EMAIL_NOT_FOUND:
                                Log.d("MyApp", "time out");

                                Snackbar.make(mLoginLayout, R.string.login_not_fou, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_verify2, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;

                            case ParseException.TIMEOUT:
                                Log.d("MyApp", "time out");

                                Snackbar.make(mLoginLayout, R.string.login_request, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;

                            case ParseException.CONNECTION_FAILED:
                                Log.d("MyApp", "connection failed");

                                Snackbar.make(mLoginLayout, R.string.login_cont_con, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok2, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;
                            case ParseException.INTERNAL_SERVER_ERROR:
                                Log.d("MyApp", "email taken");

                                Snackbar.make(mLoginLayout, R.string.login_intern, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;
                        }

                    }

                }
            });

        }else {

            showInternetConnectionLostMessage();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();


        if(ParseUser.getCurrentUser() != null){
            Log.d("myapp:LoginActivity","currentUser exist");
            Intent mainIntent = new Intent(LoginActivity.this, AroundMeActivity.class);
            startActivity(mainIntent);
            LoginActivity.this.finish();
        }
        Log.d("myapp:LoginActivity", "onResume");

    }

    public void showInternetConnectionLostMessage(){
        Snackbar.make(mLoginLayout, R.string.login_no_int, Snackbar.LENGTH_SHORT).show();

    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);



    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }


    @Override
    public void onBackPressed() {

        if(doubleBackToExitPressedOnce)
        {
            super.onBackPressed();
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.login_press_again, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    @Override
    public void onClick(View v) {

    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), R.string.login_missi, Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;



        String username = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (username.isEmpty() || username.length() < 3) {
            _emailText.setError(getString(R.string.login_email_at));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 ) {
            _passwordText.setError(getString(R.string.login_pass_at));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(LoginActivity.this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( progressDialog!=null && progressDialog.isShowing() ){
            progressDialog.cancel();
        }
    }
}
