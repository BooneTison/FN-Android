package com.example.fn_android;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fn_android.databinding.FragmentHoursBinding;
import com.example.fn_android.placeholder.PlaceholderContent.PlaceholderItem;
import com.example.fn_android.databinding.FragmentItemBinding;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PlaceholderItem}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    //private final List<PlaceholderItem> mValues;
    private final List<String> ourList;
    private final int type;
    private final int  HOURS = 0;

    //public MyItemRecyclerViewAdapter(List<PlaceholderItem> items) {
       // mValues = items;
   // }
    public MyItemRecyclerViewAdapter(List<String> items, int fragType) {
        type = fragType;
        ourList = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (type == HOURS) { // Hours Fragment
            return new ViewHolder(FragmentHoursBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        else {
            return new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mItem = mValues.get(position);
       // holder.mIdView.setText(mValues.get(position).id);
        //holder.mContentView.setText(mValues.get(position).content);
        holder.mItem = ourList.get(position);
        holder.mContentView.setText(ourList.get(position));
    }

    @Override
    public int getItemCount() {

        //return mValues.size();
        return ourList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //public final TextView mIdView;
        public final TextView mContentView;
        //public PlaceholderItem mItem;
        public String mItem;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            //mIdView = binding.itemNumber;
            mContentView = binding.content;
        }

        // Hours Fragment
        public ViewHolder(FragmentHoursBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}