package com.example.tessa.kyc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tessa.kyc.CompanyFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Company} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CompaniesRecyclerViewAdapter extends RecyclerView.Adapter<CompaniesRecyclerViewAdapter.ViewHolder> {

    public static Context context;
    public static List<Company> mCompanies;
    public static OnListFragmentInteractionListener mListener;

    public CompaniesRecyclerViewAdapter(Context context) {
        super();
    }

    public CompaniesRecyclerViewAdapter(Context context, List<Company> companies) {
        mCompanies = companies;
        this.context = context;
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
        int resID = CompaniesRecyclerViewAdapter.context.getResources().getIdentifier(mCompanies.get(position).image, "drawable", context.getPackageName());
        holder.mImageView.setImageResource(resID);
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
        public final View mView;
        public final ImageView mImageView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Company mItem;

        public ViewHolder(View view) {
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
}
