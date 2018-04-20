package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.wwwebteam.clients.tfare.utils.Definitions;

/**
 * Created by wwweb on 26/03/2017.
 */

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check for conditions here and move on using smooth transition.
        SharedPreferences prefs = this.getSharedPreferences(Definitions.PREFERENCE_FILE, MODE_PRIVATE);
        boolean isFirstTime = prefs.getBoolean(Definitions.ISFIRSTTIME, true);
        boolean isRegistered = prefs.getBoolean(Definitions.ISREGISTERED, false);
        Intent mainActivity = new Intent(SplashActivity.this, MainActivity.class);
        if (isFirstTime) {
            //Show the first time tutorial
            mainActivity = new Intent(SplashActivity.this, TutorialActivity.class);
        } else {
            if (!isRegistered) {
                mainActivity = new Intent(SplashActivity.this, RegisterActivity.class);
            }
        }
        startActivity(mainActivity);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }
}
