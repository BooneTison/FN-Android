package com.myapp.fn_android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentAthleticsBinding;
import com.myapp.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class AthleticsRecyclerViewAdapter extends RecyclerView.Adapter<AthleticsRecyclerViewAdapter.ViewHolder> {

    private List<String[]> ourList;
    private final int type;
    private final AthleticsFragment fragment;

    public AthleticsRecyclerViewAdapter(List<String[]> items, int fragType, AthleticsFragment fragment) {
        type = fragType;
        ourList = items;
        this.fragment =fragment;
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

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.itemNumber;
            mContentView = binding.content;
            mSport = "";
        }

        // Athletics Fragment
        public ViewHolder(FragmentAthleticsBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.timeText;
            mContentView = binding.content;
            mSport = "";
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}