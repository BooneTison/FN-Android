package com.example.fn_android;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class DiningDetailFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String buildingID;
    private String buildingName;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DiningDetailFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DiningDetailFragment newInstance(int columnCount) {
        DiningDetailFragment fragment = new DiningDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_dining_detail_list, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            buildingID = bundle.getString("id");
            buildingName = bundle.getString("name");
        }

        // Set non-list content
        TextView location = view.getRootView().findViewById(R.id.location);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView hoursRecyclerView = (RecyclerView) view.findViewById(R.id.hoursList);
        RecyclerView menuRecyclerView = (RecyclerView) view.findViewById(R.id.menuList);
        if (mColumnCount <= 1) {
            hoursRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            menuRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            hoursRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            menuRecyclerView.setLayoutManager(new GridLayoutManager(context,mColumnCount));
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Use this to change content
            String locString = "";
            List<String[]> hoursList = new ArrayList<>();
            List<String[]> menuList = new ArrayList<>();
            try {

                // Get the location
                String service = makeServiceCallByID("https://cs.furman.edu/~csdaemon/FUNow/restaurantGet.php",buildingID);
                if (!service.equals("]")) {
                    JSONArray locArray = new JSONArray(service);
                    JSONObject locObject = locArray.getJSONObject(0);
                    locString = locObject.getString("location");
                    locString = "Located " + locString;
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
                        hoursList.add(new String[]{list.remove(0).toString(), buildingID});

                    // Create the hours
                    switch (buildingName) {
                        case "Daniel Dining Hall":
                            // TODO - DH menu
                            break;
                        case "Bread and Bowl":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Snacks", buildingID}, new String[]{"Misc. Sandwiches", buildingID},
                                    new String[]{"Drinks", buildingID}, new String[]{"...and more", buildingID});
                            break;
                        case "The Library Cafe":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Snacks", buildingID},new String[]{"Smoothies", buildingID},
                                    new String[]{"Coffee Drinks", buildingID}, new String[]{"Muffins", buildingID}, new String[]{"Drinks", buildingID},
                                    new String[]{"...and more", buildingID});
                            break;
                        case "Sweet and Savory":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Snacks", buildingID}, new String[]{"Sandwiches", buildingID},
                                    new String[]{"Cookies", buildingID}, new String[]{"Muffins", buildingID}, new String[]{"...and more", buildingID});
                            break;
                        case "Chick-Fil-A":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Chicken Sandwiches", buildingID}, new String[]{"Nuggets", buildingID},
                                    new String[]{"Waffle Fries", buildingID}, new String[]{"...and more", buildingID});
                            break;
                        case "Moe's":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Burritos", buildingID}, new String[]{"Chips & Salsa", buildingID},
                                    new String[]{"Quesadillas", buildingID}, new String[]{"Tacos", buildingID}, new String[]{"Nachos", buildingID},
                                    new String[]{"...and more", buildingID});
                            break;
                        case "Sushi with Gusto":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Sushi", buildingID}, new String[]{"...and more", buildingID});
                            break;
                        case "The Paddock":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Specialty Sandwiches", buildingID}, new String[]{"Grilled Items", buildingID},
                                    new String[]{"Salads", buildingID}, new String[]{"Drinks", buildingID}, new String[]{"Beer and Wine", buildingID},
                                    new String[]{"...and more", buildingID});
                            break;
                        case "Barnes & Noble Cafe":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Starbucks Coffee", buildingID}, new String[]{"Snacks", buildingID},
                                    new String[]{"Hot Chocolate", buildingID}, new String[]{"...and more", buildingID});
                            break;
                        case "Traditions Grille":
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Sandwiches", buildingID}, new String[]{"Grilled Items", buildingID},
                                    new String[]{"Snacks", buildingID}, new String[]{"Drinks", buildingID}, new String[]{"Beer and Wine", buildingID},
                                    new String[]{"...and more", buildingID});
                            break;
                        default:
                            menuList = (List<String[]>) Arrays.asList(new String[]{"Food", buildingID},new String[]{"Drinks", buildingID},
                                    new String[]{"...andmore", buildingID});
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalLocString = locString;
            List<String[]> finalMenuList = menuList;
            handler.post(() -> {
                //recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                hoursRecyclerView.setAdapter(new DiningRecyclerViewAdapter(hoursList,1));
                menuRecyclerView.setAdapter(new DiningRecyclerViewAdapter(finalMenuList,1));
                menuRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                location.setText(finalLocString);
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

            String str = "[";
            int brack = line.indexOf("[");
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("id").equals(key)) {
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

    }

}