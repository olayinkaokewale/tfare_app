package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wwwebteam.clients.tfare.utils.DatabaseEngine;
import com.wwwebteam.clients.tfare.utils.Definitions;
import com.wwwebteam.clients.tfare.utils.ServerEngine;

public class MainActivity extends Activity {

    ServerEngine Engine;
    DatabaseEngine dbEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(this);
        getDataFromBase();
        initailizeWidgets();
        //changeStatusBar();
    }

    private void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    String topUpBalance = "0", userId, phoneNumber, userLocation;
    String[] gets = {DatabaseEngine.TAB_USER_ID, DatabaseEngine.TAB_USER_PHONE_NO, DatabaseEngine.TAB_USER_BALANCE, DatabaseEngine.TAB_USER_LOCATION};
    private void getDataFromBase() {
        dbEngine.open();
        Cursor userCursor = dbEngine.getDistinctDataFromDatabaseTable(gets, DatabaseEngine.DB_TAB_USER, null);
        //Do the rest here.
        if (userCursor != null && userCursor.getCount() > 0) {
            userCursor.moveToFirst();
            topUpBalance = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseEngine.TAB_USER_BALANCE));
            userId = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseEngine.TAB_USER_ID));
            phoneNumber = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseEngine.TAB_USER_PHONE_NO));
            userLocation = userCursor.getString(userCursor.getColumnIndexOrThrow(DatabaseEngine.TAB_USER_LOCATION));

        }
        dbEngine.close();
    }

    ImageButton settingsBtn;//, backButton;
    Button topUpBtn;
    LinearLayout buyTicket, fundWallet, support, ticketHistory;
    TextView topupBalanceArea;
    private void initailizeWidgets() {
        settingsBtn = (ImageButton) findViewById(R.id.settings_btn);
        topUpBtn = (Button) findViewById(R.id.topup_btn);
        buyTicket = (LinearLayout) findViewById(R.id.ticket_btn);
        fundWallet = (LinearLayout) findViewById(R.id.fundwallet_btn);
        support = (LinearLayout) findViewById(R.id.support_btn);
        ticketHistory = (LinearLayout) findViewById(R.id.ticket_history_btn);
        topupBalanceArea = (TextView) findViewById(R.id.balance);
        topupBalanceArea.setText("N"+topUpBalance);
        /*backButton = (ImageButton) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        //Handle clicks now.
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        topUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, FundWalletActivity.class), Definitions.REQUEST_TOPUP);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        buyTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BuyTicketActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        fundWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(MainActivity.this, FundWalletActivity.class), Definitions.REQUEST_TOPUP);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SupportActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        ticketHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, TicketHistoryActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
             if (requestCode == Definitions.REQUEST_TOPUP) {
                 //Increase balance with data from intent.
                 if (data != null) {
                     int balance = data.getExtras().getInt(Definitions.JSONKEY_TOPUPBALANCE);
                     topupBalanceArea.setText("N"+balance);
                 }
             }
        }
    }
}
