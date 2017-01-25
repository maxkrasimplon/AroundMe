package com.angopapo.aroundme.Aplication;

import android.util.Log;

/**
 * Created by Angopapo, LDA on 25.11.16.
 */
public class Config {
    private static final String LOG_TAG = "myapp";
    private static final Boolean LOG_DEBUG = true;

    public static void log(String message){
        Log.d(LOG_TAG, message);
    }
}
