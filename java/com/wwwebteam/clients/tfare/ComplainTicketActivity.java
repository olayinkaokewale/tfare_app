package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wwwebteam.clients.tfare.utils.DatabaseEngine;
import com.wwwebteam.clients.tfare.utils.Definitions;
import com.wwwebteam.clients.tfare.utils.ServerEngine;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wwweb on 28/03/2017.
 */
public class ComplainTicketActivity extends Activity {

    ServerEngine Engine;
    DatabaseEngine dbEngine;
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.complain_ticket);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(this);
        getDataFromBase();
        initializeWidgets();
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

    TextView errorDisplayArea;
    ProgressBar loadingBar;
    ImageButton settingsBtn;//, backButton;
    EditText complainTitle, complainMessage;
    Button selectUrgencyBtn, sendComplaintBtn;

    String title, urgency="0", message;
    private void initializeWidgets() {
        settingsBtn = (ImageButton) findViewById(R.id.settings_btn);
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
        complainTitle = (EditText) findViewById(R.id.complaint_title);
        complainMessage = (EditText) findViewById(R.id.complaint_message);
        selectUrgencyBtn = (Button) findViewById(R.id.select_urgency);
        sendComplaintBtn = (Button) findViewById(R.id.send_complaint);
        errorDisplayArea = (TextView) findViewById(R.id.error_display_area);
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
                startActivity(new Intent(ComplainTicketActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        selectUrgencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog for urgency selection here.
            }
        });

        sendComplaintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = complainTitle.getText().toString();
                message = complainMessage.getText().toString();
                if (!title.isEmpty() && !message.isEmpty()) {
                    new SendComplaintTask().execute();
                } else {
                    setErrorMessage("All fields are required.");
                }
            }
        });
    }

    private void setErrorMessage(String msg) {
        errorDisplayArea.setText(msg);
        errorDisplayArea.setVisibility(View.VISIBLE);
    }

    private class SendComplaintTask extends AsyncTask<String, String, String> {
        int feedback = 0;
        String message = "Unable to lodge complaint";
        @Override
        public void onPreExecute() {
            errorDisplayArea.setText("");
            errorDisplayArea.setVisibility(View.INVISIBLE);
            loadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject request = new JSONObject();
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_FILE_COMPLAINT);
                request.put(Definitions.JSONKEY_USERID, userId);
                request.put(Definitions.JSONKEY_COMPLAINT, message);
                request.put(Definitions.JSONKEY_COMPLAINTITLE, title);
                request.put(Definitions.JSONKEY_COMPLAINURGENCY, urgency);
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair(Definitions.SERVER_POST, request.toString()));
                JSONObject result = Engine.performServerOperation(param);
                if (result != null) {
                    feedback = result.getInt("feedback");
                    message = result.getString("details");
                }
            } catch (JSONException e) {

            }

            return null;
        }

        @Override
        public void onPostExecute(String string) {
            //Perform the post execute task here.
            loadingBar.setVisibility(View.GONE);
            errorDisplayArea.setVisibility(View.VISIBLE);
            if (feedback == 0) {
                //Show message
                errorDisplayArea.setTextColor(Color.rgb(0xCF, 0x1E, 0x1E));
                errorDisplayArea.setText(message);
            } else {
                errorDisplayArea.setTextColor(Color.rgb(0x56, 0xAF, 0x38));
                errorDisplayArea.setText(message);
            }
        }
    }
}
