package com.example.tessa.kyc;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tessa on 13/3/2018.
 */

public class MainLoggedInFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userID;

    private DatabaseReference mDatabase;
    private DatabaseReference usersRef;
    private DatabaseReference statusRef;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference islandRef;

    private TextView emailView;
    private TextView statusView;
    private TextView idView;
    private TextView tokenStatusView;

    HashMap<Integer, String> status;

    String fileFullPath;
    String packageName = "com.example.tessa.kyc";
    ApplicationInfo appInfo;
    private static final String filePath = "/files/token.json";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        usersRef = mDatabase.child("users").child(userID);
        statusRef = usersRef.child("status");

        status = new HashMap<>();
        status.put(0, "Pending Verification");
        status.put(1, "Pending Token Generation");
        status.put(2, "Verified Customer");
        status.put(3, "Lost Token");

        downloadFile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mainloggedin, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailView = (TextView) view.findViewById(R.id.MainLog_Email_TextView);
        statusView = (TextView) view.findViewById(R.id.MainLog_Status_TextView);
        idView = (TextView) view.findViewById(R.id.MainLog_ID_TextView);
        tokenStatusView = (TextView) view.findViewById(R.id.MainLog_TokenStatus);

        emailView.setText(currentUser.getEmail().toString());
        idView.setText(currentUser.getUid().toString());

        if (mAuth.getCurrentUser().isEmailVerified())
            statusView.setText("[VERIFIED]");
        else
            statusView.setText("[UNVERIFIED]");

        statusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("DATASNAPSHOT", "getvalue: "+dataSnapshot.getValue());
                if (dataSnapshot.equals(2)) {
                    PackageManager packageManager = getActivity().getPackageManager();
                    try {
                        appInfo = packageManager.getApplicationInfo(packageName,PackageManager.GET_META_DATA);

                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                    fileFullPath = appInfo.dataDir + filePath;
                    JSONObject token = getToken(fileFullPath);
                    String message = saveToken(token);
                }
                tokenStatusView.setText(status.get(Integer.valueOf(dataSnapshot.getValue().toString())));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void downloadFile() {
        islandRef = storageRef.child("raw/banks.json");

        File rootPath = new File(Environment.getExternalStorageDirectory(), "blocktrace");

        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath,"banks.json");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ",";local tem file created  created " +localFile.toString());
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ",";local tem file not created  created " +exception.toString());
            }
        });
    }

    public void incomingToken() {
        PackageManager packageManager = getActivity().getPackageManager();
        try {
            appInfo = 	packageManager.getApplicationInfo(packageName,PackageManager.GET_META_DATA);

        } catch (Exception ex){
            ex.printStackTrace();
        }
        fileFullPath = appInfo.dataDir + filePath;
        JSONObject token = null;
        token = getToken(fileFullPath);
        String message = saveToken(token);
    }

    public JSONObject getToken(String directory){
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
        try {

            jsonObject.put("Error","Failed to get token");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //save token to internal storage
    public String saveToken(JSONObject token){
        String filename = "token.json";
        try{
            FileOutputStream fileOutputStream = getActivity().openFileOutput(filename,MODE_PRIVATE);
            fileOutputStream.write(token.toString().getBytes());
            fileOutputStream.close();

            Log.i("Norman",token.toString());
            return "Saved";
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Not Saved";
    }


}
