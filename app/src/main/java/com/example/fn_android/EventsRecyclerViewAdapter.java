package com.example.fn_android;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fn_android.databinding.FragmentDiningDetailBinding;
import com.example.fn_android.databinding.FragmentEventsClpBinding;
import com.example.fn_android.databinding.FragmentEventsSyncdinBinding;
import com.example.fn_android.databinding.FragmentItemBinding;

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

        /*holder.mContentView.setOnClickListener(v -> { // Navigate to detail page
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            Bundle bundle = new Bundle();
            bundle.putString("id",holder.mIdView.getText().toString());
            bundle.putString("name",holder.mContentView.getText().toString());
            DiningDetailFragment diningDetailFragment = new DiningDetailFragment();
            diningDetailFragment.setArguments(bundle);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.activity_main,diningDetailFragment).addToBackStack(null).commit();
        });*/
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