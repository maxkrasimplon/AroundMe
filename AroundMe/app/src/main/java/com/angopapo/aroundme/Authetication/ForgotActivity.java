package com.angopapo.aroundme.Authetication;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.angopapo.aroundme.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

import butterknife.Bind;
import butterknife.ButterKnife;

//import android.net.ParseException;

public class ForgotActivity extends Activity {
    private static final String TAG = "ForgotActivity";

    private RelativeLayout mLoginLayout;

    private Dialog progressDialog;

    @Bind(R.id.editText) EditText _emailText;
    @Bind(R.id.resetButton) Button _resetButton;
    @Bind(R.id.link_login) TextView _loginLink;
    final Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        ButterKnife.bind(this);

        mLoginLayout = (RelativeLayout) findViewById(R.id.layout_forgot);

        _resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                signup();

            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });


       // myMail = (TextView)findViewById(R.id.editText);



    }


    public void showInternetConnectionLostMessage() {

        //Toast.makeText(getBaseContext(), "No internet connection. please connect", Toast.LENGTH_LONG).show();
        Snackbar.make(mLoginLayout, R.string.login_no_int, Snackbar.LENGTH_SHORT).show();
    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }
        if (isInternetAvailable()) {
            _resetButton.setEnabled(true);

        /*final ProgressDialog progressDialog = new ProgressDialog(ForgotActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Verifying your email...");
        progressDialog.show();*/
            showProgressBar(getString(R.string.forgot_veri));

            ParseUser.requestPasswordResetInBackground(_emailText.getText().toString(), new RequestPasswordResetCallback() {
                public void done(ParseException e) {
                    if (e == null) {


                        // An email was successfully sent with reset instructions.
                        dismissProgressBar();
                        // Dismiss progress bar
                        //progressDialog.dismiss();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                context);
                        alertDialogBuilder.setTitle(R.string.forgot_send);
                        alertDialogBuilder
                                .setMessage(R.string.forgot_check)
                                .setCancelable(false)
                                .setPositiveButton(R.string.forgot_login, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, close
                                        // current activity
                                        // ForgotActivity.this.finish();
                                        //Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
                                        dismissProgressBar();
                                        //startActivity(intent);

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        ForgotActivity.this.finish();
                                    }
                                })
                                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                        dismissProgressBar();
                                        // if this button is clicked, just close
                                        // the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                });

                        // create alert dialog
                        AlertDialog alertDialog = alertDialogBuilder.create();

                        // show it
                        alertDialog.show();


                    } else {

                        switch (e.getCode()) {

                            case ParseException.TIMEOUT:
                                Log.d("MyApp", "time out");

                                Snackbar.make(mLoginLayout, R.string.signup_request_t, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                }).setActionTextColor(Color.WHITE).show();

                                dismissProgressBar();

                                break;

                            case ParseException.CONNECTION_FAILED:
                                Log.d("MyApp", "connection failed");

                                Snackbar.make(mLoginLayout, R.string.login_cant, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
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
                            case ParseException.EMAIL_NOT_FOUND:
                                Log.d("MyApp", "email not found");

                                dismissProgressBar();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        context);
                                alertDialogBuilder.setTitle(R.string.forgot_em_not);
                                alertDialogBuilder
                                        .setMessage(R.string.forgot_email_wasnt)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.forgot_register, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                // if this button is clicked, close
                                                // current activity
                                                // LoginActivity.this.finish();
                                                dismissProgressBar();

                                                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                                                startActivity(intent);
                                                ForgotActivity.this.finish();
                                            }
                                        })
                                        .setNegativeButton(R.string.login_verify, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {

                                                dismissProgressBar();
                                                // if this button is clicked, just close
                                                // the dialog box and do nothing
                                                dialog.cancel();
                                            }
                                        });

                                // create alert dialog
                                AlertDialog alertDialog = alertDialogBuilder.create();

                                // show it
                                alertDialog.show();

                                break;
                        }
                            // Something went wrong. Look at the ParseException to see what's up.

                            // Dismiss Progress bar
                            //progressDialog.dismiss();




                        }
                    }
                }

                );

            }else{

            showInternetConnectionLostMessage();
        }

    }


    public void onSignupSuccess() {
        _resetButton.setEnabled(true);
        setResult(RESULT_OK, null);
        finish();
    }

    public void onSignupFailed() {
        //Toast.makeText(getBaseContext(), "Enter your email address", Toast.LENGTH_LONG).show();

        _resetButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;


        String email = _emailText.getText().toString();


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.forgot_valid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }


        return valid;
    }
    @Override
    public void onBackPressed() {

        super.onBackPressed();
        // Do Here what ever you want do on back press;
    }
    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
