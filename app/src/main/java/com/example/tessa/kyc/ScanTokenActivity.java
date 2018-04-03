package com.example.tessa.kyc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

public class ScanTokenActivity extends AppCompatActivity {//implements DialogListener {

    JSONObject token;
    JSONObject tokenjson;
    PublicKey org_key;

    Tag TAG;
    final String DED = "DED";
    public static final String MIME_TEXT_PLAIN = "text/plain";

    final String getKeyURL = null;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_token);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(ScanTokenActivity.this,
                    "This device does not support NFC",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        else if (!mNfcAdapter.isEnabled()) {
            Toast.makeText(ScanTokenActivity.this,
                    "NFC is disabled. Please allow it in device settings and try again",
                    Toast.LENGTH_SHORT).show();
        }
        Log.i("NFC", "on create");

    }

    public void onResume()
    {
        Log.i("NFC", "on resume");
        super.onResume();
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            TAG = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            try {
                //write("this is payload text", TAG);
                finish();
            }
            catch(Exception e)
            {

            }
        }
    }

    private NdefRecord createRecord(String mimeType, String text) throws UnsupportedEncodingException {
        Log.i("NFC", "createrec");

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "my/type".getBytes(Charset.forName("US-ASCII")), new byte[0], text.getBytes(Charset.forName("US-ASCII")));
        return recordNFC;
    }

    private void write(String mimeType, String text, Tag tag) throws IOException, FormatException {
        Log.i("NFC", "writec");

        NdefRecord[] records = { createRecord(mimeType, text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, MainLoggedInActivity.class);
        startActivity(intent);
    }

//    class getKeyTask extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... params){
//            //URL to call
//            String urlString = params[0];
//            HttpURLConnection urlConnection = null;
//
//            try {
//                URL url = new URL(urlString);
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//                //set the request method to Post
//                urlConnection.setRequestMethod("GET");
//
//                int responseCode = urlConnection.getResponseCode();
//
//
//                if (responseCode == HttpsURLConnection.HTTP_OK){
//                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                    StringBuilder sb = new StringBuilder("");
//                    String line = "";
//
//                    while ((line = in.readLine())!=null){
//                        sb.append(line);
//                    }
//                    in.close();
//                    Log.i(DED,sb.toString());
//                    return sb.toString();
//                }
//                else {
//                    return new String("false : " + responseCode);
//                }
//                // urlConnection.connect();
//            }catch (Exception ex){
//                return new String("Exception: " + ex.getMessage());
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            Toast.makeText(ScanTokenActivity.this, result, Toast.LENGTH_LONG).show();
//
//            try {
//                tokenjson = new JSONObject(result);
//                Log.i(DED, tokenjson.toString());
//                Log.i(DED, tokenjson.get("request_id").toString());
//                byte[] publicBytes = Base64.decode(result,Base64.DEFAULT);
//                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
//                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//                org_key = keyFactory.generatePublic(keySpec);
//                Log.i(DED,"done");
//
//            } catch (Exception ex){
//                ex.printStackTrace();
//            }
//
//        }
//
//    }

//    class PostTask extends AsyncTask<String,Void,String> {
//        @Override
//        protected String doInBackground(String... params) {
//            //URL to call
//            String urlString = params[0];
//
//            HttpURLConnection urlConnection = null;
//
//            try {
//                URL url = new URL(urlString);
//
//                JSONObject jsonObject = new JSONObject();
//
//                /*jsonObject.put("name",nameView.getText().toString());
//                jsonObject.put("postal_code", postalCodeView.getText().toString());
//                jsonObject.put("id_number",identifNoView.getText().toString());
//                jsonObject.put("dob",ddView.getText().toString()
//                        +"/"+mmView.getText().toString()
//                        +"/"+yyyyView.getText().toString());
//
//                Log.e(TAG,jsonObject.toString());*/
//
//                urlConnection = (HttpURLConnection) url.openConnection();
//                //set the request method to Post
//                urlConnection.setRequestMethod("POST");
//                urlConnection.setRequestProperty("Content-Type","application/json");
//                urlConnection.setDoInput(true);
//                urlConnection.setDoOutput(true);
//
//
//                //output the stream to the server
//                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.
//                        getOutputStream());
//                wr.write(jsonObject.toString());
//                wr.flush();
//
//                int responseCode = urlConnection.getResponseCode();
//
//
//                if (responseCode == HttpsURLConnection.HTTP_OK){
//                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
//                    StringBuilder sb = new StringBuilder("");
//                    String line = "";
//
//                    while ((line = in.readLine())!=null){
//                        sb.append(line);
//                    }
//                    in.close();
//                    Log.i(DED,sb.toString());
//                    return sb.toString();
//                }
//                else {
//                    return new String("false : " + responseCode);
//                }
//                // urlConnection.connect();
//            }catch (Exception ex){
//                return new String("Exception: " + ex.getMessage());
//            }
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            Toast.makeText(ScanTokenActivity.this, result, Toast.LENGTH_LONG).show();
//            try {
//                token = new JSONObject(result);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//
//    }


}
