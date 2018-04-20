package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class TicketHistoryActivity extends Activity {

    ServerEngine Engine;
    DatabaseEngine dbEngine;
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.tickethistory_activity);
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
    LinearLayout mainContents;
    private void initializeWidgets() {
        settingsBtn = (ImageButton) findViewById(R.id.settings_btn);
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
        mainContents = (LinearLayout) findViewById(R.id.main_content);
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
                startActivity(new Intent(TicketHistoryActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

        new GetTicketHistoryTask().execute();
    }

    private class GetTicketHistoryTask extends AsyncTask<String, String, String> {
        int feedback = 0;
        String message = "Unable to retrieve ticket history";
        @Override
        public void onPreExecute() {
            errorDisplayArea.setText("");
            errorDisplayArea.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject request = new JSONObject();
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_GET_TICKET_HISTORY);
                request.put(Definitions.JSONKEY_USERID, userId);
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair(Definitions.SERVER_POST, request.toString()));
                JSONObject result = Engine.performServerOperation(param);
                if (result != null) {
                    feedback = result.getInt("feedback");
                    if (feedback == 1) {
                        JSONArray ticketArray = result.getJSONArray("details");
                        ArrayList<HashMap<String, String>> ticketList = new ArrayList<HashMap<String, String>>();
                        for (int i=0; i<ticketArray.length(); i++) {
                            JSONObject ticketDetails = ticketArray.getJSONObject(i);
                            HashMap<String, String> ticketMap = new HashMap<String, String>();
                            ticketMap.put(Definitions.JSONKEY_TICKETID, ticketDetails.getString(Definitions.JSONKEY_TICKETID));
                            ticketMap.put(Definitions.JSONKEY_USERID, ticketDetails.getString(Definitions.JSONKEY_USERID));
                            ticketMap.put(Definitions.JSONKEY_STARTBUSSTOP, ticketDetails.getString(Definitions.JSONKEY_STARTBUSSTOP));
                            ticketMap.put(Definitions.JSONKEY_DESTINATION, ticketDetails.getString(Definitions.JSONKEY_DESTINATION));
                            ticketMap.put(Definitions.JSONKEY_PRICE, ticketDetails.getString(Definitions.JSONKEY_PRICE));
                            ticketMap.put(Definitions.JSONKEY_BUYINGTIME, ticketDetails.getString(Definitions.JSONKEY_BUYINGTIME));
                            ticketMap.put(Definitions.JSONKEY_USAGETIME, ticketDetails.getString(Definitions.JSONKEY_USAGETIME));
                            ticketMap.put(Definitions.JSONKEY_TICKETADMITS, ticketDetails.getString(Definitions.JSONKEY_TICKETADMITS));

                            ticketList.add(ticketMap);
                        }
                        inflateResults(ticketList);
                    } else {
                        message = result.getString("details");
                    }
                }
            } catch (JSONException e) {

            }

            return null;
        }

        @Override
        public void onPostExecute(String string) {
            //Perform the post execute task here.
            loadingBar.setVisibility(View.GONE);
            if (feedback == 0) {
                //Show message
                errorDisplayArea.setVisibility(View.VISIBLE);
                errorDisplayArea.setText(message);
            }
        }
    }

    Handler updateHandler = new Handler();
    private void inflateResults(ArrayList<HashMap<String, String>> ticketList) {
        LayoutInflater factory = getLayoutInflater();
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 5);
        for (int i=0; i<ticketList.size(); i++) {
            final HashMap<String, String> map = ticketList.get(i);
            final ViewGroup ticketView = (ViewGroup) factory.inflate(R.layout.ticket_row, null);
            TextView ticketDetails = (TextView) ticketView.findViewById(R.id.ticket_details);
            String details = "Ticket ID: " + map.get(Definitions.JSONKEY_TICKETID) + "\nTicket Route Info: FROM " + map.get(Definitions.JSONKEY_STARTBUSSTOP) + " TO " + map.get(Definitions.JSONKEY_DESTINATION) + "\nDate Bought: " + map.get(Definitions.JSONKEY_BUYINGTIME) + "\nTicket Admits: " + map.get(Definitions.JSONKEY_TICKETADMITS);
            ticketDetails.setText(details);

            //Set onclick listener to view.
            ticketView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent ticketView = new Intent(TicketHistoryActivity.this, ViewTicketActivity.class);
                    ticketView.putExtra(Definitions.JSONKEY_TICKETID, Integer.parseInt(map.get(Definitions.JSONKEY_TICKETID)));
                    ticketView.putExtra(Definitions.JSONKEY_TICKETADMITS, Integer.parseInt(map.get(Definitions.JSONKEY_TICKETADMITS)));
                    startActivity(ticketView);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            });

            updateHandler.post(new Runnable() {
                @Override
                public void run() {
                    mainContents.addView(ticketView, layoutParams);
                }
            });
        }
    }
}