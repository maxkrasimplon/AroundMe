package com.angopapo.aroundme.VipAccount;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.MyVisitores.MyVisitorsActivity;
import com.angopapo.aroundme.Passport.MapsActivity;
import com.angopapo.aroundme.R;
import com.parse.ParseException;
import com.parse.SaveCallback;

import java.util.Calendar;
import java.util.Date;

public class VipActivationActivity extends AppCompatActivity implements ActivityWithToolbar, View.OnClickListener {

    // in-billing start here
    private IInAppBillingService mService;


    Toolbar mToolbar;
    private User mCurrentUser;

    private TextView mDateTravel, mDateVisitor, mDateAds, mDatePrivate ;

    private RelativeLayout mLocationLayout;

    private Dialog progressDialog;
    private Date startDate;
    private Date expiate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_activation);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        
        final Button travel = (Button) findViewById(R.id.button_travel);
        final Button visitor = (Button) findViewById(R.id.button_visitor);
        final Button remove = (Button) findViewById(R.id.button_remove);
        final Button privatemode = (Button) findViewById(R.id.button_private);

        mDateAds = (TextView) findViewById(R.id.date1);
        mDateTravel = (TextView) findViewById(R.id.date2);
        mDateVisitor = (TextView) findViewById(R.id.date3);
        mDatePrivate = (TextView) findViewById(R.id.date4);



        mLocationLayout = (RelativeLayout) findViewById(R.id.layout_location);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.vip_features_titlte);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = (User)User.getCurrentUser();

         if(mCurrentUser.isVip()) {

            
            remove.setText(R.string.VIP);
            travel.setText(R.string.VIP);
            visitor.setText(R.string.VIP);
             privatemode.setText(R.string.VIP);
            
            remove.setEnabled(false);
            travel.setEnabled(false);
            visitor.setEnabled(false);
             privatemode.setEnabled(false);
           
            remove.setBackgroundResource(R.drawable.buy_button_bg_green);
            travel.setBackgroundResource(R.drawable.buy_button_bg_green);
            visitor.setBackgroundResource(R.drawable.buy_button_bg_green);
             privatemode.setBackgroundResource(R.drawable.buy_button_bg_green);
           
        }

        
        

        // set green to all activated services


        if (mCurrentUser.isAds()){

            assert remove != null;
            remove.setText(R.string.vip_activated);
            //promote.setTextColor(Color.GREEN);
            remove.setBackgroundResource(R.drawable.buy_button_bg_green);
            remove.setEnabled(false);

            //String currentDateTimeString = mCurrentUser.getAdsEnd();

            // textView is the TextView view that should display it
            //mDateAds.setText(currentDateTimeString);

            //currentDateTimeString = mCurrentUser.getAdsEnd();

            //assert mDateAds != null;
           // mDateAds.setText(getString(R.string.expirein) + " " + currentDateTimeString);

        }

        if (mCurrentUser.isTravel()){

            //assert travel != null;
            travel.setText(R.string.vip_activated);
            mDateTravel.setText(mCurrentUser.getTravelEnd());
            //promote.setTextColor(Color.GREEN);
            travel.setBackgroundResource(R.drawable.buy_button_bg_green);
            travel.setEnabled(false);

            //assert mDateTravel != null;
           // mDateTravel.setText(getString(R.string.expirein) + " " + mCurrentUser.getTravelEnd().toString());

        }

        if (mCurrentUser.isVisitor()){

            assert visitor != null;
            visitor.setText(R.string.vip_activated);
            //promote.setTextColor(Color.GREEN);
            visitor.setBackgroundResource(R.drawable.buy_button_bg_green);
            visitor.setEnabled(false);

            //assert mDateVisitor != null;
            // mDateVisitor.setText(getString(R.string.expirein) + " " + mCurrentUser.getVisitorEnd());

        }

        if (mCurrentUser.isVisitor()){

            assert visitor != null;
            privatemode.setText(R.string.vip_activated);
            //promote.setTextColor(Color.GREEN);
            privatemode.setBackgroundResource(R.drawable.buy_button_bg_green);
            privatemode.setEnabled(false);

            //assert mDateVisitor != null;
            // mDateVisitor.setText(getString(R.string.expirein) + " " + mCurrentUser.getVisitorEnd());

        }

        assert travel != null;
        travel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if  (mCurrentUser.getCredits() < 100){

                    // ask the user to confirm a deduction and to activate service

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.sorry_vip));
                    notifyLocationServices.setMessage(getString(R.string.cost_vip) + (mCurrentUser.getCredits().toString() + " " + getString(R.string.vip_act_expl)));
                    notifyLocationServices.setPositiveButton(getString(R.string.recg_vip), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // buy the service and deduct 100 Credits here

                            Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                            startActivity(intent);
                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();


                }

                else {

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.conf_1));
                    notifyLocationServices.setMessage(getString(R.string.conf_2));
                    notifyLocationServices.setPositiveButton(getString(R.string.conf_3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // deduct 100 credits in the user account and activated a service for 30 days
                            showProgressBar(getString(R.string.active_vip));

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, 30);
                            Date expDate = calendar.getTime();

                            mCurrentUser.setTravelEnd(expDate);
                            mCurrentUser.increment("points", -100);

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

                                            default:
                                                Log.d("MyApp", e.toString());

                                                //dismissProgressBar();

                                                break;
                                        }
                                    } else {

                                        dismissProgressBar();
                                        showProgressBar(getString(R.string.active_vip));

                                        mCurrentUser.setTravel();
                                        mCurrentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                dismissProgressBar();


                                                Snackbar.make(mLocationLayout, R.string.cong_vip, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent loginIntent = new Intent(VipActivationActivity.this, MapsActivity.class);
                                                        VipActivationActivity.this.startActivity(loginIntent);
                                                        VipActivationActivity.this.finish();

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();

                                                mCurrentUser.fetchInBackground();
                                                //finish();
                                                travel.setText(getString(R.string.vip_activated));
                                                travel.setBackgroundResource(R.drawable.buy_button_bg_green);
                                                travel.setEnabled(false);
                                                mCurrentUser.fetchInBackground();

                                            }
                                        });

                                        // now we have to deduct 100 creduts from current user account
                                        mCurrentUser.fetchInBackground();

                                        dismissProgressBar();

                                    }
                                }
                            });

                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();

                }
            }
        });

        assert privatemode != null;
        privatemode.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if  (mCurrentUser.getCredits() < 100){

                    // ask the user to confirm a deduction and to activate service

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.sorry_vip));
                    notifyLocationServices.setMessage(getString(R.string.cost_vip) + (mCurrentUser.getCredits().toString() + " " + getString(R.string.vip_act_expl)));
                    notifyLocationServices.setPositiveButton(getString(R.string.recg_vip), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // buy the service and deduct 100 Credits here

                            Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                            startActivity(intent);
                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();


                }

                else {

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.conf_1));
                    notifyLocationServices.setMessage(getString(R.string.conf_2));
                    notifyLocationServices.setPositiveButton(getString(R.string.conf_3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // deduct 100 credits in the user account and activated a service for 30 days
                            showProgressBar(getString(R.string.active_vip));

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, 30);
                            Date expDate = calendar.getTime();

                            mCurrentUser.setTravelEnd(expDate);
                            mCurrentUser.increment("points", -100);

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

                                            default:
                                                Log.d("MyApp", e.toString());

                                                //dismissProgressBar();

                                                break;
                                        }
                                    } else {

                                        dismissProgressBar();
                                        showProgressBar(getString(R.string.active_vip));

                                        mCurrentUser.setPrivate();

                                        mCurrentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                dismissProgressBar();


                                                Snackbar.make(mLocationLayout, R.string.cong_vip, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent loginIntent = new Intent(VipActivationActivity.this, MapsActivity.class);
                                                        VipActivationActivity.this.startActivity(loginIntent);
                                                        VipActivationActivity.this.finish();

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();

                                                mCurrentUser.fetchInBackground();
                                                //finish();
                                                privatemode.setText(getString(R.string.vip_activated));
                                                privatemode.setBackgroundResource(R.drawable.buy_button_bg_green);
                                                privatemode.setEnabled(false);
                                                mCurrentUser.fetchInBackground();

                                            }
                                        });

                                        // now we have to deduct 100 creduts from current user account
                                        mCurrentUser.fetchInBackground();

                                        dismissProgressBar();

                                    }
                                }
                            });

                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();

                }
            }
        });

        assert visitor != null;
        visitor.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // change the color to signify a click
                if  (mCurrentUser.getCredits() < 100){

                    // ask the user to confirm a deduction and to activate service

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.sorry_vip));
                    notifyLocationServices.setMessage(getString(R.string.cost_vip) + (mCurrentUser.getCredits().toString() + " " + getString(R.string.vip_act_expl)));
                    notifyLocationServices.setPositiveButton(getString(R.string.recg_vip), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // buy the service and deduct 100 Credits here
                            Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                            startActivity(intent);

                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();


                }

                else {

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.conf_1));
                    notifyLocationServices.setMessage(getString(R.string.conf_2));
                    notifyLocationServices.setPositiveButton(getString(R.string.conf_3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // deduct 100 credits in the user account and activated a service for 30 days
                            showProgressBar(getString(R.string.active_vip));

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, 30);
                            Date expDate = calendar.getTime();

                            mCurrentUser.setVisitorEnd(expDate);
                            mCurrentUser.increment("points", -100);

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

                                            default:
                                                Log.d("MyApp", e.toString());

                                                //dismissProgressBar();

                                                break;
                                        }
                                    } else {

                                        dismissProgressBar();
                                        showProgressBar(getString(R.string.active_vip));

                                        mCurrentUser.setVisitor();
                                        mCurrentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                dismissProgressBar();

                                                Snackbar.make(mLocationLayout, R.string.cong_vip, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent loginIntent = new Intent(VipActivationActivity.this, MyVisitorsActivity.class);
                                                        VipActivationActivity.this.startActivity(loginIntent);
                                                        VipActivationActivity.this.finish();

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();

                                                mCurrentUser.fetchInBackground();
                                                //finish();
                                                visitor.setText(getString(R.string.vip_activated));
                                                visitor.setBackgroundResource(R.drawable.buy_button_bg_green);
                                                visitor.setEnabled(false);
                                                mCurrentUser.fetchInBackground();

                                            }
                                        });

                                        // now we have to deduct 100 creduts from current user account
                                        mCurrentUser.fetchInBackground();

                                        dismissProgressBar();

                                    }
                                }
                            });

                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();

                }
            }
        });
        

        assert remove != null;
        remove.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if  (mCurrentUser.getCredits() < 100){

                    // ask the user to confirm a deduction and to activate service

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.sorry_vip));
                    notifyLocationServices.setMessage(getString(R.string.cost_vip) + (mCurrentUser.getCredits().toString() + " " + getString(R.string.vip_act_expl)));
                    notifyLocationServices.setPositiveButton(getString(R.string.recg_vip), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // buy the service and deduct 100 Credits here

                            Intent intent = new Intent(getApplicationContext(), StoreActivity.class);
                            startActivity(intent);
                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();


                }

                else {

                    AlertDialog.Builder notifyLocationServices = new AlertDialog.Builder(VipActivationActivity.this);
                    notifyLocationServices.setTitle(getString(R.string.conf_1));
                    notifyLocationServices.setMessage(getString(R.string.conf_2));
                    notifyLocationServices.setPositiveButton(getString(R.string.conf_3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // deduct 100 credits in the user account and activated a service for 30 days
                            showProgressBar(getString(R.string.active_vip));

                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.DATE, 30);
                            Date expDate = calendar.getTime();

                            mCurrentUser.setAdsEnd(expDate);

                            mCurrentUser.increment("points", -100);
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

                                            default:
                                                Log.d("MyApp", e.toString());

                                                //dismissProgressBar();

                                                break;
                                        }
                                    } else {

                                        dismissProgressBar();
                                        showProgressBar(getString(R.string.active_vip));

                                        mCurrentUser.setNoAds();
                                        mCurrentUser.setAdsString(false);
                                        mCurrentUser.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                dismissProgressBar();

                                                Snackbar.make(mLocationLayout, R.string.cong_vip, Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent loginIntent = new Intent(VipActivationActivity.this, AroundMeActivity.class);
                                                        VipActivationActivity.this.startActivity(loginIntent);
                                                        VipActivationActivity.this.finish();

                                                    }
                                                }).setActionTextColor(Color.WHITE).show();

                                                mCurrentUser.fetchInBackground();
                                                //finish();
                                                remove.setText(getString(R.string.vip_activated));
                                                remove.setBackgroundResource(R.drawable.buy_button_bg_green);
                                                remove.setEnabled(false);
                                                mCurrentUser.fetchInBackground();

                                            }
                                        });

                                        // now we have to deduct 100 creduts from current user account
                                        mCurrentUser.fetchInBackground();

                                        dismissProgressBar();

                                    }
                                }
                            });

                        }
                    });
                    notifyLocationServices.setNegativeButton(getString(R.string.conf_4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });
                    notifyLocationServices.show();

                }
            }
        });

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
        return 7;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }

    @Override
    public void onClick(View v) {

    }

    public void showProgressBar(String message){
        progressDialog = ProgressDialog.show(this, "", message, true);
    }
    public void dismissProgressBar() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
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
}
