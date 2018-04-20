package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by wwweb on 28/03/2017.
 */
public class FAQActivity extends Activity {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.faq_activity);
        initializeWidgets();
    }

    ImageButton settingsBtn;//, backButton;
    LinearLayout mainContents;
    private void initializeWidgets() {
        settingsBtn = (ImageButton) findViewById(R.id.settings_btn);
        mainContents = (LinearLayout) findViewById(R.id.main_content);
        /*backButton = (ImageButton) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FAQActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
    }
}
