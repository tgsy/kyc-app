package com.example.tessa.kyc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CompanyActivity extends AppCompatActivity {

    int companyID;
    EditText companyIDEditText;
    Button signUpButton;

    HashMap<Integer,String> validCompanies;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);

        companyIDEditText = (EditText) findViewById(R.id.company_id);
        signUpButton = (Button) findViewById(R.id.company_submit);

        sharedPref = this.getSharedPreferences(
                getString(R.string.preference_companies_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        String jsonString = parseJson(R.raw.banks);
        List<Company> list = Arrays.asList(new Gson().
                fromJson(jsonString, Company[].class));
        validCompanies = new HashMap<>();

        for (Company c:list) {
            validCompanies.put(c.getId(), c.getName());
            //editor.putBoolean(Integer.toString(c.getId()), false);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.company_submit:
                signUpforCompany(companyIDEditText);
                break;
        }
    }

    public void signUpforCompany(EditText companyid) {
        if (validateForm()) {
            companyID = Integer.valueOf(companyid.getText().toString());
            if (validCompanies.containsKey(companyID)) {
                editor.putBoolean(Integer.toString(companyID), true);
                editor.commit();
                /*Toast.makeText(CompanyActivity.this,
                        "Sign Up for " + validCompanies.get(companyID) + " successful",
                        Toast.LENGTH_SHORT).show();*/
                Intent intent = new Intent(this, ScanTokenActivity.class);
//                intent.putExtra("MainLoggedInActivity", "Company");
                intent.putExtra("Company", validCompanies.get(companyID));
                intent.putExtra("Origin", "Company");
                startActivity(intent);
            } else Toast.makeText(CompanyActivity.this,
                    "Please enter a valid company ID",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String parseJson(int resource) {
        String line;
        String output = "";
        InputStream inputStream = getResources().openRawResource(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while ((line = reader.readLine()) != null) {
                output = output + line;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return output;
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(companyIDEditText.getText().toString())) {
            companyIDEditText.setError("Required.");
            valid = false;
        } else {
            companyIDEditText.setError(null);
        }

        return valid;
    }
}
