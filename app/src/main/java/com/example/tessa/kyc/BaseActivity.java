package com.example.tessa.kyc;

/**
 * Created by tessa on 7/2/2018.
 */

import android.app.ProgressDialog;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import static com.example.tessa.kyc.BlocktraceCrypto.rsaEncrypt;

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    //post method
    public String Http_Post(String urlString, JSONObject jsonObject){
        HttpURLConnection urlConnection = null;
        try {
            //set up the connection to the URL
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            if (urlString.contains("register_kyc")){
                String encoded = Base64.encodeToString(("admin"+":"+"secret").getBytes(StandardCharsets.UTF_8),Base64.NO_WRAP);
                urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
            }

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            //write the output to the url and send JSONObject to the url
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(jsonObject.toString());
            wr.flush();

            //get the response message
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder message = new StringBuilder("");
                String line = "";

                while ((line = in.readLine())!= null){
                    message.append(line);
                }
                in.close();
                //disconnect with the url after done
                urlConnection.disconnect();

                return message.toString();
            }
            else {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                StringBuilder message = new StringBuilder("");
                String line = "";

                while ((line = in.readLine())!= null){
                    message.append(line);
                }
                in.close();
                //disconnect with the url after done
                urlConnection.disconnect();
                try {
                    JSONObject errorMessage = new JSONObject(message.toString());
                    String messageError = errorMessage.get("Error").toString();
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                return "False: " + message;
            }

        } catch (IOException ex) {
            return "Exception: " + ex.getMessage();
        }
    }

    //get method
    public String Http_Get(String urlString){
        HttpURLConnection urlConnection = null;
        try {
            //set up the connection to the URL
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            //get the response message
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder data_received = new StringBuilder("");
                String line = "";

                while ((line = in.readLine()) != null) {
                    data_received.append(line);
                }
                in.close();
                //disconnect with the url after done
                urlConnection.disconnect();

                return data_received.toString();
            } else {
                return "False: " + responseCode;
            }
        }catch (IOException ex){
            return "Exception: " + ex.getMessage();
        }
    }

    //encryption
    public JSONObject encrypt_json(JSONObject plainJson, byte[] public_key){
        JSONObject encrypted_info = new JSONObject();
        try {
            Iterator<String> keys = plainJson.keys();
            while (keys.hasNext()){
                String k = keys.next();
                if (k.equals("request_id")){
                    encrypted_info.put(k,plainJson.get(k));
                }
                else {
                    String encryptedKey = Arrays.deepToString(rsaEncrypt(k, public_key));
                    String encryptedValue = Arrays.deepToString(rsaEncrypt(plainJson.getString(k), public_key));
                    encrypted_info.put(encryptedKey, encryptedValue);
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return encrypted_info;
    }

    //save token to internal storage
    public String saveToken(JSONObject token){
        String filename = "token.json";
        try{
            FileOutputStream fileOutputStream = openFileOutput(filename,MODE_PRIVATE);
            fileOutputStream.write(token.toString().getBytes());
            fileOutputStream.close();

            Log.i("Norman",token.toString());
            return "Saved";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Not Saved";
    }

    //get the token from the file
    public JSONObject getToken(){
        String output = "";
        JSONObject jsonObject;
        try{
            String message;
            FileInputStream fileInputStream = openFileInput("token.json");
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while ((message = bufferedReader.readLine())!= null){
                output+= message;
            }
            Log.i("Norman",output);

            jsonObject = new JSONObject(output);
            Log.i("Norman",jsonObject.toString());
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
        }
        jsonObject = new JSONObject();
        try {
            jsonObject.put("Error","File is not Found");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return jsonObject;
    }

    public JSONObject getToken(String directory) throws JSONException {
        String line;
        String output = "";
        JSONObject jsonObject;
        try{
            String message;
            FileInputStream fileInputStream = new FileInputStream(new File(directory));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            while ((message = bufferedReader.readLine())!= null){
                output+= message;
            }
            Log.i("Norman",output);

            jsonObject = new JSONObject(output);
            Log.i("Norman",jsonObject.toString());
            return jsonObject;
        }catch (Exception e){
            e.printStackTrace();
        }
        jsonObject = new JSONObject();
        jsonObject.put("Error","Failed to get token");
        return jsonObject;
    }
}