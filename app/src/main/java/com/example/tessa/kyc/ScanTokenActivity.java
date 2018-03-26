package com.example.tessa.kyc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.net.ssl.HttpsURLConnection;

public class ScanTokenActivity extends AppCompatActivity implements DialogListener {

    JSONObject token;
    JSONObject tokenjson;
    PublicKey org_key;

    public static final String TAG = MainActivity.class.getSimpleName();
    final String DED = "DED";

    final String getKeyURL = null;

    private WriteNFCFragment mWriteNfcFragment;
    private ReadNFCFragment mReadNfcFragment;
    private NfcAdapter mNfcAdapter;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scan_token);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (getIntent().getExtras().getString("Origin").equalsIgnoreCase("Company")) {
            showReadFragment();

        } else if (getIntent().getExtras().getString("Origin").equalsIgnoreCase("Report")) {
        }

    }
    public void onClick(View view) {
        if (getIntent().getExtras().getString("Origin").equalsIgnoreCase("Company")) {
            Toast.makeText(ScanTokenActivity.this,
                    "Sign Up for " + getIntent().getExtras().getString("Company") + " successful",
                    Toast.LENGTH_SHORT).show();

        } else if (getIntent().getExtras().getString("Origin").equalsIgnoreCase("Report")) {
            Toast.makeText(ScanTokenActivity.this,
                    "Report Successful",
                    Toast.LENGTH_SHORT).show();
        }

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

    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed = false;
        isWrite = false;
    }

    private void showReadFragment() {
        mReadNfcFragment = (ReadNFCFragment) getFragmentManager().findFragmentByTag(ReadNFCFragment.TAG);

        if (mReadNfcFragment == null) {

            mReadNfcFragment = ReadNFCFragment.newInstance();
        }
        mReadNfcFragment.show(getFragmentManager(),ReadNFCFragment.TAG);
    }

    private void showWriteFragment() {

        isWrite = true;

        mWriteNfcFragment = (WriteNFCFragment) getFragmentManager().findFragmentByTag(WriteNFCFragment.TAG);

        if (mWriteNfcFragment == null) {

            mWriteNfcFragment = WriteNFCFragment.newInstance();
        }
        mWriteNfcFragment.show(getFragmentManager(),WriteNFCFragment.TAG);

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            Toast.makeText(this, getString(R.string.nfc_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {

                if (isWrite) {

                    //String messageToWrite = mEtMessage.getText().toString();
                    //mNfcWriteFragment = (NFCWriteFragment) getFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    //mNfcWriteFragment.onNfcDetected(ndef,messageToWrite);

                } else {
                    mReadNfcFragment = (ReadNFCFragment)getFragmentManager().findFragmentByTag(ReadNFCFragment.TAG);
                    mReadNfcFragment.onNfcDetected(ndef);
                }
            }
        }
    }
}
