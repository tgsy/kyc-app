package com.example.tessa.kyc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class MainActivity extends BaseActivity {

    private SharedPreferences sharedPref;

    private SharedPreferences.Editor editor;

    private boolean previouslyStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        sharedPref = getBaseContext().getSharedPreferences(
                getString(R.string.preference_getting_started), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        previouslyStarted = sharedPref.getBoolean(getString(R.string.preference_getting_started), false);
        Log.i("ONSTART", "previouslyStarted = "+ previouslyStarted);

        if (!previouslyStarted) {
            editor.putBoolean(getString(R.string.preference_getting_started), Boolean.TRUE);
            editor.apply();
        } else
            moveOn();
    }

    public void onGetStarted(View view){
        moveOn();
    }

    public void moveOn() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


/*    public void onLogin(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.i(TAG, "signed in");
        if (currentUser!=null) updateUI(currentUser);
        else onResume();
    }*/

/*    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }*/

}
