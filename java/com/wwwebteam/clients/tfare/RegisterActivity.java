package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wwwebteam.clients.tfare.utils.DatabaseEngine;
import com.wwwebteam.clients.tfare.utils.Definitions;
import com.wwwebteam.clients.tfare.utils.ServerEngine;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wwweb on 26/03/2017.
 */
public class RegisterActivity extends Activity {

    EditText phoneNumber, password;
    Button registerButton, loginButton;
    TextView errorDisplayArea;
    ProgressBar loadingSpinner;
    ServerEngine Engine;
    DatabaseEngine dbEngine;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.register_activity);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(this);
        initializeWidgets();
        changeStatusBar();

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

    String phoneNumberStr, passwordStr;
    private void initializeWidgets() {
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        password = (EditText) findViewById(R.id.password);
        registerButton = (Button) findViewById(R.id.register_btn);
        loginButton = (Button) findViewById(R.id.login_btn);
        errorDisplayArea = (TextView) findViewById(R.id.error_display_area);
        loadingSpinner = (ProgressBar) findViewById(R.id.loading_spinner);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumberStr = phoneNumber.getText().toString();
                passwordStr = password.getText().toString();
                if (!phoneNumberStr.isEmpty() && !passwordStr.isEmpty()) {
                    new RegisterTask().execute();
                } else {
                    String msg = "";
                    if (phoneNumberStr.isEmpty()) {
                        msg = "Phone number can not be empty!";
                    } else {
                        msg = "password can not be empty!";
                    }
                    setErrorMessage(msg);
                }

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });
    }

    private void setErrorMessage(String msg) {
        errorDisplayArea.setText(msg);
        errorDisplayArea.setVisibility(View.VISIBLE);
    }


    private class RegisterTask extends AsyncTask<String, String, String> {
        int feedback = 0;
        String errorMessage = "Error occurred while registering.";
        @Override
        public void onPreExecute() {
            errorDisplayArea.setText("");
            errorDisplayArea.setVisibility(View.INVISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                JSONObject request = new JSONObject();
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_REGISTER);
                request.put(Definitions.JSONKEY_PHONE_NUMBER, phoneNumberStr);
                request.put(Definitions.JSONKEY_PASSWORD, passwordStr);
                List<NameValuePair> param = new ArrayList<NameValuePair>();
                param.add(new BasicNameValuePair(Definitions.SERVER_POST, request.toString()));
                Log.d("RegisterActivity", param.toString());
                JSONObject result = Engine.performServerOperation(param);
                if (result != null) {
                    feedback = result.getInt("feedback");
                    if (feedback == 1) { //Proceed to storing the data into shared preference and database.
                        //Get the details.
                        JSONObject details = result.getJSONObject("details");
                        String userId = details.getString(Definitions.JSONKEY_USERID);
                        String phoneNumber = details.getString(Definitions.JSONKEY_PHONE_NUMBER);
                        String fullName = details.getString(Definitions.JSONKEY_FULLNAME);
                        String location = details.getString(Definitions.JSONKEY_LOCATION);
                        String balance = details.getString(Definitions.JSONKEY_TOPUPBALANCE);

                        SharedPreferences prefs = RegisterActivity.this.getSharedPreferences(Definitions.PREFERENCE_FILE, MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(Definitions.ISREGISTERED, true);
                        editor.commit();

                        dbEngine.open();
                        ContentValues initialValues = new ContentValues();
                        initialValues.put(DatabaseEngine.TAB_USER_ID, userId);
                        initialValues.put(DatabaseEngine.TAB_USER_PHONE_NO, phoneNumber);
                        initialValues.put(DatabaseEngine.TAB_USER_FULLNAME, fullName);
                        initialValues.put(DatabaseEngine.TAB_USER_LOCATION, location);
                        initialValues.put(DatabaseEngine.TAB_USER_BALANCE, balance);
                        initialValues.put(DatabaseEngine.TAB_USER_TOKEN, "");
                        dbEngine.insertIntoDatabaseTable(initialValues, DatabaseEngine.DB_TAB_USER);
                        dbEngine.close();
                    } else {
                        errorMessage = result.getString("details");
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
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            } else {
                //Show message
                loadingSpinner.setVisibility(View.INVISIBLE);
                errorDisplayArea.setVisibility(View.VISIBLE);
                errorDisplayArea.setText(errorMessage);
            }
        }
    }
}
