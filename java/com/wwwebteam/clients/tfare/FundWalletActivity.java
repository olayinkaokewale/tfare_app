package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Handler;

import com.wwwebteam.clients.tfare.utils.DatabaseEngine;
import com.wwwebteam.clients.tfare.utils.Definitions;
import com.wwwebteam.clients.tfare.utils.ServerEngine;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.zip.Inflater;

/**
 * Created by wwweb on 28/03/2017.
 */
public class FundWalletActivity extends Activity {

    ServerEngine Engine;
    DatabaseEngine dbEngine;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(this);
        setContentView(R.layout.fundwallet_activity);
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

    Button selectCardBtn, fundTfareBtn;
    EditText fundAmount;
    TextView errorDisplayArea;
    ProgressBar loadingBar;
    ImageButton settingsBtn;//, backButton;

    String amountEntered;
    private void initializeWidgets() {
        settingsBtn = (ImageButton) findViewById(R.id.settings_btn);
        selectCardBtn = (Button) findViewById(R.id.select_card);
        fundTfareBtn = (Button) findViewById(R.id.fund_tfare);
        fundAmount = (EditText) findViewById(R.id.enter_amount);
        errorDisplayArea = (TextView) findViewById(R.id.error_display_area);
        loadingBar = (ProgressBar) findViewById(R.id.loading_spinner);
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
                startActivity(new Intent(FundWalletActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
        selectCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createCardDialog();
            }
        });
        fundTfareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Get basic information and send to server
                amountEntered = fundAmount.getText().toString();
                new FundTfareTask().execute();
            }
        });
    }

    String creditCardId;
    Dialog cardDialog;
    ProgressBar cardLoadingSpinner;
    LinearLayout cardsListLayout;
    private void createCardDialog() {
        cardDialog = new Dialog(FundWalletActivity.this);
        cardDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        cardDialog.setContentView(R.layout.creditcard_dialog);
        cardDialog.setCancelable(false);

        cardLoadingSpinner = (ProgressBar) cardDialog.findViewById(R.id.loading_spinner);
        cardsListLayout = (LinearLayout) cardDialog.findViewById(R.id.cards_list);
        new GetCardsFromServerTask().execute();
        cardDialog.show();
    }

    private class GetCardsFromServerTask extends AsyncTask<String, String, String> {
        int feedback = 0;
        String message = "Unable to retrieve credit cards";
        @Override
        public void onPreExecute() {
            //Set dialog loading bar.
            errorDisplayArea.setText("");
            errorDisplayArea.setVisibility(View.INVISIBLE);
            cardLoadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject request = new JSONObject();
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_GET_USER_CREDITCARD);
                request.put(Definitions.JSONKEY_USERID, userId);
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair(Definitions.SERVER_POST, request.toString()));
                JSONObject result = Engine.performServerOperation(param);
                if (result != null) {
                    feedback = result.getInt("feedback");
                    if (feedback == 1) { //Continue with tasks.
                        JSONArray cardsArray = result.getJSONArray("details");
                        ArrayList<HashMap<String, String>> cardList = new ArrayList<HashMap<String, String>>();
                        for (int i=0; i<cardsArray.length(); i++) {
                            JSONObject cardDetails = cardsArray.getJSONObject(i);
                            HashMap<String, String> cardMap = new HashMap<String, String>();
                            cardMap.put(Definitions.JSONKEY_CARDID, cardDetails.getString(Definitions.JSONKEY_CARDID));
                            cardMap.put(Definitions.JSONKEY_CARDNUMBER, cardDetails.getString(Definitions.JSONKEY_CARDNUMBER));
                            cardList.add(cardMap);
                        }
                        inflateDialogHandler(cardList);
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
            cardLoadingSpinner.setVisibility(View.GONE);
            if (feedback == 0) {
                cardDialog.dismiss();
                errorDisplayArea.setVisibility(View.VISIBLE);
                errorDisplayArea.setText(message);
            }
        }
    }

    Handler updateHandler = new Handler();
    private void inflateDialogHandler(ArrayList<HashMap<String, String>> cardsArray) {
        LayoutInflater factory = getLayoutInflater();
        for (int i=0; i<cardsArray.size(); i++) {
            HashMap<String, String> map = cardsArray.get(i);
            final TextView cardView = (TextView) factory.inflate(R.layout.card_row, null);
            String cardNumber = map.get(Definitions.JSONKEY_CARDNUMBER);
            final String displayCardDetails = "*-*-*-" + cardNumber.substring(cardNumber.length()-4, cardNumber.length());
            cardView.setText(displayCardDetails);
            cardView.setTag(map.get(Definitions.JSONKEY_CARDID));
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    creditCardId = cardView.getTag().toString();
                    cardDialog.dismiss();
                    updateHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            selectCardBtn.setText("Selected: " + displayCardDetails);
                        }
                    });
                }
            });
            updateHandler.post(new Runnable() {
                @Override
                public void run() {
                    cardsListLayout.addView(cardView);
                    cardDialog.setCancelable(true);
                }
            });

        }

    }

    private class FundTfareTask extends AsyncTask<String, String, String> {
        int feedback = 0;
        String message = "Unable to fund wallet. Try again";
        int topUpBalance;
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
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_TOPUP);
                request.put(Definitions.JSONKEY_USERID, userId);
                request.put(Definitions.JSONKEY_CARDID, creditCardId);
                request.put(Definitions.JSONKEY_AMOUNT, amountEntered);
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair(Definitions.SERVER_POST, request.toString()));
                Log.d("FundWallet", param.toString());
                JSONObject result = Engine.performServerOperation(param);
                if (result != null) {
                    feedback = result.getInt("feedback");
                    message = result.getString("details");
                    if (feedback == 1) {
                        topUpBalance = result.getInt("topup_balance");
                        dbEngine.open();
                        ContentValues initialValues = new ContentValues();
                        initialValues.put(DatabaseEngine.TAB_USER_BALANCE, topUpBalance);
                        dbEngine.updateDataInDatabase(DatabaseEngine.DB_TAB_USER, initialValues, DatabaseEngine.TAB_USER_ID+"="+userId);
                        dbEngine.close();
                    }
                }
            } catch (JSONException e) {

            }

            return null;
        }

        @Override
        public void onPostExecute(String string) {
            //Perform the post execute task here.
            if (feedback == 1) {
                //Progress to main activity
                Intent i = FundWalletActivity.this.getIntent();
                i.putExtra(Definitions.JSONKEY_TOPUPBALANCE, topUpBalance);
                setResult(RESULT_OK, i);
                finish();
            } else {
                //Show message
                loadingBar.setVisibility(View.INVISIBLE);
                errorDisplayArea.setVisibility(View.VISIBLE);
                errorDisplayArea.setText(message);
            }
        }
    }
}
