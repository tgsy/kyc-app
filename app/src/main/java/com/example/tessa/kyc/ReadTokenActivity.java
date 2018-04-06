package com.example.tessa.kyc;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.Arrays;

public class ReadTokenActivity extends BaseActivity {

    JSONObject token;
    JSONObject tokenjson;
    PublicKey org_key;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private NfcAdapter nfcAdapter;
    private TextView content;

    private String origin;
    private String username;
    private String password;
    private String userID;
    private int companyID;
    private String companyName;

    final String getKeyURL = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_token);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        content = (TextView) findViewById(R.id.readToken_content); //for displaying the result (for debugging only)

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userID = mAuth.getCurrentUser().getUid();

        origin = getIntent().getStringExtra("Origin");

        if (origin.equalsIgnoreCase("Company Login") ||
                origin.equalsIgnoreCase("Company Registration")) {
            companyName = getIntent().getStringExtra("Company Name");
            username = getIntent().getStringExtra("Username");
            password = getIntent().getStringExtra("Password");
        }
    }

    public void onClick(View view) {
        if (origin.equalsIgnoreCase("Company Login")) {
            //if token gives correct keys
            //new LoginOrgTask().execute();

            Intent intent = new Intent(this, MainLoggedInActivity.class);
            startActivity(intent);

            Toast.makeText(this,
                    "Login to "+companyName+" Successful",
                    Toast.LENGTH_SHORT).show();
            finish();
            //else not successful
        }

        else if (origin.equals("Company Registration")) {
            //if token gives correct keys
            //new RegisterOrgTask().execute();

            Intent intent = new Intent(this, MainLoggedInActivity.class);
            startActivity(intent);

            Toast.makeText(this,
                    companyName+" Registration Successful",
                    Toast.LENGTH_SHORT).show();
            finish();
            //else not successful
        }

        else {
            //check if tag gives correct keys
            Intent intent = new Intent(this, MainLoggedInActivity.class);
            intent.putExtra("E-mail", getIntent().getStringExtra("E-mail"));
            intent.putExtra("ID", getIntent().getStringExtra("ID"));
            startActivity(intent);

            Toast.makeText(this,
                    "Login to blocktrace Successful",
                    Toast.LENGTH_SHORT).show();
            finish();
            //else logout
            /*mAuth.signOut();
            Intent intent = new Intent (this, LoginActivity.class);
            startActivity(intent);
            finish();*/
        }
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
            Toast.makeText(ReadTokenActivity.this, result, Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReadTokenActivity.this);
            alertDialog.setTitle("Update Token");
            alertDialog.setMessage("Please update your token");
            final EditText input = new EditText(ReadTokenActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            alertDialog.setIcon(R.drawable.ic_verified_user_black_24dp);
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent (getParent(), WriteTokenActivity.class);
                            startActivity(intent);
                        }
                    });
            alertDialog.show();
            mDatabase.child("users").child(userID).child("company").child(Integer.toString(companyID)).setValue(true);
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
                loginObject.put("username", username);
                loginObject.put("password", BlocktraceCrypto.hash256(password));
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
            Toast.makeText(ReadTokenActivity.this, result, Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ReadTokenActivity.this);
            alertDialog.setTitle("Update Token");
            alertDialog.setMessage("Please update your token");
            final EditText input = new EditText(ReadTokenActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            alertDialog.setIcon(R.drawable.ic_verified_user_black_24dp);
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent (getParent(), WriteTokenActivity.class);
                            startActivity(intent);
                        }
                    });
            alertDialog.show();
        }

    }

//    /*
//       This method is to detect the NFC Tag and perform reading token function
//   */
//    @Override
//    protected void onNewIntent(Intent intent){
//        super.onNewIntent(intent);
//
//        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
//            Toast.makeText(this,"NfcIntent!", Toast.LENGTH_LONG).show();
//            Parcelable[] parcelables;
//            parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            if (parcelables != null && parcelables.length>0){
//                readTextFromMessage((NdefMessage)parcelables[0]);
//            }
//        }
//    }
//
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        enableForegroundDipatchSystem();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        disableForegroundDispatchSystem();
//    }
//    /*
//        enableForegroundDispatchSystem and disableForegroundDispatchSystem is for detecting whether got token or not
//     */
//    private void enableForegroundDipatchSystem(){
//        Intent intent = new Intent(this, ReadTokenActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        IntentFilter[] intentFilters = new IntentFilter[] {};
//        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
//    }
//
//    private void disableForegroundDispatchSystem(){
//        nfcAdapter.disableForegroundDispatch(this);
//    }
//
//    /*
//        This method is for retrieving the info in the token
//     */
//    public String getTextFromNdefRecord(NdefRecord ndefRecord){
//        String tagContent = null;
//        try {
//            byte[] payload = ndefRecord.getPayload();
//            String textEncoding = ((payload[0] & 128)==0)?"UTF-8":"UTF-16";
//            int languageSize = payload[0] & 0063;
//            tagContent = new String(payload, languageSize + 1, payload.length - languageSize -1, textEncoding);
//
//        }catch (UnsupportedEncodingException ex){
//            Log.e("getTextFromNdefRecord",ex.getMessage(),ex);
//        }
//        return tagContent;
//    }
//
//    /*
//        This method is to perform reading on the info received from the token
//     */
//    private void readTextFromMessage(NdefMessage ndefMessage){
//        NdefRecord[] ndefRecords = ndefMessage.getRecords();
//        if (ndefRecords != null && ndefRecords.length>0){
//            NdefRecord ndefRecord = ndefRecords[0];
//            String tagContent = getTextFromNdefRecord(ndefRecord);
//            content.setText(tagContent);
//        }
//        else{
//            Toast.makeText(this,"No NDEF records found!",Toast.LENGTH_LONG).show();
//        }
//    }


}
