package com.example.tessa.kyc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import org.json.JSONObject;

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

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private int companyID;
    private EditText companyIDEditText;
    private Button signUpButton;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button linkAccountButton;
    private ImageView companyLogo;

    private String username;
    private String password;

    private HashMap<Integer,String> validCompanies;
    private HashMap<Integer,String> companyImages;

    private StorageReference mImageRef;

    private static final File FILE = new File("/storage/emulated/0/blocktrace/banks.json");

/*    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;*/

    @Override
    public void onCreate(Bundle savedInstanceState) { //was protected
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        companyIDEditText = (EditText) findViewById(R.id.company_id);
        signUpButton = (Button) findViewById(R.id.company_submit);

        usernameEditText = (EditText) findViewById(R.id.Company_email);
        passwordEditText = (EditText)findViewById(R.id.Company_password);

        linkAccountButton = (Button) findViewById(R.id.Company_link_button);

        companyLogo = (ImageView) findViewById(R.id.company_logo);

        mImageRef = FirebaseStorage.getInstance().getReference().child("companylogos");
        /*sharedPref = this.getSharedPreferences(
                getString(R.string.preference_companies_key), Context.MODE_PRIVATE);
        editor = sharedPref.edit();*/

        String jsonString = parseJson(FILE);
        List<Company> list = Arrays.asList(new Gson().
                fromJson(jsonString, Company[].class));
        validCompanies = new HashMap<>();
        companyImages = new HashMap<>();

        for (Company c:list) {
            validCompanies.put(c.getId(), c.getName());
            companyImages.put(c.getId(), c.getImage());
            //editor.putBoolean(Integer.toString(c.getId()), false);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.company_submit:
                checkforCompany(companyIDEditText);
                break;
            case R.id.Company_link_button:
                linkCompany(Integer.valueOf(companyIDEditText.getText().toString()));


        }
    }

    public void checkforCompany(EditText companyid) {
        if (validateForm()) {
            //do not allow integer
            companyID = Integer.valueOf(companyid.getText().toString());
            if (validCompanies.containsKey(companyID)) {
                findViewById(R.id.company_submit).setVisibility(View.GONE);
                Glide.with(this)
                        .load(mImageRef.child(companyImages.get(companyID)))
                        .into(companyLogo);
                findViewById(R.id.Company_email_password_fields).setVisibility(View.VISIBLE);
                findViewById(R.id.Company_link_button).setVisibility(View.VISIBLE);

            } else Toast.makeText(CompanySignUpActivity.this,
                    "Please enter a valid company ID",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void linkCompany(int companyID) {
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        new RegisterOrgTask();
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("company").child(Integer.toString(companyID)).setValue(true);
        Intent intent = new Intent(this, ScanTokenActivity.class);
        intent.putExtra("Company", validCompanies.get(companyID));
        intent.putExtra("Origin", "Company");
        startActivity(intent);
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

    //for register organization
    class RegisterOrgTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                //get the public key from companybackend to encrypt the info to be sent to company, the info to be sent is in JSON format
                String request_and_key = Http_Get("https://shielded-bayou-99151.herokuapp.com/get_key");

                JSONObject requestJson = new JSONObject(request_and_key);
                String str_pub_key = requestJson.get("public_key").toString();
                String request_id = requestJson.get("request_id").toString();

                byte[] publicKeyByte = BlocktraceCrypto.pemToBytes(str_pub_key);

                JSONObject register_org_info = new JSONObject();
                register_org_info.put("request_id", request_id);
                register_org_info.put("username", username);
                register_org_info.put("password", BlocktraceCrypto.hash256(password));

                JSONObject token = getToken();
                register_org_info.put("block_id", "blockid");//token.get("block_id"));
                register_org_info.put("AES_key", "aes key");//token.get("AES_key"));

                JSONObject encrypted_info = encrypt_json(register_org_info,publicKeyByte);

                //receive response on the status of registration from the company
                String result = Http_Post("https://shielded-bayou-99151.herokuapp.com/register_user", encrypted_info);

                return result;

            } catch (Exception ex) {
                return "Exception: " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //Toast.makeText(CompanySignUpActivity.this, result, Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CompanySignUpActivity.this);
            alertDialog.setTitle("Update Token");
            alertDialog.setMessage("Please update your token");
            final EditText input = new EditText(CompanySignUpActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            //alertDialog.setIcon(R.drawable.ic_verified_user_black_24dp);
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new updateTokenTask();
                        }
                    });
            alertDialog.show();
        }
    }

    class updateTokenTask extends AsyncTask<String,Void,String>{
        //JSONObject token;

        @Override
        protected String doInBackground(String... params){
            try {
                //get the public key from kyc backend for encryption of block_id and AES_key (JSONObject) to be sent
                String str_public_key = Http_Get("https://kyc-project.herokuapp.com/getkey");

                byte[] pubKeyByte = BlocktraceCrypto.pemToBytes(str_public_key);

                JSONObject update_object = new JSONObject();
                JSONObject token = getToken();
                update_object.put("AES_key",token.get("AES_key"));
                update_object.put("block_id",token.get("block_id"));

                JSONObject encrypted_info = encrypt_json(update_object, pubKeyByte);

                //receive the new AES key from kyc backend and update the token
                JSONObject new_AES_key_object = new JSONObject(Http_Post("https://kyc-project.herokuapp.com/update_token", encrypted_info));
                String new_AES_key = new_AES_key_object.get("AES_key").toString();

                return new_AES_key;
            }catch (Exception ex){
                return "Exception " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result){
            try {
                JSONObject token = getToken();
                //remove the original AES_key from token and update it with new AES_key
                token.remove("AES_key");
                token.put("AES_key",result);
                Log.i("Norman","newToken");
                Log.i("Norman",token.toString());

                String message = saveToken(token);

                Toast.makeText(CompanySignUpActivity.this, result, Toast.LENGTH_LONG).show();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
