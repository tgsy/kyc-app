package com.example.tessa.kyc;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * Created by tessa on 13/3/2018.
 */

public class MainLoggedInFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userID;

    private DatabaseReference mDatabase;
    private DatabaseReference usersRef;

    private FirebaseStorage storage;
    private StorageReference storageRef;
    private StorageReference islandRef;

    private TextView emailView;
    private TextView statusView;
    private TextView idView;


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

        emailView.setText(currentUser.getEmail().toString());
        idView.setText(currentUser.getUid().toString());

        if (mAuth.getCurrentUser().isEmailVerified())
            statusView.setText("[VERIFIED]");
        else
            statusView.setText("[UNVERIFIED]");

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
}
