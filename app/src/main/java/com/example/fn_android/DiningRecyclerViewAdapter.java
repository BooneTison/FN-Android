package com.example.fn_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fn_android.databinding.FragmentDiningBinding;
import com.example.fn_android.databinding.FragmentDiningDetailBinding;
import com.example.fn_android.databinding.FragmentHoursDetailBinding;
import com.example.fn_android.databinding.FragmentItemBinding;
import com.example.fn_android.placeholder.PlaceholderContent.PlaceholderItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        if (holder.mOpenCloseButton != null) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                // Get the list of restaurants
                List<String[]> s = new ArrayList<>();
                Boolean open = false;
                try {
                    String service = makeServiceCallByID("https://cs.furman.edu/~csdaemon/FUNow/restaurantHoursGet.php",ourList.get(position)[1]);
                    if (!service.equals("]")) {
                        JSONArray jsonArray = new JSONArray(service);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            HoursTime range;
                            if (jsonObject.isNull("start") || jsonObject.isNull("end")) { // No time given, so closed
                                range = new HoursTime("null","null", jsonObject.getString("dayOrder"),
                                        jsonObject.getString("dayOfWeek"));
                            }
                            else if (jsonObject.isNull("meal")) { // No meal, is by day of week
                                range = new HoursTime(jsonObject.getString("start"), jsonObject.getString("end"),
                                        jsonObject.getString("dayOrder"), jsonObject.getString("dayOfWeek"), "null");
                            }
                            else { // Meal given, by meal
                                range = new HoursTime(jsonObject.getString("start"), jsonObject.getString("end"),
                                        jsonObject.getString("dayOrder"), jsonObject.getString("dayOfWeek"), jsonObject.getString("meal"));
                            }
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEE", Locale.US);
                            sdf.setTimeZone(TimeZone.getDefault());
                            String strDate = sdf.format(Calendar.getInstance().getTime());
                            HoursTime curr = new HoursTime(strDate.substring(0,5), strDate.substring(0,5),"0", strDate.substring(6));
                            if (curr.isPlaceOpen(range))
                                open = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Boolean finalOpen = open;
                handler.post(() -> {
                    if (finalOpen)
                        holder.mOpenCloseButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    else
                        holder.mOpenCloseButton.setImageResource(R.drawable.ic_baseline_remove_circle_24);
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return ourList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public String mItem;
        public ImageButton mOpenCloseButton;

        // Basic Item Fragment
        public ViewHolder(FragmentItemBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            mOpenCloseButton = null;
        }

        // Dining Fragment
        public ViewHolder(FragmentDiningBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            mOpenCloseButton = binding.openclosedButton;
        }

        // Hours Detail Fragment
        public ViewHolder(FragmentDiningDetailBinding binding) {
            super(binding.getRoot());
            mIdView = binding.itemNumber;
            mContentView = binding.content;
            mOpenCloseButton = null;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public static String makeServiceCallByID (String reqUrl, String key) {
        String line;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
            connection.disconnect();
            in.close();

            String str = "[";
            int brack = line.indexOf("[");
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (key.equals(jsonObject.getString("id"))) {
                    str += jsonObject.toString() + ",";
                }
            }
            str = str.substring(0,str.length()-1);
            str += "]";
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }
}