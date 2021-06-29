package com.myapp.fn_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentHoursBinding;
import com.myapp.fn_android.databinding.FragmentHoursDetailBinding;
import com.myapp.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class HoursRecyclerViewAdapter extends RecyclerView.Adapter<HoursRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;

    int HOURS = 0;
    int HOURS_DETAIL = 1;

    public HoursRecyclerViewAdapter(List<String[]> items, int fragType) {
        type = fragType;
        ourList = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (type == HOURS)  // Hours Fragment
            viewHolder = new ViewHolder(FragmentHoursBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else if (type == HOURS_DETAIL) // Hours detail fragment
            viewHolder = new ViewHolder(FragmentHoursDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else { // Default item fragment
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mIdView.setText(ourList.get(position)[1]);

        if (type == HOURS) {
            holder.mContentView.setOnClickListener(v -> { // Navigate to hours detail page
                Bundle bundle = new Bundle();
                bundle.putString("id", holder.mIdView.getText().toString());
                bundle.putString("name", holder.mContentView.getText().toString());
                Navigation.findNavController(v).navigate(R.id.hoursDetailFragment,bundle);
            });
        }
    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public String mItem;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        // Hours Fragment
        public ViewHolder(FragmentHoursBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        // Hours Detail Fragment
        public ViewHolder(FragmentHoursDetailBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}