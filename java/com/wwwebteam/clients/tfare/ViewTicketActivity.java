package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwwebteam.clients.tfare.utils.Definitions;
import com.wwwebteam.clients.tfare.utils.ServerEngine;

/**
 * Created by wwweb on 26/04/2017.
 */
public class ViewTicketActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_ticket_activity);
        initializeWidget();
        getDataFromExtra();
    }

    ImageView qrcode;
    TextView ticketAdmitView;
    private void initializeWidget() {
        qrcode = (ImageView) findViewById(R.id.qrcode_holder);
        ticketAdmitView = (TextView) findViewById(R.id.ticket_admit_view);
    }

    int ticketId, ticketAdmit;
    private void getDataFromExtra() {
        Intent i = this.getIntent();
        if (i.hasExtra(Definitions.JSONKEY_TICKETID)) {
            ticketId = i.getIntExtra(Definitions.JSONKEY_TICKETID, 0);
            qrcode.setImageBitmap(ServerEngine.generateQRBitMap(ticketId+""));
        }

        if (i.hasExtra(Definitions.JSONKEY_TICKETADMITS)) {
            ticketAdmit = i.getIntExtra(Definitions.JSONKEY_TICKETADMITS, 0);
            ticketAdmitView.setText(ticketAdmit+"");
        }
    }
}
