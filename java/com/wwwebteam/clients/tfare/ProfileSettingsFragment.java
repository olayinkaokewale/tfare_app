package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wwweb on 26/04/2017.
 */

public class ProfileSettingsFragment extends Fragment {

    ServerEngine Engine;
    DatabaseEngine dbEngine;
    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        Engine = new ServerEngine();
        dbEngine = new DatabaseEngine(this.getActivity());
        getDataFromBase();
    }

    Activity thisActivity;
    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);
        thisActivity = getActivity();
        initializeWidgets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_setting_profile, container, false);
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


    EditText name, location;
    Button updateProfile;
    TextView errorDisplayArea;
    ProgressBar loadingBar;
    //ImageButton backButton;

    String nameStr, locationStr;
    private void initializeWidgets() {
        View thisView = this.getView();
        name = (EditText) thisView.findViewById(R.id.name);
        location = (EditText) thisView.findViewById(R.id.location);
        updateProfile = (Button) thisView.findViewById(R.id.update_profile);
        errorDisplayArea = (TextView) thisView.findViewById(R.id.error_display_area);
        loadingBar = (ProgressBar) thisView.findViewById(R.id.loading_spinner);
        /*backButton = (ImageButton) findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });*/

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameStr = name.getText().toString();
                locationStr = location.getText().toString();
                if (!nameStr.isEmpty() && !locationStr.isEmpty()) {
                    new UpdateProfileTask().execute();
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
    private class UpdateProfileTask extends AsyncTask<String, String, String> {

        int feedback = 0;
        String message = "Unable update profile";
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
                //Update Profile: {"action":"update_profile", "user_id":"", "fullname":"", "location":""}
                request.put(Definitions.SERVER_ACTION, Definitions.ACTION_PROFILE_UPDATE);
                request.put(Definitions.JSONKEY_USERID, userId);
                request.put(Definitions.JSONKEY_FULLNAME, nameStr);
                request.put(Definitions.JSONKEY_LOCATION, locationStr);
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


