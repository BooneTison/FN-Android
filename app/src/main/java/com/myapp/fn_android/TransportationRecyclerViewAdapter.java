package com.myapp.fn_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.fn_android.databinding.FragmentItemBinding;
import com.myapp.fn_android.databinding.FragmentTransportationBinding;

import java.util.HashMap;
import java.util.List;

public class TransportationRecyclerViewAdapter extends RecyclerView.Adapter<TransportationRecyclerViewAdapter.ViewHolder> {

    private final List<String[]> ourList;
    private final int type;
    private final Context context;
    HashMap<String, Integer> images = new HashMap<>();

    int TRANSPORT = 0;

    public TransportationRecyclerViewAdapter(List<String[]> items, int fragType, Context context) {
        type = fragType;
        ourList = items;
        this.context = context;
        images.put("location.circle",R.drawable.transport_arrow_circle);
        images.put("car",R.drawable.transport_car);
        images.put("location.circle.fill",R.drawable.transport_arrow_down);
        images.put("shield.lefthalf.fill",R.drawable.transport_shield);
        images.put("staroflife.fill",R.drawable.transport_star);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (type == TRANSPORT)  // Transport Fragment
            viewHolder = new ViewHolder(FragmentTransportationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        else { // Default item fragment
            viewHolder = new ViewHolder(FragmentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mContentView.setText(ourList.get(position)[0]);
        holder.mIcon = ourList.get(position)[1];
        holder.mRGB = ourList.get(position)[2];
        @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(images.get(holder.mIcon));
        holder.mIconView.setImageDrawable(drawable);

    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public final ImageView mIconView;
        public String mRGB;
        public String mIcon;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mIconView = null;
            mRGB = "";
            mIcon = "";
        }

        // Phone Fragment
        public ViewHolder(FragmentTransportationBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mIconView = binding.vehicleIcon;
            mRGB = "";
            mIcon = "";
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}