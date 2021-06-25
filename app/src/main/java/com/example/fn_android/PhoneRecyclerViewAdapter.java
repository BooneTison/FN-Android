package com.example.fn_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fn_android.databinding.FragmentItemBinding;
import com.example.fn_android.databinding.FragmentPhoneBinding;

import java.util.List;

public class PhoneRecyclerViewAdapter extends RecyclerView.Adapter<PhoneRecyclerViewAdapter.ViewHolder> {

    private List<String[]> ourList;
    private final int type;
    private final Context context;

    int PHONE = 0;

    public PhoneRecyclerViewAdapter(List<String[]> items, int fragType, Context context) {
        type = fragType;
        ourList = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (type == PHONE)  // Phone Fragment
            viewHolder = new ViewHolder(FragmentPhoneBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else { // Default item fragment
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        return viewHolder;
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

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mNumberView.setText(ourList.get(position)[1]);

        holder.mContentView.setOnClickListener(v -> { // Phone call TODO - Check if this is working
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String s = holder.mNumberView.getText().toString();
            s = s.substring(0,3) + s.substring(4,7) + s.substring(8);
            s = "tel:" + s;
            callIntent.setData(Uri.parse(s));

            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(callIntent);
        });

        holder.mPhoneButton.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String s = holder.mNumberView.getText().toString();
            s = s.substring(0,3) + s.substring(4,7) + s.substring(8);
            s = "tel:" + s;
            callIntent.setData(Uri.parse(s));

            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(callIntent);
        });

        holder.mNumberView.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            String s = holder.mNumberView.getText().toString();
            s = s.substring(0,3) + s.substring(4,7) + s.substring(8);
            s = "tel:" + s;
            callIntent.setData(Uri.parse(s));

            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            context.startActivity(callIntent);
        });

    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mNumberView;
        public final TextView mContentView;
        public String mItem;
        public ImageButton mPhoneButton;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mNumberView = binding.itemNumber;
            mContentView = binding.content;
            mPhoneButton = null;
        }

        // Phone Fragment
        public ViewHolder(FragmentPhoneBinding binding) {
            super(binding.getRoot());
            mNumberView = binding.number;
            mContentView = binding.content;
            mPhoneButton = binding.phoneButton;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}