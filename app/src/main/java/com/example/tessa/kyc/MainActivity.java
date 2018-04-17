package com.example.tessa.kyc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = getBaseContext().getSharedPreferences(
                getString(R.string.preference_getting_started), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        boolean previouslyStarted = sharedPref.getBoolean(getString(R.string.preference_getting_started), false);
        if (!previouslyStarted) {
            editor.putBoolean(getString(R.string.preference_getting_started), Boolean.TRUE);
            editor.apply();
        } else
            moveOn();
    }

    public void onGetStarted(View view) {
        moveOn();
    }

    public void moveOn() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
