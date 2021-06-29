package com.myapp.fn_android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentEventsClpBinding;
import com.myapp.fn_android.databinding.FragmentEventsSyncdinBinding;
import com.myapp.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class EventsRecyclerViewAdapter extends RecyclerView.Adapter<EventsRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;

    public EventsRecyclerViewAdapter(List<String[]> items, int fragType) {
        type = fragType;
        ourList = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        int SYNCDIN = 0;
        int CLP = 1;
        if (type == SYNCDIN)  // SyncDIN Fragment
            viewHolder = new ViewHolder(FragmentEventsSyncdinBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else if (type == CLP) // CLP fragment
            viewHolder = new ViewHolder(FragmentEventsClpBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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

        holder.mContentView.setOnClickListener(v -> { // Navigate to detail page
            Bundle bundle = new Bundle();
            bundle.putString("eventName",holder.mContentView.getText().toString());
            Navigation.findNavController(v).navigate(R.id.eventsDetailFragment,bundle);
        });

        holder.mHoursView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventName",holder.mContentView.getText().toString());
            Navigation.findNavController(v).navigate(R.id.eventsDetailFragment,bundle);
        });
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

        // SyncDin Fragment
        public ViewHolder(FragmentEventsSyncdinBinding binding) {
            super(binding.getRoot());
            mHoursView = binding.timeText;
            mContentView = binding.content;
        }

        // CLP Fragment
        public ViewHolder(FragmentEventsClpBinding binding) {
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