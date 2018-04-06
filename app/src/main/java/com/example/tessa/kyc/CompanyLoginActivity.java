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
    }

    public void onClick(View view) {
        if (view.getId() == R.id.CompanyLogin_button) {

            //new LoginOrgTask().execute();

            Intent intent = new Intent(this, ReadTokenActivity.class);
            intent.putExtra("Origin", "Company Login");
            intent.putExtra("Company Name", companyName);
            intent.putExtra("Username", usernameEditText.getText().toString());
            intent.putExtra("Password", passwordEditText.getText().toString());
            startActivity(intent);


        }
    }

    //for login organization
    class LoginOrgTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                //get the public key from company backend for encryption of the object to be sent to company backend
                String request_and_key = Http_Get("https://shielded-bayou-99151.herokuapp.com/get_key");

                JSONObject requestJson = new JSONObject(request_and_key);
                String str_pub_key = requestJson.get("public_key").toString();
                String request_id = requestJson.get("request_id").toString();

                byte[] publicKeyByte = BlocktraceCrypto.pemToBytes(str_pub_key);

                JSONObject token = getToken();
                String merkle_raw = token.get("merkle_raw").toString();
                Log.i("Norman",merkle_raw);

                //get user private key and encrypt the merkle root
                byte[] userPrivateKeyByte = BlocktraceCrypto.pemToBytes(token.get("private_key").toString());

                String encryptedMerkle = Arrays.toString(BlocktraceCrypto.sign(merkle_raw,userPrivateKeyByte));

                JSONObject loginObject = new JSONObject();
                loginObject.put("username", usernameEditText.getText().toString());
                loginObject.put("password", BlocktraceCrypto.hash256(passwordEditText.getText().toString()));
                loginObject.put("request_id", request_id);
                loginObject.put("block_id",token.get("block_id"));
                loginObject.put("merkle_raw",encryptedMerkle);

                Log.i("Norman","happy");
                Log.i("Norman",encryptedMerkle);
                Log.i("Norman", loginObject.toString());

                JSONObject encrypted_info = encrypt_json(loginObject,publicKeyByte);

                //receive the response from company backend to indicate the status of login
                String result = Http_Post("https://shielded-bayou-99151.herokuapp.com/login_org", encrypted_info);

                return result;

            } catch (Exception ex) {
                return "Exception: "+ ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(CompanyLoginActivity.this, result, Toast.LENGTH_LONG).show();
        }

    }

}
