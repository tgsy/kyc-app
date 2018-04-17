package com.example.tessa.kyc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class CompanyLoginActivity extends BaseActivity {

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mImageRef = mStorageRef.child("companylogos");
    private String companyName;

    ImageView companyLogoImageView;
    Button companyLoginButton;
    EditText usernameEditText;
    EditText passwordEditText;

    int companyID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_login);
        showProgressDialog();
        String logoFilename = getIntent().getStringExtra("Company Logo");
        companyName = getIntent().getStringExtra("Company Name");
        companyLogoImageView = (ImageView) findViewById(R.id.CompanyLogin_logo);
        usernameEditText = (EditText) findViewById(R.id.CompanyLogin_username);
        passwordEditText = (EditText) findViewById(R.id.CompanyLogin_password);
        companyLoginButton = (Button) findViewById(R.id.CompanyLogin_button);
        TextView companyLoginName = (TextView) findViewById(R.id.CompanyLogin_name);
        String company = getString(R.string.company_login, companyName);
        companyLoginName.setText(company);


        companyID = getIntent().getIntExtra("Company ID", 1000);

        Glide.with(this)
                .load(mImageRef.child(logoFilename))
                .into(companyLogoImageView);
        hideProgressDialog();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.CompanyLogin_button) {
            if (validateForm()) {
                Intent intent = new Intent(this, ReadTokenActivity.class);
                intent.putExtra("Origin", "Company Login");
                intent.putExtra("Company Name", companyName);
                intent.putExtra("Username", usernameEditText.getText().toString());
                intent.putExtra("Password", passwordEditText.getText().toString());
                startActivity(intent);
            }
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        if (TextUtils.isEmpty(usernameEditText.getText())) {
            usernameEditText.setError("Required.");
            valid = false;
        } else
            usernameEditText.setError(null);
        if (TextUtils.isEmpty(passwordEditText.getText().toString())) {
            passwordEditText.setError("Required.");
            valid = false;
        } else
            passwordEditText.setError(null);

        return valid;
    }

    @Override
    public void onBackPressed() {
        Intent i = getParentActivityIntent();
        i.putExtra("fragmentToLoad", 1);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
