package com.angopapo.aroundme.Settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Passport.UpdateLocationActivity;
import com.angopapo.aroundme.R;
import com.angopapo.aroundme.VipAccount.VipAccountActivity;


public class MyAccountActivity extends AppCompatActivity implements ActivityWithToolbar, GridView.OnItemClickListener, View.OnClickListener {

    private Toolbar mToolbar;

    LinearLayout mLocationButton, mVipMember, mPrivateModde;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.my_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLocationButton = (LinearLayout)findViewById(R.id.button_vip_features);
        mVipMember = (LinearLayout)findViewById(R.id.button_vip_store);
        mPrivateModde = (LinearLayout)findViewById(R.id.button_private_active);



        mLocationButton.setOnClickListener(this);
        mVipMember.setOnClickListener(this);
        mPrivateModde.setOnClickListener(this);
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
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.button_vip_features:
            {
                Intent termsIntent = new Intent(this, UpdateLocationActivity.class);
                startActivity(termsIntent);
            }
            break;
            case R.id.button_vip_store:
            {
                Intent privacyIntent = new Intent(this, VipAccountActivity.class);
                startActivity(privacyIntent);
            }

            break;

            case R.id.button_private_active:
            {
                Intent privacyIntent = new Intent(this, PrivateModeActivity.class);
                startActivity(privacyIntent);
            }

            break;

        }




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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}