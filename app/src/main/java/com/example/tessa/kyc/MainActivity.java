package com.example.tessa.kyc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private FirebaseAuth mAuth;

    final String TAG = "DED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onGetStarted(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        Log.i(TAG, "signed in");
//        if (currentUser!=null) updateUI(currentUser);
//        else onResume();
//    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

}
