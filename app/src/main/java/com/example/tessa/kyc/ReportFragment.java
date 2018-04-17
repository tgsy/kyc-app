package com.example.tessa.kyc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

import static com.example.tessa.kyc.BlocktraceCrypto.rsaEncrypt;

public class ReportFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);
        Button reportLoss = (Button) v.findViewById(R.id.report_loss_button);
        reportLoss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Report Loss of Token");
                alertDialog.setMessage(getResources().getString(R.string.token_loss_prompt));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                lp.setMargins(16, 16,16, 16);
                final EditText input = new EditText(getContext());
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setIcon(R.drawable.ic_report_black_24dp);
                alertDialog.setPositiveButton("REPORT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        Log.i("input", input.getText().toString());
                                        if (TextUtils.isEmpty(input.getText().toString())) {
                                            Log.i("input", input.getText().toString());
                                            input.setError("Required");
                                        }
                                        else {
                                            Log.i("input", input.getText().toString().toUpperCase());
                                            new reportTokenLost().execute(input.getText().toString().toUpperCase());
                                        }
                                }

                            }
                        });
                alertDialog.setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alertDialog.show();
            }
        });
        return v;
    }

    class reportTokenLost extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params){

            try {
                //get the public key for encryption of info to be sent (JSONObject)
                String str_public_key = Http_Get("https://kyc-project.herokuapp.com/getkey");

                byte[] pubKeyByte = BlocktraceCrypto.pemToBytes(str_public_key);

                JSONObject nric = new JSONObject();
                nric.put("block_id", BlocktraceCrypto.hash256(params[0]));

                JSONObject encrypted_info = encrypt_json(nric, pubKeyByte);
                String resp = Http_Post("https://kyc-project.herokuapp.com/token_lost", encrypted_info);
                //receive the response from kyc backend
                JSONObject message;
                if (resp.contains("False:")) {
                    message = new JSONObject(resp.substring(6));
                    return "False: "+message.toString();
                }
                else if (resp.contains("Exception:")){
                    message = new JSONObject(resp.substring(10));
                    return "Exception: "+message.toString();
                }
                else
                    message = new JSONObject(resp);

                return message.toString();
            } catch (Exception ex) {
                Log.i("ERROR",ex.getMessage());
                return "Exception: " + ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("input", result);
            if (!result.contains("Exception") &&
                    !result.contains("False: ")) {
                Toast.makeText(getContext(), "Report Submission Successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
            else
                Toast.makeText(getContext(), "Report Submission Was Unsuccessful, Please try again later.", Toast.LENGTH_SHORT).show();
        }
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
                return "False: " + responseCode;
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
}
