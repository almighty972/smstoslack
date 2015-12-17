package com.jeanlambert.androidslackconnector;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.CompoundButton;


public class MainActivity extends AppCompatActivity {

    private CheckBox mSlackRedirectionCheckbox;

    private SharedPreferences mSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPrefs = this.getSharedPreferences(
                getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        boolean isChecked = mSharedPrefs.getBoolean("enableSlackRedirection", true);

        mSlackRedirectionCheckbox = (CheckBox) findViewById(R.id.slackRedirectionCheckbox);
        mSlackRedirectionCheckbox.setChecked(isChecked);
        mSlackRedirectionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mSharedPrefs.edit().putBoolean("enableSlackRedirection", checked).commit();
            }
        });

    }
}
