package com.angopapo.aroundme.MyVisitores;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.angopapo.aroundme.Aplication.ActivityWithToolbar;
import com.angopapo.aroundme.Aplication.NavigationDrawer;
import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.ClassHelper.AroundMeVisitors;
import com.angopapo.aroundme.ClassHelper.User;
import com.angopapo.aroundme.InternetHelper.WaitForInternetConnectionView;
import com.angopapo.aroundme.Profile.ProfileUerActivity;
import com.angopapo.aroundme.R;
import com.mikepenz.materialdrawer.Drawer;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyVisitorsActivity extends AppCompatActivity implements ActivityWithToolbar, GridView.OnItemClickListener {

    private Toolbar mToolbar;
    private Drawer drawer;
    private LinearLayout mNoUsersFound;
    private LinearLayout mLoadingMessages;
    private Button mRetryButton;
    private LinearLayout mInternet;
    private WaitForInternetConnectionView mWaitForInternetConnectionView;

    //Layout views
    private GridView mWhoSeeUsersGrid;


    //Current user
    private User mCurrentUser;

    //Users who has seeing current user
    private List<User> mWhoSeeUsers;
    private MyVisitorsAdapter mMyVisitorsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitors);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mWaitForInternetConnectionView = (WaitForInternetConnectionView) findViewById(R.id.wait_for_internet_connection);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.drawer_item_visitor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        drawer = NavigationDrawer.createDrawer(this);

        mCurrentUser = (User)User.getCurrentUser();

        requestOnlineUser();

        mInternet = (LinearLayout)findViewById(R.id.linearLayout22);

        mNoUsersFound = (LinearLayout) findViewById(R.id.noUsersFound);
        mLoadingMessages = (LinearLayout)findViewById(R.id.layout_loading_messages);
        mRetryButton = (Button) findViewById(R.id.button_retry);

        mRetryButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent loginIntent = new Intent(MyVisitorsActivity.this, AroundMeActivity.class);
                MyVisitorsActivity.this.startActivity(loginIntent);
                MyVisitorsActivity.this.finish();
            }
        });

        //Initial layout views
        mWhoSeeUsersGrid = (GridView) findViewById(R.id.grid_who_see_users);

        //mWaitForInternetConnectionView.checkInternetConnection(new WaitForInternetConnectionView.OnConnectionIsAvailableListener() {
          //  @Override
           // public void onConnectionIsAvailable() {

                 showLodingMessaes();
                AroundMeVisitors.getWhoSeeList(mCurrentUser, new AroundMeVisitors.GetWhoSeeUsersCallback() {
                    @Override
                    public void done(List<AroundMeVisitors> whoSeeUsers) {
                        if (whoSeeUsers != null) {
                            final List<String> whoSeeUsersIdArray = new ArrayList<String>();
                            final HashMap<String, String> lastSeeingDateHashMap = new HashMap<String, String>();
                            for (AroundMeVisitors whoSeeUser : whoSeeUsers) {
                                if (!whoSeeUsersIdArray.contains(whoSeeUser.getWhoSeeUserId())) {
                                    lastSeeingDateHashMap.put(whoSeeUser.getWhoSeeUserId(), whoSeeUser.getDateSting());
                                    whoSeeUsersIdArray.add(whoSeeUser.getWhoSeeUserId());
                                }
                            }
                            Log.d("myapp", String.format("usersIdArraySize: %d, hashMapSize: %d", whoSeeUsersIdArray.size(), lastSeeingDateHashMap.size()));
                            ParseQuery<User> getWhoSeeUsersQuery = User.getUserQuery();
                            getWhoSeeUsersQuery.whereNotEqualTo(User.COL_PRIVATE_ACTIVE, "true");
                            getWhoSeeUsersQuery.whereContainedIn(User.COL_ID, whoSeeUsersIdArray);
                            getWhoSeeUsersQuery.fromLocalDatastore();
                            getWhoSeeUsersQuery.findInBackground(new FindCallback<User>() {
                                @Override
                                public void done(List<User> whoSeeUsers, ParseException e) {
                                    if (whoSeeUsers != null) {

                                        mWhoSeeUsers = whoSeeUsers;
                                        if (mWhoSeeUsers.size() > 0) {


                                            ParseObject.pinAllInBackground((List<User>) whoSeeUsers,
                                                    new SaveCallback() {
                                                        public void done(ParseException e) {
                                                            if (e == null) {
                                                                // if (!isFinishing()) {

                                                                mMyVisitorsAdapter.notifyDataSetChanged();
                                                                //  }
                                                            } else {
                                                                Log.i("TodoListActivity",
                                                                        "Error pinning todos: "
                                                                                + e.getMessage());
                                                            }
                                                        }
                                                    });


                                            hideUserNotFound();
                                            hideLodingMessaes();

                                            mWhoSeeUsers = whoSeeUsers;
                                            Log.d("myapp", String.format("usersArraySize: %d", whoSeeUsers.size()));
                                            mMyVisitorsAdapter = new MyVisitorsAdapter(MyVisitorsActivity.this, whoSeeUsers, lastSeeingDateHashMap);
                                            mWhoSeeUsersGrid.setAdapter(mMyVisitorsAdapter);
                                            mWhoSeeUsersGrid.setOnItemClickListener(MyVisitorsActivity.this);

                                        } else {

                                            hideLodingMessaes();

                                            showUserNotFound();
                                        }
                                    } else if (e != null) {

                                        hideLodingMessaes();

                                        Log.d("myapp:findUsers", e.toString());
                                        showUserNotFound();
                                    }
                                }
                            });
                        }
                       // mWaitForInternetConnectionView.close();
                    }
                });

          //  }
       // });
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

    public void showUserNotFound() {
        mNoUsersFound.setVisibility(View.VISIBLE);
    }

    public void hideUserNotFound() {
        mNoUsersFound.setVisibility(View.GONE);
    }

    public void showLodingMessaes(){
        mLoadingMessages.setVisibility(View.VISIBLE);
    }

    public void hideLodingMessaes(){
        mLoadingMessages.setVisibility(View.GONE);
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
        return 4;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void onSizeChanged(int height) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent profileIntent = new Intent(this, ProfileUerActivity.class);
        profileIntent.putExtra(ProfileUerActivity.EXTRA_USER_ID, mMyVisitorsAdapter.getItem(position).getObjectId());
        startActivity(profileIntent);
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
}
