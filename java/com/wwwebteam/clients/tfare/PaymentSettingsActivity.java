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

/**
 * Created by wwweb on 28/03/2017.
 */
public class PaymentSettingsActivity  extends Activity {

    ServerEngine Engine;
    DatabaseEngine dbEngine;
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(this);
        setContentView(R.layout.payment_settings_activity);
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


    EditText cardNumber, cardCv2, cardExpiryDate, cardPin;
    Button addCard;
    TextView errorDisplayArea;
    ProgressBar loadingBar;
    //ImageButton backButton;

    String cardNumberStr, cardCv2Str, cardExpiryDateStr, cardPinStr;
    private void initializeWidgets() {
        cardNumber = (EditText) findViewById(R.id.card_number);
        cardCv2 = (EditText) findViewById(R.id.cv2_number);
        cardExpiryDate = (EditText) findViewById(R.id.expiry_date);
        cardPin = (EditText) findViewById(R.id.card_pin);
        addCard = (Button) findViewById(R.id.add_card);
        errorDisplayArea = (TextView) findViewById(R.id.error_display_area);
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
        /*backButton = (ImageButton) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardNumberStr = cardNumber.getText().toString();
                cardCv2Str = cardCv2.getText().toString();
                cardExpiryDateStr = cardExpiryDate.getText().toString();
                cardPinStr = cardPin.getText().toString();
                if (!cardNumberStr.isEmpty() && !cardCv2Str.isEmpty() && !cardExpiryDateStr.isEmpty() && !cardPinStr.isEmpty()) {
                    new AddCardTask().execute();
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
    private class AddCardTask extends AsyncTask<String, String, String> {

        int feedback = 0;
        String message = "Unable to add credit card";
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
                //Add card: {"action":"add_card", "user_id":"", "card_number":"", "card_cv2":"", "card_exp_date":"", "card_pin":"", "isgiftcard":""}
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_ADD_CARD);
                request.put(Definitions.JSONKEY_USERID, userId);
                request.put(Definitions.JSONKEY_CARDNUMBER, cardNumberStr);
                request.put(Definitions.JSONKEY_CARDCV2, cardCv2Str);
                request.put(Definitions.JSONKEY_CARDPIN, cardPinStr);
                request.put(Definitions.JSONKEY_ISGIFTCARD, "1");
                request.put(Definitions.JSONKEY_CARDEXPDATE, cardExpiryDateStr);
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
