package com.example.tessa.kyc;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseActivity implements
        View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("users");
        mEmailField = (EditText) findViewById(R.id.Login_email);
        mPasswordField = (EditText) findViewById(R.id.Login_password);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null) updateUI(currentUser);
        else onResume();
    }


    private void signIn(String email, String password) {
        if (!validateForm()) return;
        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            showProgressDialog();
            usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                       goToActivity(ProfileActivity.class);
                    } else {
                        goToActivity(MainLoggedInActivity.class);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            findViewById(R.id.Profile_Invalid).setVisibility(View.VISIBLE);
        }
    }

    private void goToActivity(Class activityclass) {
        Intent intent = new Intent(this, activityclass);
        intent.putExtra("Origin", "Login");
        intent.putExtra("E-mail", mAuth.getCurrentUser().getEmail());
        intent.putExtra("ID", mAuth.getCurrentUser().getUid());
        startActivity(intent);
        hideProgressDialog();
        finish();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else
            mEmailField.setError(null);


        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else
            mPasswordField.setError(null);


        return valid;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_sign_in_button) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
        else if (i == R.id.Login_signUp) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
    }
}
