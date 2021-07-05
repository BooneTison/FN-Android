package com.myapp.fn_android;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentAthleticsBinding;
import com.myapp.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class AthleticsRecyclerViewAdapter extends RecyclerView.Adapter<AthleticsRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;

    public AthleticsRecyclerViewAdapter(List<String[]> items, int fragType) {
        type = fragType;
        ourList = items;
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mHoursView.setText(ourList.get(position)[1]);
    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mHoursView;
        public final TextView mContentView;
        public String mItem;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.itemNumber;
            mContentView = binding.content;
        }

        // Athletics Fragment
        public ViewHolder(FragmentAthleticsBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.timeText;
            mContentView = binding.content;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}