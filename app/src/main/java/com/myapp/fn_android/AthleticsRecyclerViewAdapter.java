package com.myapp.fn_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentAthleticsBinding;
import com.myapp.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class AthleticsRecyclerViewAdapter extends RecyclerView.Adapter<AthleticsRecyclerViewAdapter.ViewHolder> {

    private List<String[]> ourList;
    private final int type;
    private final AthleticsFragment fragment;
    private final Context context;

    public AthleticsRecyclerViewAdapter(List<String[]> items, int fragType, AthleticsFragment fragment, Context context) {
        type = fragType;
        ourList = items;
        this.fragment = fragment;
        this.context = context;
    }

    // method for filtering our recyclerview items.
    public void filterList(List<String[]> filterllist) {
        // below line is to add our filtered
        // list in our course array list.
        ourList = filterllist;
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        int ATHLETICS = 0;
        if (type == ATHLETICS)  // Athletics Fragment
            viewHolder = new ViewHolder(FragmentAthleticsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else { // Default item fragment
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mHoursView.setText(ourList.get(position)[1]);
        holder.mSport = ourList.get(position)[2];
        holder.mURL = ourList.get(position)[3];

        // Filter by sport
        if (holder.mSport.equals("")) {
            holder.mContentView.setOnClickListener(v -> {
                fragment.filterFromAdapter(holder.mContentView.getText().toString());
                if (holder.mHoursView.getText().toString().equals("on")) holder.mHoursView.setText("off");
                else holder.mHoursView.setText("on");
            });

            holder.mHoursView.setOnClickListener(v -> {
                fragment.filterFromAdapter(holder.mContentView.getText().toString());
                if (holder.mHoursView.getText().toString().equals("on")) holder.mHoursView.setText("off");
                else holder.mHoursView.setText("on");
            });
        }

        // Open results web page
        if (!holder.mURL.equals("")) {
            holder.mContentView.setOnClickListener(v -> {
                String url = "https://furmanpaladins.com" + holder.mURL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            });
            holder.mHoursView.setOnClickListener(v -> {
                String url = "https://furmanpaladins.com" + holder.mURL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            });
        }
    }


    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mHoursView;
        public final TextView mContentView;
        public String mItem;
        public String mSport;
        public String mURL;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.itemNumber;
            mContentView = binding.content;
            mSport = "";
            mURL = "";
        }

        // Athletics Fragment
        public ViewHolder(FragmentAthleticsBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.timeText;
            mContentView = binding.content;
            mSport = "";
            mURL = "";
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}