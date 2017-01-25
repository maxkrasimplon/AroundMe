package com.angopapo.aroundme.Passport;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.LocationHelper.models.GeocoderResult;
import com.angopapo.aroundme.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements ActivityWithToolbar, OnMapReadyCallback, SearchView.OnQueryTextListener, SearchAddressFragment.SearchAddressListener, View.OnClickListener {

    private static final String TAG = "myapp:MapsActivity";

    public static final int REQUEST_CODE_LOCATION = 1001;
    private static final int REQUEST_EXTERNAL_LOCATION = 12386;

    private Toolbar mToolbar;
    private MenuItem mSearchMenuItem;
    private SearchView mSearchView;
    private SearchAddressFragment mSearchAddressFragment;
    private User mCurrentUser;
    private Drawer drawer;

    private Button mSaveLocationButton;

    protected void log(String message){
        Log.d(TAG, message);
    }

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker mMarker;
    private Circle mCircle;
    private GeocoderResult mGeocoderResult;
    private ParseGeoPoint mParseGeoPoint;

    private Dialog progressDialog;

    RelativeLayout mSignUpActivity;

    private RelativeLayout mSettingsLayout;

    // private User mCurrentUser;

    protected void showMessage(String message) {
        Snackbar.make(mSignUpActivity, message, Snackbar.LENGTH_SHORT).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.drawer_item_passport);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer = NavigationDrawer.createDrawer(this);

        mSaveLocationButton = (Button) findViewById(R.id.button_save_location);
        mSaveLocationButton.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= 23) {


            // Marshmallow+
            PermissionRequestLocation();


        } else {
            // Pre-Marshmallow

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


        }


        mCurrentUser = (User)User.getCurrentUser();

        mSettingsLayout = (RelativeLayout) findViewById(R.id.layout_settings);

        requestOnlineUser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_maps, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView)mSearchMenuItem.getActionView();
        mSearchView.setOnQueryTextListener(this);
        return true;
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void searchLocation(String addressString){
        if(mSearchAddressFragment == null){
            mSearchAddressFragment = new SearchAddressFragment();
            Bundle args = new Bundle();
            args.putString(SearchAddressFragment.ARG_ADDRESS, addressString);
            mSearchAddressFragment.setArguments(args);
            mSearchAddressFragment.setSearchAddressListener(this);
            getSupportFragmentManager().beginTransaction().add(R.id.container, mSearchAddressFragment).commit();
        } else if (mSearchAddressFragment != null && !mSearchAddressFragment.isVisible()){
            getSupportFragmentManager().beginTransaction().add(R.id.container, mSearchAddressFragment).commit();
            mSearchAddressFragment.getAddresses(addressString);
        } else {
            mSearchAddressFragment.getAddresses(addressString);
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */

    @Override
    public void onMapReady(GoogleMap map) {
        // Add a marker in Sydney, Australia, and move the camera.

        Log.d("Startmap","Mapping started");

        mMap = map;
        if(/*mCurrentUser != null &&*/ mCurrentUser.isVip()) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location loc) {
                    if(mMarker != null) mMarker.remove();
                    mParseGeoPoint = new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
                  //  LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());
                  //  mMarker = mMap.addMarker(new MarkerOptions().position(location).title("My location"));
                  //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
                  //  Log.d("Startmap","Mapping started2");

                    LatLng location = new LatLng(mCurrentUser.getGeoPoint().getLatitude(), mCurrentUser.getGeoPoint().getLongitude());
                    mMarker = mMap.addMarker(new MarkerOptions().position(location).title("My location"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
                    Log.d("Startmap","Mapping started2");
                    mMap.setMyLocationEnabled(false);
                    Log.d("Startmap","Mapping StoPPed2");


                }
            });
        }
        LatLng location = null;
        if(mCurrentUser != null && mCurrentUser.getGeoPoint() != null){
            location  =  new LatLng(mCurrentUser.getGeoPoint().getLatitude(), mCurrentUser.getGeoPoint().getLongitude());
            mMarker = map.addMarker(new MarkerOptions().position(location).title("My location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
        } else{
            location = new LatLng(-34, 151);
            mMarker = map.addMarker(new MarkerOptions().position(location).title("Sydney"));
            map.moveCamera(CameraUpdateFactory.newLatLng(location));
        }
    }

    @Override
    public void onBackPressed() {
        if(mSearchAddressFragment != null && mSearchAddressFragment.isVisible()){
            getSupportFragmentManager().beginTransaction().remove(mSearchAddressFragment);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        log(String.format("start search - %s", query));
        getCurrentFocus().clearFocus();
        searchLocation(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void AddressSelected(GeocoderResult geocoderResult) {
        if(mMap != null) {
            mGeocoderResult = geocoderResult;
            LatLng location = geocoderResult.getLocation();
            mParseGeoPoint = new ParseGeoPoint(location.latitude, location.longitude);
            if(mMarker != null) mMarker.remove();
            if(mCircle != null) mCircle.remove();
            mMarker = mMap.addMarker(new MarkerOptions().position(location).title(geocoderResult.getFormattedAddress()));
          //  mCircle = mMap.addCircle(new CircleOptions().center(location).radius(100).strokeColor(Color.RED).fillColor(getColor()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));

            mMap.setMyLocationEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_save_location:

                if (isInternetAvailable()) {

                    showProgressBar(getString(R.string.maps_save2));

                    if (mParseGeoPoint != null) {
                        mCurrentUser.setGeoPoint(mParseGeoPoint);
                        mCurrentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    switch (e.getCode()) {
                                        case ParseException.CONNECTION_FAILED:
                                            Log.d("MyApp", "connection failed");

                                            dismissProgressBar();

                                            Snackbar.make(mSettingsLayout, R.string.settings_cantconnect, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;
                                        case ParseException.INTERNAL_SERVER_ERROR:
                                            Log.d("MyApp", "internal server error");

                                            dismissProgressBar();

                                            Snackbar.make(mSettingsLayout, R.string.settings_internal, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            }).setActionTextColor(Color.WHITE).show();


                                            break;

                                        default:
                                            Log.d("MyApp", e.toString());

                                            //dismissProgressBar();

                                            break;
                                    }

                                } else {

                                    dismissProgressBar();

                                    mCurrentUser.fetchInBackground();

                                    Intent loginIntent = new Intent(MapsActivity.this, AroundMeActivity.class);
                                    MapsActivity.this.startActivity(loginIntent);
                                    MapsActivity.this.finish();

                                }

                            }
                        });
                    }else {


                    }
                }
                else {

                    showInternetConnectionLostMessage();
                }
        }
    }
    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
    public void showInternetConnectionLostMessage(){
        Snackbar.make(mSettingsLayout, R.string.settings_no_inte, Snackbar.LENGTH_SHORT).show();

    }

    public boolean isInternetAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
        return 5;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

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

    public void PermissionRequestLocation(){

        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new android.app.AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Permission Needed")
                        .setMessage("Storage location is needed to access your gallery to update your profile picture")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_EXTERNAL_LOCATION);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_EXTERNAL_LOCATION);
            }
        } else {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);


                } else {

                    Context context = MapsActivity.this;
                    CharSequence text = "You denied the location permission, We Disabled the function. Grant the permission to use this function !";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return;
            }

        }
    }
}
