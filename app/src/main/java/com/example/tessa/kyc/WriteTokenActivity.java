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
import android.os.Bundle;
import android.util.Log;
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

        if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            try{
                token = new updateTokenTask().execute(tokenStr).get();
                Log.i("Norman","onNewintent, token:"+token);
            } catch (Exception ex) {
                Log.i("ERROR", "Onnewintent: "+ex.getMessage());
                Toast.makeText(WriteTokenActivity.this, "Oh no, something went wrong. Please try again.", Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }

            NdefMessage ndefMessage = createNdefMessage(token);
            writeNdefMessage(tag,ndefMessage);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
        nfcAdapter.enableForegroundDispatch(this,pendingIntent,null,null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
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

