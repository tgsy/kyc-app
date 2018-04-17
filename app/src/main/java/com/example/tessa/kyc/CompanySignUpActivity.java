package com.example.tessa.kyc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CompanySignUpActivity extends BaseActivity {
    private int companyID;
    private EditText companyIDEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ImageView companyLogo;

    private HashMap<Integer,String> validCompanies = new HashMap<>();
    private HashMap<Integer,String> companyImages = new HashMap<>();

    private StorageReference mImageRef;

    private static final File FILE = new File("/storage/emulated/0/blocktrace/banks.json");

    @Override
    public void onCreate(Bundle savedInstanceState) { //was protected
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_signup);

        companyIDEditText = (EditText) findViewById(R.id.company_id);
        usernameEditText = (EditText) findViewById(R.id.Company_username);
        passwordEditText = (EditText)findViewById(R.id.Company_password);
        companyLogo = (ImageView) findViewById(R.id.company_logo);
        mImageRef = FirebaseStorage.getInstance().getReference().child("companylogos");

        String jsonString = parseJson(FILE);
        List<Company> list = Arrays.asList(new Gson().
                fromJson(jsonString, Company[].class));

        for (Company c:list) {
            validCompanies.put(c.getId(), c.getName());
            companyImages.put(c.getId(), c.getImage());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.company_submit:
                checkforCompany(companyIDEditText);
                break;
            case R.id.Company_link_button:
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                Intent intent = new Intent(this, ReadTokenActivity.class);
                intent.putExtra("Company Name", validCompanies.get(companyID));
                intent.putExtra("Company ID", companyID);
                intent.putExtra("Origin", "Company Registration");
//                intent.putExtra("Token", getToken().toString());
                intent.putExtra("Username", username);
                intent.putExtra("Password", password);
                startActivity(intent);
        }
    }

    public void checkforCompany(EditText companyid) {
        if (validateCompanyField()) {
            //do not allow integer
            companyID = Integer.valueOf(companyid.getText().toString());
            if (validCompanies.containsKey(companyID)) {
                showProgressDialog();
                findViewById(R.id.company_submit).setVisibility(View.GONE);
                Glide.with(this)
                        .load(mImageRef.child(companyImages.get(companyID)))
                        .into(companyLogo);
                findViewById(R.id.Company_email_password_fields).setVisibility(View.VISIBLE);
                findViewById(R.id.Company_link_button).setVisibility(View.VISIBLE);
                hideProgressDialog();

            } else Toast.makeText(CompanySignUpActivity.this,
                    "Please enter a valid company ID",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String parseJson(File file) {
        String line;
        String output = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
            try {
                while ((line = reader.readLine()) != null) {
                    output += line;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return output;
    }

    private boolean validateForm() {
        boolean valid = true;

        if (usernameEditText.getVisibility()==View.VISIBLE &&
                passwordEditText.getVisibility()==View.VISIBLE) {
            if (TextUtils.isEmpty(usernameEditText.getText().toString())) {
                usernameEditText.setError("Required.");
                valid = false;
            } else if (TextUtils.isEmpty(passwordEditText.getText().toString())) {
                passwordEditText.setError("Required.");
                valid = false;
            }
            else {
                usernameEditText.setError(null);
               passwordEditText.setError(null);
            }
        }
        return valid;
    }

    private boolean validateCompanyField(){
        boolean valid = true;
        if (TextUtils.isEmpty(companyIDEditText.getText().toString())) {
            companyIDEditText.setError("Required.");
            valid = false;
        } else {
            companyIDEditText.setError(null);
        }
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
