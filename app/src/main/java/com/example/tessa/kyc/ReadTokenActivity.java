package com.example.tessa.kyc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ReadTokenActivity extends BaseActivity {


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
            companyID = getIntent().getIntExtra("Company ID", 1000);
            companyName = getIntent().getStringExtra("Company Name");
            username = getIntent().getStringExtra("Username");
            password = getIntent().getStringExtra("Password");
        }

    }

    //for register organization
    class RegisterOrgTask extends AsyncTask<String,Void,String> {
        JSONObject token;
        @Override
        protected String doInBackground(String... params) {
            String tokenStr = params[0];
            try {
                token = new JSONObject(tokenStr);
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

                //JSONObject token = getToken();
                register_org_info.put("block_id", token.get("block_id"));
                register_org_info.put("AES_key", token.get("AES_key"));

                JSONObject encrypted_info = encrypt_json(register_org_info,publicKeyByte);

                //receive response on the status of registration from the company
                String result = Http_Post("https://shielded-bayou-99151.herokuapp.com/register_user", encrypted_info);

                return result;

            } catch (Exception ex) {
                Log.i("ERROR", "registerorg:"+ex.getMessage());
                Toast.makeText(ReadTokenActivity.this, "Oh no, something went wrong. Please scan your blocktrace again.", Toast.LENGTH_LONG).show();
                return "Exception: " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if (!result.contains("False:") && !result.contains("Exception:")) {
                Toast.makeText(getApplicationContext(),
                        companyName + " Registration Successful",
                        Toast.LENGTH_SHORT).show();
                mDatabase.child("users").child(userID).child("company").child(Integer.toString(companyID)).setValue(true);
                Intent intent = new Intent(getApplicationContext(), WriteTokenActivity.class);
                intent.putExtra("KEY", token.toString());
                startActivity(intent);
                finish();
            } else
                Toast.makeText(getApplicationContext(),
                        "Oh no, something went wrong. Please scan your blocktrace again. Error: " +
                                result.substring(6), Toast.LENGTH_SHORT).show();
        }

    }

    //for login organization
    class LoginOrgTask extends AsyncTask<String,Void,String> {
        JSONObject token;
        String result;
        @Override
        protected String doInBackground(String... params) {
            String tokenStr = params[0];
            try {
                token = new JSONObject(tokenStr);
                //get the public key from company backend for encryption of the object to be sent to company backend
                String request_and_key = Http_Get("https://shielded-bayou-99151.herokuapp.com/get_key");

                JSONObject requestJson = new JSONObject(request_and_key);
                String str_pub_key = requestJson.get("public_key").toString();
                String request_id = requestJson.get("request_id").toString();

                byte[] publicKeyByte = BlocktraceCrypto.pemToBytes(str_pub_key);

                //JSONObject token = getToken();
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
                result = Http_Post("https://shielded-bayou-99151.herokuapp.com/login_org", encrypted_info);

                return result;

            } catch (Exception ex) {
                Log.i("ERROR","loginorgtask: "+ex.getMessage());
                return "Exception: "+ ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.contains("False:")) {
                Toast.makeText(getApplicationContext(),
                        companyName + " Login Successful",
                        Toast.LENGTH_SHORT).show();
            }

            else
                Toast.makeText(getApplicationContext(),
                        result.substring(6),
                        Toast.LENGTH_SHORT).show();

            startActivity(new Intent(ReadTokenActivity.this, MainLoggedInActivity.class));
            finish();
        }
    }


   /*
       This method is to detect the NFC Tag and perform reading token function
   */
    @Override
    protected void onNewIntent(Intent intent) {
        content.setText("Please Do Not Remove Your blocktrace Token From Your Device");
        super.onNewIntent(intent);
        String tokenStr;
        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            //Toast.makeText(this,"NfcIntent!", Toast.LENGTH_LONG).show();
            Parcelable[] parcelables;
            parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (parcelables != null && parcelables.length>0){
                tokenStr = readTextFromMessage((NdefMessage)parcelables[0]);
                try {
                    if (origin.equalsIgnoreCase("Company Login"))
                        new LoginOrgTask().execute(tokenStr);

                    else if (origin.equalsIgnoreCase("Company Registration"))
                        new RegisterOrgTask().execute(tokenStr);

                } catch (Exception ex){
                    Log.i("ERROR","onnewintent: "+ex.getMessage());
                    Toast.makeText(getApplicationContext(),
                            "Oh no, something went wrong. Please try again.",
                            Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDipatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }
    /*
        enableForegroundDispatchSystem and disableForegroundDispatchSystem is for detecting whether got token or not
     */
    private void enableForegroundDipatchSystem(){
        Intent intent = new Intent(this, ReadTokenActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }

    /*
        This method is for retrieving the info in the token
     */
    public String getTextFromNdefRecord(NdefRecord ndefRecord){
        String tagContent = null;
        try {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128)==0)?"UTF-8":"UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1, payload.length - languageSize -1, textEncoding);

        } catch (UnsupportedEncodingException ex){
            Log.i("ERROR","getTextFromNdefRecord "+ex.getMessage());
            Toast.makeText(this,
                    "Error: Unsupported Encoding", Toast.LENGTH_LONG).show();
        }
        return tagContent;
    }

    /*
        This method is to perform reading on the info received from the token
     */
    private String readTextFromMessage(NdefMessage ndefMessage){
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        String tagContent = "";
        if (ndefRecords != null && ndefRecords.length>0){
            NdefRecord ndefRecord = ndefRecords[0];
            tagContent = getTextFromNdefRecord(ndefRecord);
            //content.setText(tagContent);
        }
        else{
            //Toast.makeText(this,"No NDEF records found!",Toast.LENGTH_LONG).show();
            Toast.makeText(this,"Error: No NDEF records found",Toast.LENGTH_LONG).show();
        }
        return tagContent;
    }
}
