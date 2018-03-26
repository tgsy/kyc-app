package com.example.tessa.kyc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tessa.kyc.CompanyFragment.OnListFragmentInteractionListener;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Company} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CompaniesRecyclerViewAdapter extends RecyclerView.Adapter<CompaniesRecyclerViewAdapter.ViewHolder> {

    public static Context context;
    private static List<Company> mCompanies = new ArrayList<>();
    private static OnListFragmentInteractionListener mListener;

    private static HashMap<Integer, Boolean> appliedfor = new HashMap<>();
    List<Company> list;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mImageRef = mStorageRef.child("companylogos");

    public CompaniesRecyclerViewAdapter(Context context) {
        this.context = context;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String jsonString = parseJson(R.raw.banks);
        list = Arrays.asList(new Gson().fromJson(jsonString, Company[].class));

        for (Company c: list) {
            getDataFromFirebase(c);
            if (appliedfor.isEmpty());
            else if (appliedfor.get(c.getId())) {
                mCompanies.add(c);
                notifyDataSetChanged();
            }
            Log.i("DED","size in recyclerview="+ mCompanies.size());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_company, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mCompanies.get(position);
       /* int resID = CompaniesRecyclerViewAdapter.context.getResources().getIdentifier(mCompanies.get(position).image, "drawable", context.getPackageName());
        holder.mImageView.setImageResource(resID);*/
        Glide.with(context /* context */)
                .using(new FirebaseImageLoader())
                .load(mImageRef.child(mCompanies.get(position).getImage()))
                .into(holder.mImageView);
        holder.mIdView.setText(String.valueOf(mCompanies.get(position).id+"     "+mCompanies.get(position).name));
        holder.mContentView.setText(mCompanies.get(position).description);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCompanies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView mImageView;
        private final TextView mIdView;
        private final TextView mContentView;
        private Company mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.logo);
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.description);
        }

        @Override
        public String toString() {
            return super.toString() + " " + mContentView.getText() + "'";
        }
    }

    private void getDataFromFirebase(Company c){
        final String cid = String.valueOf(c.getId());

        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("company")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(cid).getValue().toString().equalsIgnoreCase("true")) {
                            appliedfor.put(Integer.valueOf(cid), true);
                            for (Company c: list) {
                                if (String.valueOf(c.getId()).equalsIgnoreCase(cid)) {
                                    mCompanies.add(c);
                                    notifyDataSetChanged();
                                }
                            }
                        } else appliedfor.put(Integer.valueOf(cid), false);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    private String parseJson(int resource) {
        String line;
        String output = "";
        InputStream inputStream = context.getResources().openRawResource(resource);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while ((line = reader.readLine()) != null) {
                output += line;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return output;
    }
}
