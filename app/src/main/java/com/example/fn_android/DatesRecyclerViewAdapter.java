package com.example.fn_android;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fn_android.databinding.FragmentDatesBinding;
import com.example.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class DatesRecyclerViewAdapter extends RecyclerView.Adapter<DatesRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;

    public DatesRecyclerViewAdapter(List<String[]> items, int fragType) {
        type = fragType;
        ourList = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        int DATES = 0;
        if (type == DATES)  // Dates Fragment
            viewHolder = new ViewHolder(FragmentDatesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else { // Default item fragment
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mDateView.setText(ourList.get(position)[1]);

    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mDateView;
        public final TextView mContentView;
        public String mItem;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mDateView = binding.itemNumber;
            mContentView = binding.content;
        }

        // Dates Fragment
        public ViewHolder(FragmentDatesBinding binding) {
            super(binding.getRoot());
            mDateView = binding.dateText;
            mContentView = binding.content;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}