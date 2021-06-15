package com.example.fn_android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fn_android.databinding.FragmentHoursBinding;
import com.example.fn_android.databinding.FragmentHoursDetailBinding;
import com.example.fn_android.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.fn_android.databinding.FragmentItemBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class HoursRecyclerViewAdapter extends RecyclerView.Adapter<HoursRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;
    private final int  HOURS = 0;
    private final int HOURS_DETAIL = 1;

    public HoursRecyclerViewAdapter(List<String[]> items, int fragType) {
        type = fragType;
        ourList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (type == HOURS)  // Hours Fragment
            viewHolder = new ViewHolder(FragmentHoursBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else if (type == HOURS_DETAIL) // Hours detail fragment
            viewHolder = new ViewHolder(FragmentHoursDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else {
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mIdView.setText(ourList.get(position)[1]);

        holder.mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity) v.getContext();
                Bundle bundle = new Bundle();
                bundle.putString("id",holder.mIdView.getText().toString());
                bundle.putString("name",holder.mContentView.getText().toString());
                HoursDetailFragment hoursDetailFragment = new HoursDetailFragment();
                hoursDetailFragment.setArguments(bundle);
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_main,hoursDetailFragment).addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {

        //return mValues.size();
        return ourList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}