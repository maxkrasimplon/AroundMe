package com.angopapo.aroundme.Passport;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.angopapo.aroundme.Aplication.DispatchActivity;
import com.angopapo.aroundme.Authetication.SaveLocationActivity;
import com.facebook.accountkit.AccountKit;
import com.google.android.gms.common.api.GoogleApiClient;
import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.LocationHelper.AroundMeLocationService;
import com.angopapo.aroundme.LocationHelper.LocationAddress;
import com.angopapo.aroundme.R;
import com.angopapo.aroundme.Settings.PrivacyActivity;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.OnClick;

public class UpdateLocationActivity extends AppCompatActivity implements ActivityWithToolbar, View.OnClickListener {

    public static final int REQUEST_CODE_LOCATION = 10;
    private static final int REQUEST_EXTERNAL_LOCATION = 12;

    AroundMeLocationService aroundMeLocationService;

    OnClick onClickListener;
    private TextView mLocationText;
    private ProgressBar mLocationProgress;
    private Location mCurrentLocation;
    private Location mLastLocation;
    private TextView mLastLocationText;
    public Context context;

    public String provider;

    private LocationManager locationManager;

    RelativeLayout mSignUpActivity;

    private User mCurrentUser;

    private boolean mTurnOnLocationIsShow = false;
    Button mTravelButton, mRefreshButton;


    private LocationManager mLocationManager;

    private GoogleApiClient mGoogleApiClient;


    private RelativeLayout mLocationLayout;

    private Dialog progressDialog;


    Toolbar mToolbar;

