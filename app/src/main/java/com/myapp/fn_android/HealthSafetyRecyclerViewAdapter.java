package com.myapp.fn_android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentHealthSafetyBinding;
import com.myapp.fn_android.databinding.FragmentItemBinding;

import java.util.HashMap;
import java.util.List;

public class HealthSafetyRecyclerViewAdapter extends RecyclerView.Adapter<HealthSafetyRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;
    private final Context context;
    HashMap<String, Integer> images = new HashMap<>();

    int HEALTH_SAFETY = 0;

    public HealthSafetyRecyclerViewAdapter(List<String[]> items, int fragType, Context context) {
        type = fragType;
        ourList = items;
        this.context = context;
        images.put("shield",R.drawable.hs_shield);
        images.put("car",R.drawable.hs_car);
        images.put("bandage.fill",R.drawable.hs_bandage);
        images.put("person.circle",R.drawable.hs_person);
        images.put("heart.circle",R.drawable.hs_error);
        images.put("staroflife",R.drawable.hs_star);
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
        String icon = ourList.get(position)[3];

        if (images.containsKey(icon))
            holder.mIconView.setImageResource(images.get(icon));
        else
            holder.mIconView.setImageResource(R.drawable.baseline_health_and_safety_24);

        switch (holder.mType) {
            case "phone":
                holder.mImageButton.setImageResource(R.drawable.ic_baseline_local_phone_24_purple);

                holder.mImageButton.setOnClickListener(v -> { // Phone call
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    String s = holder.mNumberLink;
                    s = "tel:" + s;
                    callIntent.setData(Uri.parse(s));
                    context.startActivity(callIntent);
                });

                holder.mContentView.setOnClickListener(v -> { // Phone call
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    String s = holder.mNumberLink;
                    s = "tel:" + s;
                    callIntent.setData(Uri.parse(s));
                    context.startActivity(callIntent);
                });

                holder.mIconView.setOnClickListener(v -> { // Phone call
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    String s = holder.mNumberLink;
                    s = "tel:" + s;
                    callIntent.setData(Uri.parse(s));
                    context.startActivity(callIntent);
                });
                break;
            case "link":
                holder.mImageButton.setImageResource(R.drawable.ic_baseline_link_24_purple);

                // Navigate to website when clicked
                holder.mImageButton.setOnClickListener(v -> {
                    String url = holder.mNumberLink;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                });

                holder.mContentView.setOnClickListener(v -> {
                    String url = holder.mNumberLink;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                });

                holder.mIconView.setOnClickListener(v -> {
                    String url = holder.mNumberLink;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    context.startActivity(i);
                });
                break;
            case "app":
                holder.mImageButton.setImageResource(R.drawable.ic_baseline_exit_to_app_24);

                // Open app when clicked
                holder.mContentView.setOnClickListener(v -> {
                    String pack = holder.mNumberLink;
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(pack);
                    if (i == null) {
                        String url = "https://play.google.com/store/apps/details?id=" + pack + "&hl=en_US&gl=US";
                        i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                    }
                    context.startActivity(i);
                });

                holder.mImageButton.setOnClickListener(v -> {
                    String pack = holder.mNumberLink;
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(pack);
                    if (i == null) {
                        String url = "https://play.google.com/store/apps/details?id=" + pack + "&hl=en_US&gl=US";
                        i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                    }
                    context.startActivity(i);
                });

                holder.mIconView.setOnClickListener(v -> {
                    String pack = holder.mNumberLink;
                    Intent i = context.getPackageManager().getLaunchIntentForPackage(pack);
                    if (i == null) {
                        String url = "https://play.google.com/store/apps/details?id=" + pack + "&hl=en_US&gl=US";
                        i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                    }
                    context.startActivity(i);
                });
                break;
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
        public ImageView mIconView;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mNumberLink = "";
            mContentView = binding.content;
            mImageButton = null;
            mType = "";
            mIconView = null;
        }

        // Health and Safety Fragment
        public ViewHolder(FragmentHealthSafetyBinding binding) {
            super(binding.getRoot());
            mNumberLink = "";
            mContentView = binding.content;
            mImageButton = binding.phoneLinkButton;
            mType = "";
            mIconView = binding.iconImage;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}