package com.example.tessa.kyc;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by tessa on 13/3/2018.
 */

public class ReportFragment extends Fragment implements View.OnClickListener {

    Button reportLoss;
    Button reportFound;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_report, container, false);
        reportLoss = (Button) v.findViewById(R.id.report_loss_button);
        reportFound = (Button) v.findViewById(R.id.report_found_button);
        reportLoss.setOnClickListener(this);
        reportFound.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.report_loss_button:
                intent = new Intent(getActivity(), MainLoggedInActivity.class);
                Toast.makeText(getActivity(),
                        "Submission Successful",
                        Toast.LENGTH_SHORT).show();
                break;

            case R.id.report_found_button:
                intent = new Intent(getActivity(), ScanTokenActivity.class);
                intent.putExtra("Origin", "Report");
                break;
        }
        startActivity(intent);
    }

    /*@Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.StartButton:

            ...

                break;
        }
    }*/
}
