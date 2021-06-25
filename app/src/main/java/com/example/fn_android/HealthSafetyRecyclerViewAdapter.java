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

import com.example.fn_android.databinding.FragmentHealthSafetyBinding;
import com.example.fn_android.databinding.FragmentItemBinding;

import java.util.List;

public class HealthSafetyRecyclerViewAdapter extends RecyclerView.Adapter<HealthSafetyRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;
    private final Context context;

    int HEALTH_SAFETY = 0;

    public HealthSafetyRecyclerViewAdapter(List<String[]> items, int fragType, Context context) {
        type = fragType;
        ourList = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (type == HEALTH_SAFETY)  // Health and Safety Fragment
            viewHolder = new ViewHolder(FragmentHealthSafetyBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else { // Default item fragment
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = ourList.get(position)[0];
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mNumberLink = ourList.get(position)[1];
        holder.mType = ourList.get(position)[2];

        if (holder.mType.equals("phone")) {
            holder.mImageButton.setImageResource(R.drawable.ic_baseline_local_phone_24_purple);

            holder.mImageButton.setOnClickListener(v -> { // Phone call TODO - Check if this is working
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String s = holder.mNumberLink;
                s = "tel:" + s;
                callIntent.setData(Uri.parse(s));

                if(ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                context.startActivity(callIntent);
            });
        }
        else if (holder.mType.equals("link")) {
            holder.mImageButton.setImageResource(R.drawable.ic_baseline_link_24_purple);

            // Navigate to website when clicked
            holder.mImageButton.setOnClickListener(v -> {
                String url = holder.mNumberLink;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            });
        }
    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public String mNumberLink;
        public final TextView mContentView;
        public String mItem;
        public ImageButton mImageButton;
        public String mType;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mNumberLink = "";
            mContentView = binding.content;
            mImageButton = null;
            mType = "";
        }

        // Health and Safety Fragment
        public ViewHolder(FragmentHealthSafetyBinding binding) {
            super(binding.getRoot());
            mNumberLink = "";
            mContentView = binding.content;
            mImageButton = binding.phoneLinkButton;
            mType = "";
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}