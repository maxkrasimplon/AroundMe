package com.angopapo.aroundme.Authetication;

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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.R;
import com.mikepenz.materialdrawer.view.BezelImageView;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.StringTokenizer;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SignUpActivity extends Activity {


    //private static final String TAG = "SignUpActivity";
    public static final String TAG = "myapp";

    private static final int REQUEST_EXTERNAL_STORAGE = 123;
    private static final int REQUEST_CAMERA = 124 ;

    @Bind(R.id.input_name) EditText _nameText;
    @Bind(R.id.input_username) EditText _usernameText;
    @Bind(R.id.input_email) EditText _emailText;
    @Bind(R.id.input_password) EditText _passwordText;
    @Bind(R.id.button_change) Button _changePicture;
    @Bind(R.id.btn_signup) Button _signupButton;
    @Bind(R.id.link_login) TextView _loginLink;
    @Bind(R.id.profilePhoto) BezelImageView mImageView;
    @Bind(R.id.genderGroup) RadioGroup mGenderLayout;

    private ScrollView mSignUpActivity;


    Intent mLoginIntent;

    private Dialog progressDialog;

    public Context context;

    public String provider;

    private Uri picUri;
    private Uri picUri2;

    private TextView _TextAccept;

    protected LocationListener locationListener;

    private LocationManager locationManager;


    protected String gender = null;

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_male:
                if (checked)
                    gender = User.GENDER_MALE;

                break;
            case R.id.radio_femele:
                if (checked)
                    gender = User.GENDER_FEMALE;
                break;
        }
    }


    protected void showMessage(String message) {
        Snackbar.make(mSignUpActivity, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);

        mLoginIntent = this.getIntent();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
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

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                ChangePhoto();
            }
        });

        mSignUpActivity = (ScrollView) findViewById(R.id.layout_signup);

        //Initialize ImageView
        //mImageView = (BezelImageView) findViewById(R.id.profilePhoto);



        _changePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ChangePhoto();
            }
        });


        mLoginIntent = this.getIntent();


        TextView textView =(TextView)findViewById(R.id.text_accept);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = String.format(getString(R.string.signup_accept) + getString(R.string.terms) + getString(R.string.and) + getString(R.string.privacy));
        textView.setText(Html.fromHtml(text));

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


    public void ChangePhoto (){

        final CharSequence[] items = {"Camera", "Gallery"};

        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SignUpActivity.this);

        builder.setTitle("Profile Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals("Camera")) {

                    if (Build.VERSION.SDK_INT >= 23) {


                        PermissionRequestCamera();



                    } else {


                        Camera();
                    }



                } else if (items[item].equals("Gallery")) {

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


    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        if (isInternetAvailable()) {
            _signupButton.setEnabled(true);

            showProgressBar(getString(R.string.signup_creat));


            // Locate the image in res > drawable-hdpi
            Bitmap bitmap = ((BitmapDrawable)mImageView.getDrawable()).getBitmap();
            // Convert it to byte
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Compress image to lower quality scale 1 - 100
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();

            // Create the ParseFile
            final ParseFile file = new ParseFile("profile.png", image);
            // Upload the image into Parse Cloud

                        file.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {


                                        User newUser = new User();
                                        newUser.setEmail(_emailText.getText().toString());
                                        newUser.setNickname(_nameText.getText().toString());
                                        newUser.setUsername(_usernameText.getText().toString());
                                        newUser.setPassword(_passwordText.getText().toString());
                                        newUser.setInstallation(ParseInstallation.getCurrentInstallation());
                                        newUser.setNoVip();
                                        newUser.setDescription(getString(R.string.signup_yh));
                                        newUser.setDist(100);
                                        newUser.setNoPrivate();
                                        newUser.put(User.COL_IS_MALE, gender);
                                        newUser.setProfilePhotoThumb(file);
                                        newUser.setProfilePhoto(file);
                                        newUser.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e != null) {

                                                    dismissProgressBar();

                                                    // new values add here //

                                                    StringTokenizer tokens = new StringTokenizer(e.toString(), ":");

                                                    String second = tokens.nextToken();// this will contain " they taste good"

                                                    // new values ends here //

                                                    switch (e.getCode()) {

                                                        case ParseException.TIMEOUT:
                                                            Log.d("MyApp", "time out");

                                                            Snackbar.make(mSignUpActivity, R.string.signup_request_t, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).setActionTextColor(Color.WHITE).show();

                                                            dismissProgressBar();

                                                            break;

                                                        case ParseException.CONNECTION_FAILED:
                                                            Log.d("MyApp", "connection failed");

                                                            Snackbar.make(mSignUpActivity, R.string.login_cant, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).setActionTextColor(Color.WHITE).show();

                                                            dismissProgressBar();

                                                            break;
                                                        case ParseException.INTERNAL_SERVER_ERROR:
                                                            Log.d("MyApp", "internal server error");

                                                            Snackbar.make(mSignUpActivity, R.string.login_error, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).setActionTextColor(Color.WHITE).show();

                                                            dismissProgressBar();

                                                            break;
                                                        case ParseException.EMAIL_TAKEN:
                                                            Log.d("MyApp", "email taken");

                                                            Snackbar.make(mSignUpActivity, R.string.login_teken, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).setActionTextColor(Color.WHITE).show();

                                                            dismissProgressBar();

                                                            break;
                                                        case ParseException.USERNAME_TAKEN:
                                                            Log.d("MyApp", "username taken");

                                                            Snackbar.make(mSignUpActivity, R.string.signup_userta, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).setActionTextColor(Color.rgb(255,255,168)).show();

                                                            dismissProgressBar();

                                                            break;

                                                        case ParseException.OTHER_CAUSE:
                                                            Log.d("MyApp", "time out");

                                                            Snackbar.make(mSignUpActivity, R.string.signup_request, Snackbar.LENGTH_INDEFINITE).setAction(R.string.login_ok, new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {

                                                                }
                                                            }).setActionTextColor(Color.WHITE).show();



                                                            break;

                                                    }

                                                    }else{

                                                    dismissProgressBar();

                                                    Intent loginIntent = new Intent(SignUpActivity.this, SaveLocationActivity.class);
                                                    SignUpActivity.this.startActivity(loginIntent);
                                                    SignUpActivity.this.finish();

                                                    }
                                                }
                                            }

                                            );
                                        }
                                    }
                                }

                                );

                    }else{

            showInternetConnectionLostMessage();
        }
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.login_missi, Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String username = _usernameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();


        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError(getString(R.string.signup_name_at));
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (username.isEmpty() || username.length() < 3) {
            _usernameText.setError(getString(R.string.signup_user_at));
            valid = false;
        } else {
            _usernameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getString(R.string.signup_valid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 6 ) {
            _passwordText.setError(getString(R.string.signup_pass_atl));

            valid = false;
        } else {
            _passwordText.setError(null);

        }


        if (gender == null) {

            valid = false;


            new android.support.v7.app.AlertDialog.Builder(SignUpActivity.this)
                    .setTitle(getString(R.string.signup_gender))
                    .setIcon(android.R.drawable.stat_notify_error)
                    .setMessage(getString(R.string.signup_gender_explzin))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.sorry_vip_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Whatever...
                        }
                    }).create().show();

        }
        //else
        //{
        //    mGenderLayout.setError(null);
       // }

        return valid;
    }

    public void showInternetConnectionLostMessage() {

        Snackbar.make(mSignUpActivity, R.string.login_no_int, Snackbar.LENGTH_SHORT).show();
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
    public void onBackPressed() {

        super.onBackPressed();
        // Do Here what ever you want do on back press;
    }

    //previewing Image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
            try {
                cropCapturedImage(Uri.fromFile(file));
            }
            catch(ActivityNotFoundException aNFE){
                String errorMessage = "Sorry - your device doesn't support the crop action!";
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
                final Bitmap thePic = extras.getParcelable("data");
                mImageView.setImageBitmap(thePic);
            }

        }

        if(requestCode==4){

            Bundle extras = data.getExtras();
            if (extras != null){
                final Bitmap thePic = extras.getParcelable("data");
                mImageView.setImageBitmap(thePic);
            }

        }
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

    public void PermissionRequestStorage(){

        if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                new AlertDialog.Builder(SignUpActivity.this)
                        .setTitle("Permission Needed")
                        .setMessage("Storage permission is needed to access your gallery to update your profile picture")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            }
        } else {

            Gallery();
        }
    }

    public void PermissionRequestCamera(){
        if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(SignUpActivity.this,
                    Manifest.permission.CAMERA)) {

                new AlertDialog.Builder(SignUpActivity.this)
                        .setTitle("Permission Needed")
                        .setMessage("Camera permission is needed to take picture to update profile picture")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(SignUpActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);

            }
        } else{

            Camera();
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

                    Context context = SignUpActivity.this;
                    CharSequence text = "You denied the storage permission, We Disabled the function. Grant the permission to use this function !";
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

                    Context context = SignUpActivity.this;
                    CharSequence text = "You denied the camera permission, We Disabled the function. Grant the permission to use this function !";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();

                }
                return;

            }

        }
    }
}
