package com.angopapo.aroundme.Aplication;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.angopapo.aroundme.AroundMe.AroundMeActivity;
import com.angopapo.aroundme.Authetication.LoginActivity;
import com.angopapo.aroundme.ClassHelper.User;


public class DispatchActivity extends Activity {

  User mCurrentUser;

  public DispatchActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mCurrentUser = (User)User.getCurrentUser();


    // Check if there is a valid user session or not

    if (mCurrentUser != null)  {

      // if the current user has valid serrion, then go to Around Me Activity

      Intent mainIntent = new Intent(DispatchActivity.this, AroundMeActivity.class);
      mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(mainIntent);

    } else {

      // if there is no valid user session, go to Login Activity

      Intent mainIntent = new Intent(DispatchActivity.this, LoginActivity.class);
      mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
      startActivity(mainIntent);
    }
  }

}
