package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by wwweb on 28/03/2017.
 */
public class SupportActivity extends Activity {

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.support_activity);
        initializeWidgets();
    }

    LinearLayout faqBtn, complainTicketBtn;
    ImageButton settingsBtn;//, backButton;
    private void initializeWidgets() {
        settingsBtn = (ImageButton) findViewById(R.id.settings_btn);
        faqBtn = (LinearLayout) findViewById(R.id.faq_btn);
        complainTicketBtn = (LinearLayout) findViewById(R.id.complain_ticket_btn);
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
                startActivity(new Intent(SupportActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
        faqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SupportActivity.this, FAQActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
        complainTicketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SupportActivity.this, ComplainTicketActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
    }
}