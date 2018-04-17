package com.example.tessa.kyc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class WriteTokenActivity extends BaseActivity {

    NfcAdapter nfcAdapter;
    String tokenStr;

    TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_token);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent intent = getIntent();
        tokenStr = intent.getStringExtra("KEY");
        Log.i("Norman",tokenStr);
        content = (TextView) findViewById(R.id.writeToken_content);
    }

    /*
       If token is detected, write the token
    */
    @Override
    protected void onNewIntent(Intent intent){
        content.setText("Please Do Not Remove Your blocktrace Token From Your Device");
        super.onNewIntent(intent);
        String token = "";

        //if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){

        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)){
            //Toast.makeText(this,"NfcIntent!", Toast.LENGTH_LONG).show();

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            try{
                token = new updateTokenTask().execute(tokenStr).get();
                Log.i("Norman","onNewintent, token:"+token);
              /*  token.put("block_id","937efdbccc5295d88d02cad8b2eb67185273b985b7d78f95fa6ba04808117a28");
                token.put("merkle_raw","44b93080f0d701ec235ba97b0a35fd0e8ad4fc2c570816495b636214beb696b5");
                token.put("private_key", "-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCsOx8bSB/7yq8D\nDUyRSINOFRFnHssBpv4KW7D/NTactulPUBJmDMsOFu1T/3bkoQUtmI5hnraox2ov\ntauhpPUpSqAGDgoHH9H5SLULQtlmhVuNWiBVB2abgp41nQTHCfrhMmBKRN/hhV1F\n3mqZUTXxqhkFLjWL7r0siCxfMMILef9Tu0+kER10zkqy5ra8NqUd0GmFxnObE3DY\n1tqZ6yHwpvgqR0PfwmPEfwxA/cL6WPVgIXKF05E3Ml6UMUAyv3skdeVdgdMBL4+E\nekgowqjnKcJsyueVLxVxapaXA+fR6BjSZDjNl1NLiqH5NlOYpIhwjFFR7ln7rquy\nRGAIFbvXAgMBAAECggEAFWdZg8nwU5nKKx5tfAafbsY2ffK5NtDadE4Vznl9+nbO\nwtEIIE+JsowN3Wj7jQknvBVf6GjReWMi4p/4nuOBpiqEfYwkGeON9CVhfm9F1jRP\nft8K8pYzXbMbVz1WuSeX2oGorsIlcoDg6QxtgfUyN4C0kEzAUc4PC2g233OPQd6o\nnedZc/TOE5vGt1O5EPPTpCF/CBXMqaQI1dgtv08b2zOh8daRgKtHfQxYFetjT/lN\nRnVike6VxKWw5ioyOQD7XXZyRui+0YXDpgT+w3XTwKnQAVCk3/193GXPRo/61zfm\nxD5lEPua4YWJF9dgsnU5w9AuNsIkrCvAJcsL/Am19QKBgQDFNbF35fnXZ1Ad7Kdc\nS4ePmfqhW5+Aoc74Fyrkic3JQyV+2RmQgiTt1PMEAXZsGOrJyvSPI4p24hAjbK5b\nJgQer229lILC67Wc6nio81podh7YkG2q75W2czqXwJxJoJ9Ya4TNK/al6TnFyfZL\nc5DnFESTwEPQyzIwCXuV8R4ZCwKBgQDfkyPvnxcxq+6I9kMc3+UE02bKHGPfbLpH\n0gAWM+ullS1zbKplvHwHDFQYJQQyXFOuRRS3RkZIi9/QMeWMY4aPILB3tMjh1rAV\nl60DWmp2BKV99y/bxLiil2oTqlgsCkDg1dreZPVAAvP5Y5XzX48OymektUCAWNkC\ndIWoOAMf5QKBgFUXo+muK62MAH/I+zXRyT5nHEO/ewVPzDQ0GufdphOvi2A+YM9/\nuFt+xRT3ZJej8Lr9faS9myNMy9XdMSZXMvPikiF5ESr19bktWr7bsijcvtaHCyr0\nkc7VzXRpQYZrbhYC3pyA2b4g8jKrwEAyL1Xl4uk1zo0rAA7GKLM8BNadAoGAGA3R\noPdB3JM977hnEhU4o6NG/Nm/GQuuowmB/uGJKeB56mA3mQiFY5C8/3gEEpMCYc6G\n4w0JhMafxtuHcepHUODYe5iUwE+D1F7kO2cf6yCm2X2dxYxNvRiDTihWoi5cOpRn\nnuPHyyJGM3+2Y1/VmWbHbd4eWXC1sGDu/GFCXM0CgYBmOSKC7LLSJT5mizpBnzmS\nWfyeTTnmKY2aiYAG6nX7gyA/gvwYFZM+PQSQPvtbGzJvEqNlLtxN2zjGnD8bcC7N\nMH0yEjEAnWpa/qJ1qd0QfiF/Z3OuI388o8ajxD17NX4CsPXv27VxXhisJwnTUJa+\nz/3l0pxOVOU2/uoR0ksrrQ==\n-----END PRIVATE KEY-----");
                token.put("AES_key","[215, 94, 25, 236, 7, 4, 121, 162, 84, 219, 130, 14, 80, 150, 82, 108, 68, 9, 61, 46, 87, 20, 178, 216, 152, 217, 193, 87, 42, 63, 113, 143]");
                Log.i("Norman",token.toString()); */
            } catch (Exception ex) {
                Log.i("ERROR", "Onnewintent: "+ex.getMessage());
                Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }

            NdefMessage ndefMessage = createNdefMessage(token);
            writeNdefMessage(tag,ndefMessage);
        }
       // }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
        //enableForegroundDipatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
        //disableForegroundDispatchSystem();
    }

    /*
    enableForegroundDispatchSystem and disableForegroundDispatchSystem is for detecting the presence of token
    */
    private void enableForegroundDipatchSystem(){
        Intent intent = new Intent(this, WriteTokenActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,intentFilters,null);
    }

    private void disableForegroundDispatchSystem(){
        nfcAdapter.disableForegroundDispatch(this);
    }

    /*
        This method format the tag to NDEF format and write the message
     */
    private void formatTag(Tag tag, NdefMessage ndefMessage){
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if (ndefFormatable == null){
                Toast.makeText(this, "Token Update Was Unsuccessful. Please try again later.", Toast.LENGTH_LONG).show();
            }
            ndefFormatable.connect();
            ndefFormatable.format(ndefMessage);
            ndefFormatable.close();

            Toast.makeText(this, "Token Updated Successfully", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.i("ERROR","formatTag: "+e.getMessage());
            Toast.makeText(this, "Token Update Was Unsuccessful. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    /*
        Write info to the token
     */
    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage){
        try {
            if (tag == null) {
                Toast.makeText(this, "Error: Tag object cannot be null", Toast.LENGTH_LONG).show();
                return;
            }
            Ndef ndef = Ndef.get(tag);

            if (ndef == null){
                //format tag with the ndef format and writes the message
                formatTag(tag,ndefMessage);
            }
            else {
                ndef.connect();

                if (!ndef.isWritable()) {
                    Toast.makeText(this, "Error: Tag is not writable", Toast.LENGTH_LONG).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
               // new updateTokenTask().execute();
            }
        } catch (Exception e) {
            Log.i("ERROR","writendefmessage: "+e.getMessage());
            Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /*
        Creates an NDEF Record which contains typed data, such as MIME-type media, a URI, or a custom application payload.
     */
    private NdefRecord createTextRecord(String content){
        try {
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");
            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) (languageSize & 0x1F));
            payload.write(language,0,languageSize);
            payload.write(text,0,textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],payload.toByteArray());
        }
        catch (UnsupportedEncodingException ex) {
            Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
            Log.i("ERROR","createTextRecord: "+ex.getMessage());
        }
        return null;
    }

    /*
        Creates an NDEF Message which is a container for one or more NDEF Records
     */
    private NdefMessage createNdefMessage(String content) {
        NdefRecord ndefRecord = createTextRecord(content);
        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] { ndefRecord});
        return ndefMessage;
    }

   /* public void onClick(View view) {
        //if successful
        new updateTokenTask().execute();

        //else scan again
        //Toast.makeText(WriteTokenActivity.this, "Token Update was unsuccessful. Please scan your token again", Toast.LENGTH_SHORT).show();
        //finish();
    }*/

    class updateTokenTask extends AsyncTask<String,Void,String> {
        JSONObject token;
        @Override
        protected String doInBackground(String... params){
            String tokenString = params[0];
            try {
                token = new JSONObject(tokenString);
                //get the public key from kyc backend for encryption of block_id and AES_key (JSONObject) to be sent
                String str_public_key = Http_Get("https://kyc-project.herokuapp.com/getkey");

                byte[] pubKeyByte = BlocktraceCrypto.pemToBytes(str_public_key);

                JSONObject update_object = new JSONObject();
                //token = getToken(tokenStr);
                update_object.put("AES_key",token.get("AES_key"));
                update_object.put("block_id",token.get("block_id"));

                JSONObject encrypted_info = encrypt_json(update_object, pubKeyByte);

                //receive the new AES key from kyc backend and update the token
                JSONObject new_AES_key_object = new JSONObject(Http_Post("https://kyc-project.herokuapp.com/update_token", encrypted_info));
                String new_AES_key = new_AES_key_object.get("AES_key").toString();

                token.remove("AES_key");
                token.put("AES_key",new_AES_key);
                Log.i("Norman",new_AES_key);

                return token.toString();

            } catch (Exception ex){
                Log.i("ERROR","updateTokenTask: "+ex.getMessage());
                Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                return "Exception " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result){
            try {
                if (result.contains("Exception "))
                    Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                else {
                    Log.i("Norman","newToken");
                    Log.i("Norman",result);
                    Toast.makeText(WriteTokenActivity.this, "blocktrace Token Update successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainLoggedInActivity.class);
                    startActivity(intent);
                    finish();
                }
            } catch (Exception ex){
                Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }
}

