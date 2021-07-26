package com.myapp.fn_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class DiningOffCampusFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private String buildingID;
    private String buildingName;
    private String url;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DiningOffCampusFragment() {
    }

    @SuppressWarnings("unused")
    public static DiningOffCampusFragment newInstance(int columnCount) {
        DiningOffCampusFragment fragment = new DiningOffCampusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dining_off_campus, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            buildingID = bundle.getString("id");
            buildingName = bundle.getString("name");
        }

        // Set non-list content
        TextView location = view.getRootView().findViewById(R.id.location);
        ImageView diningPicture = view.getRootView().findViewById(R.id.diningPicture);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView hoursRecyclerView = view.findViewById(R.id.hoursList);
        if (mColumnCount <= 1) {
            hoursRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            hoursRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Get the hours list
            String locString = "";
            List<String[]> hoursList = new ArrayList<>();
            Drawable image = null;
            try {

                // Get the location
                String service = makeServiceCallByID("https://cs.furman.edu/~csdaemon/FUNow/restaurantGet.php",buildingID);
                if (!service.equals("]")) {
                    JSONArray locArray = new JSONArray(service);
                    JSONObject locObject = locArray.getJSONObject(0);
                    locString = locObject.getString("location");

                    // Get the url
                    url = locObject.getString("url");
                }

                // Get the hours
                service = makeServiceCallByID("https://cs.furman.edu/~csdaemon/FUNow/restaurantHoursGet.php",buildingID);
                if(!service.equals("]")) {
                    List<HoursTime> list = new ArrayList<>();
                    JSONArray hoursArray = new JSONArray(service);
                    for (int i =0; i < hoursArray.length(); i++) {
                        JSONObject hoursObject = hoursArray.getJSONObject(i);
                        if (hoursObject.isNull("start") || hoursObject.isNull("end")) { // No time given, so closed
                            list.add(new HoursTime("null","null", hoursObject.getString("dayOrder"),
                                    hoursObject.getString("dayOfWeek")));
                        }
                        else if (hoursObject.getString("start").equals("00:00:00") && hoursObject.getString("end").equals("00:00:00")) { // Closed
                            list.add(new HoursTime("null","null", hoursObject.getString("dayOrder"),
                                    hoursObject.getString("dayOfWeek")));
                        }
                        else if (hoursObject.isNull("meal")) { // No meal, is by day of week
                            list.add(new HoursTime(hoursObject.getString("start"), hoursObject.getString("end"),
                                    hoursObject.getString("dayOrder"), hoursObject.getString("dayOfWeek"), "null"));
                        }
                        else { // Meal given, by meal
                            list.add(new HoursTime(hoursObject.getString("start"), hoursObject.getString("end"),
                                    hoursObject.getString("dayOrder"), hoursObject.getString("dayOfWeek"), hoursObject.getString("meal")));
                        }
                    }
                    Collections.sort(list);
                    for (int k = 0; k < hoursArray.length(); k++)
                        hoursList.add(new String[]{list.remove(0).toString(), ""});
                }

                // Get the dining picture
                String imageUrl = "https://cs.furman.edu/~csdaemon/FUNow/appImages/";
                imageUrl += buildingName + ".png";
                InputStream URLcontent = (InputStream) new URL(imageUrl).getContent();
                image = Drawable.createFromStream(URLcontent,"dining image");

            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalLocString = locString;
            Drawable finalImage = image;
            handler.post(() -> { // UI updates
                hoursRecyclerView.setAdapter(new DiningRecyclerViewAdapter(hoursList,1,this.getContext()));
                location.setText(finalLocString);
                diningPicture.setImageDrawable(finalImage);
            });
        });

        return view;
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

            StringBuilder str = new StringBuilder("[");
            int brack = line.indexOf("[");
            if (brack == -1) return "]"; // Empty php file
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("id").equals(key)) {
                    str.append(jsonObject.toString()).append(",");
                }
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            str.append("]");
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }

    public static String makeServiceCallByRestaurant(String reqUrl, String key) {
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

            StringBuilder str = new StringBuilder("[");
            int brack = line.indexOf("[");
            if (brack == -1) return "]"; // Empty php file
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("restaurant").equals(key)) {
                    str.append(jsonObject.toString()).append(",");
                }
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            str.append("]");
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }

    public static String makeServiceCallFoodServices(String reqUrl, String key) {
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

            StringBuilder str = new StringBuilder("[");
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("fullname").equals(key)) {
                    str.append(jsonObject.toString()).append(",");
                }
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            str.append("]");
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }

    public static String makeServiceCall(String reqUrl) {
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

            StringBuilder str = new StringBuilder("[");
            int brack = line.indexOf("[");
            if (brack == -1) return "]"; // Empty php file
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                str.append(jsonObject.toString()).append(",");
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            str.append("]");
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        requireActivity().setTitle(buildingName);

        Button mapButton = view.findViewById(R.id.mapviewButton);
        Bundle bundle = new Bundle();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String service = makeServiceCallFoodServices("https://cs.furman.edu/~csdaemon/FUNow/foodservicesGet.php",buildingName);
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    double lat = jsonObject.getDouble("latitude");
                    double lon = jsonObject.getDouble("longitude");
                    bundle.putString("name",buildingName);
                    bundle.putDouble("latitude",lat);
                    bundle.putDouble("longitude",lon);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> mapButton.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.mapFragment,bundle)));
        });

        view.findViewById(R.id.orderOnline).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        view.findViewById(R.id.onlineText).setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
    }

}