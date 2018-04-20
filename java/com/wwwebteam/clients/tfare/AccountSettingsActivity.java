package com.wwwebteam.clients.tfare;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wwweb on 28/03/2017.
 */
public class AccountSettingsActivity extends FragmentActivity {

    private Fragment[] fragmentList  = new Fragment[] {
            new ChangePinFragment(), new ProfileSettingsFragment()
    };

    private String[] tabNames = {
            "Change Pin", "Profile Settings"
    };

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.account_settings_activity);
        initializeWidgets();
    }

    LinearLayout tabView;
    ViewPager viewPager;
    TabsPagerAdapter mAdapter;

    private void initializeWidgets() {
        tabView = (LinearLayout) findViewById(R.id.layoutTabs);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        addTabs();
        selectTab(0);
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

    }

    private void selectTab(int i) {
        for (int j=0; j<tabs.length; j++) {
            if (i==j) {
                tabs[j].setBackgroundResource(R.drawable.tab_sel_bg);
            } else {
                tabs[j].setBackgroundResource(R.drawable.tab_unsel_bg);
            }
        }
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            //TODO: Highlight selected tab. - addBottomDots(position);
            selectTab(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    TextView[] tabs;
    private void addTabs() {
        tabs = new TextView[fragmentList.length];

        tabView.removeAllViews();
        for (int i = 0; i < tabs.length; i++) {
            tabs[i] = new TextView(this);
            tabs[i].setText(tabNames[i]);
            tabs[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER_VERTICAL));
            tabs[i].setGravity(Gravity.CENTER_VERTICAL);
            tabs[i].setGravity(Gravity.CENTER_HORIZONTAL);
            tabs[i].setPadding(50,25,50,25);
            tabs[i].setTag(i);
            tabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: Change the tab
                    viewPager.setCurrentItem((int) view.getTag());
                    selectTab((int) view.getTag());
                }
            });
            tabView.addView(tabs[i]);
        }
    }



    public class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int index) {
            return fragmentList[index];
        }

        @Override
        public int getCount() {
            return fragmentList.length;
        }

    }

}
