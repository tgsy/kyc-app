package com.example.tessa.kyc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by tessa on 13/3/2018.
 */

public class MainLoggedInFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userID;

    private TextView emailView;
    private TextView statusView;
    private TextView idView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userID = currentUser.getUid();
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
}
