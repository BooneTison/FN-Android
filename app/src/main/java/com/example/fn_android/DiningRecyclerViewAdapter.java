package com.example.fn_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fn_android.databinding.FragmentDiningBinding;
import com.example.fn_android.databinding.FragmentDiningDetailBinding;
import com.example.fn_android.databinding.FragmentHoursDetailBinding;
import com.example.fn_android.databinding.FragmentItemBinding;
import com.example.fn_android.placeholder.PlaceholderContent.PlaceholderItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DiningRecyclerViewAdapter extends RecyclerView.Adapter<DiningRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;

    public DiningRecyclerViewAdapter(List<String[]> items, int fragType) {
        type = fragType;
        ourList = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        int DINING = 0;
        int DINING_DETAIL = 1;
        if (type == DINING)  // Dining Fragment
            viewHolder = new ViewHolder(FragmentDiningBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else if (type == DINING_DETAIL) // Dining detail fragment
            viewHolder = new ViewHolder(FragmentDiningDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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

        holder.mContentView.setOnClickListener(v -> {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            Bundle bundle = new Bundle();
            bundle.putString("id",holder.mIdView.getText().toString());
            bundle.putString("name",holder.mContentView.getText().toString());
            DiningDetailFragment diningDetailFragment = new DiningDetailFragment();
            diningDetailFragment.setArguments(bundle);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_main,diningDetailFragment).addToBackStack(null).commit();
        });
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

        // Dining Fragment
        public ViewHolder(FragmentDiningBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        // Hours Detail Fragment
        public ViewHolder(FragmentDiningDetailBinding binding) {
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