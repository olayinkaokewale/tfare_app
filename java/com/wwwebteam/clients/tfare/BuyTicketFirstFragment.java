package com.wwwebteam.clients.tfare;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wwwebteam.clients.tfare.utils.DatabaseEngine;
import com.wwwebteam.clients.tfare.utils.Definitions;
import com.wwwebteam.clients.tfare.utils.ServerEngine;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.wwwebteam.clients.tfare.R.anim.fadein;
import static com.wwwebteam.clients.tfare.R.anim.fadeout;

/**
 * Created by wwweb on 26/04/2017.
 */

public class BuyTicketFirstFragment extends Fragment {
    ServerEngine Engine;
    DatabaseEngine dbEngine;
    FragmentActivity thisActivity;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(getActivity());
        getDataFromBase();

    }

    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        thisActivity = getActivity();
        initializeWidgets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buy_ticket_slide_1, container, false);
        return view;
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


    EditText currentBusstop, destination, ticketAdmits, ticketPrice;
    Button buyTicket;
    TextView errorDisplayArea;
    ProgressBar loadingBar;

    String currentBusstopStr, destinationStr, ticketAdmitStr, priceStr;
    Boolean isSelf;

    private void initializeWidgets() {
        currentBusstop = (EditText) this.getView().findViewById(R.id.start_bus_stop);
        destination = (EditText) this.getView().findViewById(R.id.destination);
        ticketAdmits = (EditText) this.getView().findViewById(R.id.ticket_admits);
        ticketPrice = (EditText) this.getView().findViewById(R.id.price);
        buyTicket = (Button) this.getView().findViewById(R.id.buy_ticket);
        errorDisplayArea = (TextView) this.getView().findViewById(R.id.error_display_area);
        loadingBar = (ProgressBar) this.getView().findViewById(R.id.loading_spinner);
        /*backButton = (ImageButton) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        buyTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentBusstopStr = currentBusstop.getText().toString();
                destinationStr = destination.getText().toString();
                ticketAdmitStr = ticketAdmits.getText().toString();
                priceStr = ticketPrice.getText().toString();
                if (!currentBusstopStr.isEmpty() && !destinationStr.isEmpty() && !ticketAdmitStr.isEmpty() && !priceStr.isEmpty()) {
                    isSelf = true;
                    new BuyTicketTask().execute();
                } else {
                    setErrorMessage("All fields are required!");
                }
            }
        });
    }



    private void setErrorMessage(String msg) {
        errorDisplayArea.setText(msg);
        errorDisplayArea.setVisibility(View.VISIBLE);
    }

    int ticketId;
    private class BuyTicketTask extends AsyncTask<String, String, String> {

        int feedback = 0;
        String message = "Unable to purchase ticket";
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
                //Buy Ticket: {"action":"buy_ticket", "user_id":"", "start_bus_stop":"", "destination":"", "price":"", "ticket_admits":""}
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_BUY_TICKET);
                request.put(Definitions.JSONKEY_USERID, userId);
                request.put(Definitions.JSONKEY_STARTBUSSTOP, currentBusstopStr);
                request.put(Definitions.JSONKEY_DESTINATION, destinationStr);
                request.put(Definitions.JSONKEY_PRICE, priceStr);
                request.put(Definitions.JSONKEY_TICKETADMITS, ticketAdmitStr);
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair(Definitions.SERVER_POST, request.toString()));
                JSONObject result = Engine.performServerOperation(param);
                if (result != null) {
                    feedback = result.getInt("feedback");
                    message = result.getString("details");
                    if (feedback == 1) {
                        ticketId = result.getInt("ticket_id");
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
            errorDisplayArea.setVisibility(View.VISIBLE);
            if (feedback == 0) {
                //Show message
                errorDisplayArea.setTextColor(Color.rgb(0xCF, 0x1E, 0x1E));
                errorDisplayArea.setText(message);
            } else {
                errorDisplayArea.setTextColor(Color.rgb(0x56, 0xAF, 0x38));
                errorDisplayArea.setText(message);
                //Send Ticket ID to view activity.
                Intent ticketView = new Intent(thisActivity, ViewTicketActivity.class);
                ticketView.putExtra(Definitions.JSONKEY_TICKETID, ticketId);
                ticketView.putExtra(Definitions.JSONKEY_TICKETADMITS, Integer.parseInt(ticketAdmitStr));
                startActivity(ticketView);
                thisActivity.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                thisActivity.finish();
            }
        }
    }
}
