package com.example.tessa.kyc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class CompanyActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private int companyID;
    private EditText companyIDEditText;
    private Button signUpButton;

    private HashMap<Integer,String> validCompanies;

    private static final File FILE = new File("/storage/emulated/0/blocktrace/banks.json");

/*    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        companyIDEditText = (EditText) findViewById(R.id.company_id);
        signUpButton = (Button) findViewById(R.id.company_submit);

        /*sharedPref = this.getSharedPreferences(
                getString(R.string.preference_companies_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();*/

        String jsonString = parseJson(FILE);
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
                mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("company").child(Integer.toString(companyID)).setValue(true);
                Intent intent = new Intent(this, ScanTokenActivity.class);
                intent.putExtra("Company", validCompanies.get(companyID));
                intent.putExtra("Origin", "Company");
                startActivity(intent);
            } else Toast.makeText(CompanyActivity.this,
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
