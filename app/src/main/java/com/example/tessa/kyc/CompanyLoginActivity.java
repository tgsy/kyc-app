package com.example.tessa.kyc;

import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.util.Arrays;

public class CompanyLoginActivity extends BaseActivity {

    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mImageRef = mStorageRef.child("companylogos");
    private String logoFilename;
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
        logoFilename = getIntent().getStringExtra("Company Logo");
        companyName = getIntent().getStringExtra("Company Name");
        companyLogoImageView = (ImageView) findViewById(R.id.CompanyLogin_logo);
        usernameEditText = (EditText) findViewById(R.id.CompanyLogin_email);
        passwordEditText = (EditText) findViewById(R.id.CompanyLogin_password);
        companyLoginButton = (Button) findViewById(R.id.CompanyLogin_button);

        companyID = getIntent().getIntExtra("Company ID", 1000);

        Glide.with(this)
                .load(mImageRef.child(logoFilename))
                .into(companyLogoImageView);
        hideProgressDialog();
    }

    public void onClick(View view) {
        if (view.getId() == R.id.CompanyLogin_button) {
            Intent intent = new Intent(this, ReadTokenActivity.class);
            intent.putExtra("Origin", "Company Login");
            intent.putExtra("Company Name", companyName);
            intent.putExtra("Username", usernameEditText.getText().toString());
            intent.putExtra("Password", passwordEditText.getText().toString());
            startActivity(intent);
        }
    }
}