    protected void showMessage(String message) {
        Snackbar.make(mSignUpActivity, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_location);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mTravelButton = (Button) findViewById(R.id.button_travel);
        mRefreshButton = (Button) findViewById(R.id.button2);
        mRefreshButton = (Button) findViewById(R.id.button2);

        mTravelButton.setOnClickListener(this);
        mLocationLayout = (RelativeLayout) findViewById(R.id.layout_location);

        mLastLocationText = (TextView) findViewById(R.id.text_last_location);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.update_location);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                checkGPS();


            }
        });


        // reverse geocoding

        aroundMeLocationService = new AroundMeLocationService(UpdateLocationActivity.this);



        mLocationProgress = (ProgressBar) findViewById(R.id.progress_location);
        mLocationText = (TextView) findViewById(R.id.text_current_location);

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        provider = locationManager.getBestProvider(criteria, false);

        // Verify and request an permittions

        if (Build.VERSION.SDK_INT >= 23) {


            // Marshmallow+
            PermissionRequestLocation();


        } else {
            // Pre-Marshmallow


            startProvider();

           // mCurrentLocation = locationManager.getLastKnownLocation(provider);
           // Location location = aroundMeLocationService.getLocation(LocationManager.GPS_PROVIDER);


        }

        onLocationChanged(getLastKnownLocation());


        mCurrentUser = (User) User.getCurrentUser();


        TextView textView = (TextView)findViewById(R.id.privacyButton);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = String.format(getString(R.string.more_info) + getString(R.string.terms) + getString(R.string.and) + getString(R.string.privacy));
        textView.setText(Html.fromHtml(text));



    }

    public void checkGPS(){


        LocationManager lm = (LocationManager)UpdateLocationActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(UpdateLocationActivity.this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Enable Now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    UpdateLocationActivity.this.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Don't ", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();

        } else if(gps_enabled && network_enabled) {

            onRestart();
        }

    }


    private void startProvider() {
        if (provider != null) {

            mCurrentLocation = locationManager.getLastKnownLocation(provider);
            Location location = aroundMeLocationService.getLocation(LocationManager.GPS_PROVIDER);
        } else {

            Location location = aroundMeLocationService.getLocation(LocationManager.GPS_PROVIDER);
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        if (location != null) {

            mLocationText.setText(String.format("%.4f : %.4f", location.getLongitude(), location.getLatitude()));
            mLocationProgress.setVisibility(View.GONE);

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            LocationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
        } else {

            mLocationText.setText(R.string.lost_2);
            mLocationProgress.setVisibility(View.GONE);

        }


    }

    protected boolean isGpsEnable(){
        if(mLocationManager != null){
            return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        return false;
    }

    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        List<String> providers = locationManager.getProviders(false);
        Location bestLocation = null;
        for (String provider : providers) {


            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                Toast.makeText(this, "Location  " + provider,
                        Toast.LENGTH_SHORT).show();
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @Override
    public void onClick(View v) {

        if (isInternetAvailable()) {
            switch (v.getId()) {

                case R.id.privacyButton:

                    Intent loginIntent = new Intent(UpdateLocationActivity.this, PrivacyActivity.class);
                    UpdateLocationActivity.this.startActivity(loginIntent);
                    //LocationFound.this.finish();

                    break;

                case R.id.button_travel:


                    if (mCurrentLocation != null) {
                        showProgressBar(getString(R.string.loc_updati));
                        mCurrentUser.setGeoPoint(mCurrentLocation);

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

                                        case ParseException.OTHER_CAUSE:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        case ParseException.OBJECT_NOT_FOUND:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        case ParseException.INVALID_QUERY:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        case ParseException.INVALID_CLASS_NAME:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();

                                        case ParseException.MISSING_OBJECT_ID:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        case ParseException.TIMEOUT:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.loc_intererror, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        case ParseException.SESSION_MISSING:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mLocationLayout, R.string.seesion_missing, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {


                                                    // logout current User
                                                    ParseObject.unpinAllInBackground();

                                                    ParseUser.logOut();

                                                    // dismiss a progress bar
                                                    dismissProgressBar();
                                                    AccountKit.logOut();

                                                    Intent intent = new Intent(UpdateLocationActivity.this, DispatchActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    UpdateLocationActivity.this.finish();




                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        default:
                                            Log.d("MyApp", e.toString());

                                            dismissProgressBar();

                                            break;
                                    }
                                } else {

                                    dismissProgressBar();
                                    Intent imageViewerIntent = new Intent(getApplicationContext(), AroundMeActivity.class);
                                    startActivity(imageViewerIntent);

                                }
                            }
                        });

                    } else {
                        AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(UpdateLocationActivity.this);
                        notifyLocationServices.setTitle(getString(R.string.location_switch));
                        notifyLocationServices.setMessage(getString(R.string.location_explain));
                        notifyLocationServices.setPositiveButton(getString(R.string.location_on), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent openLocationSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                UpdateLocationActivity.this.startActivity(openLocationSettings);
                                //LocationFound.this.finish();
                                //finish();
                            }
                        });
                        notifyLocationServices.setNegativeButton(getString(R.string.location_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               // finish();
                            }
                        });
                        notifyLocationServices.show();
                    }

                    break;
                case R.id.button_turn_on_location:

                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_CODE_LOCATION);
                    UpdateLocationActivity.this.finish();
                    finish();

                    break;
            }
        } else {
            showInternetConnectionLostMessage();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public Toolbar getToolbar() {
        return null;
    }

    @Override
    public Activity getActivity() {
        return null;
    }

    @Override
    public int getDriwerId() {
        return 0;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }

    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
    public void showInternetConnectionLostMessage(){
        Snackbar.make(mLocationLayout, R.string.loc_no_int, Snackbar.LENGTH_SHORT).show();

    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            mLastLocationText.setText(locationAddress);
        }
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    public void PermissionRequestLocation(){

        if (ContextCompat.checkSelfPermission(UpdateLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(UpdateLocationActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new android.app.AlertDialog.Builder(UpdateLocationActivity.this)
                        .setTitle("Permission Needed")
                        .setMessage("Storage location is needed to access your gallery to update your profile picture")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(UpdateLocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_EXTERNAL_LOCATION);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(UpdateLocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_EXTERNAL_LOCATION);
            }
        } else {

            startProvider();


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startProvider();



                } else {

                    Context context = UpdateLocationActivity.this;
                    CharSequence text = "You denied the location permission, We Disabled the function. Grant the permission to use this function !";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return;
            }

        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();

        if ( progressDialog!=null && progressDialog.isShowing() ){
            progressDialog.cancel();
        }

    }
}