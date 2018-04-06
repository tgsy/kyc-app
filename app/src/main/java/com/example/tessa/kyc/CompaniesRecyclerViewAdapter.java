package com.example.tessa.kyc;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CompaniesRecyclerViewAdapter extends RecyclerView.Adapter<CompaniesRecyclerViewAdapter.ViewHolder> { //FirebaseRecyclerAdapter {

    public static Context context;
    private static List<Company> mCompanies;

    private static HashMap<Integer, Boolean> appliedfor;
    List<Company> list;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private StorageReference mImageRef = mStorageRef.child("companylogos");

    private static final File FILE = new File("/storage/emulated/0/blocktrace/banks.json");

   /* private static Query query = FirebaseDatabase.getInstance()
            .getReference()
                .child("company")
                .limitToLast(50);

    private static FirebaseRecyclerOptions<Company> options = new FirebaseRecyclerOptions.Builder<Company>()
            .setQuery(query, Company.class)
                .build();*/

    public CompaniesRecyclerViewAdapter(Context context) {
        /*super(options);*/

        this.context = context;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mCompanies = new ArrayList<>();
        appliedfor = new HashMap<>();

        //String jsonString = parseJson(R.raw.banks);
        String jsonString = parseJson(FILE);
        Log.i("DED","jsonstring: "+ jsonString);

        list = Arrays.asList(new Gson().fromJson(jsonString, Company[].class));

        Log.i("DED", list.size()+"size");
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mCompanies.get(position);
       /* int resID = CompaniesRecyclerViewAdapter.context.getResources().getIdentifier(mCompanies.get(position).image, "drawable", context.getPackageName());
        holder.mImageView.setImageResource(resID);*/
        Glide.with(context /* context */)
                .load(mImageRef.child(mCompanies.get(position).getImage()))
                .into(holder.mImageView);
        holder.mIdView.setText(String.valueOf(mCompanies.get(position).id+"     "+mCompanies.get(position).name));
        holder.mContentView.setText(mCompanies.get(position).description);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CompanyLoginActivity.class);
                intent.putExtra("Company Name", holder.mItem.getName());
                intent.putExtra("Company ID", holder.mItem.getId());
                intent.putExtra("Company Logo", holder.mItem.getImage());
                context.startActivity(intent);
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
                                if (String.valueOf(c.getId()).equalsIgnoreCase(cid) && !mCompanies.contains(c)) {
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

    private String parseJson(File file) {
        String line;
        String output = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
            try {
                while ((line = reader.readLine()) != null) {
                    output += line;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return output;
    }
}
