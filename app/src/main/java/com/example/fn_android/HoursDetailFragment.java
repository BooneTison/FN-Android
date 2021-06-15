package com.example.fn_android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
public class HoursDetailFragment extends Fragment {

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
    public HoursDetailFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static HoursDetailFragment newInstance(int columnCount) {
        HoursDetailFragment fragment = new HoursDetailFragment();
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
        View view = inflater.inflate(R.layout.fragment_hours_detail_list, container, false);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            buildingID = bundle.getString("id");
            buildingName = bundle.getString("name");
        }

        // Set non-list content
        TextView phone = view.getRootView().findViewById(R.id.phonenumber);
        TextView location = view.getRootView().findViewById(R.id.location);
        List<String[]> hours = new ArrayList<>();

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.categories);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Use this to change content
            String phoneString = "";
            String locString = "";
            List<String[]> hoursList = new ArrayList<>();
            try {
                String service = makeServiceCallByName("http://cs.furman.edu/~csdaemon/FUNow/contactsGet.php",buildingName);
                if (!service.equals("]")) {
                    JSONArray phoneArray = new JSONArray(service);
                    JSONObject phoneObject = phoneArray.getJSONObject(0);
                    phoneString = phoneObject.getString("number");
                    phoneString = phoneString.substring(0,3) + "." + phoneString.substring(3,6) + "." + phoneString.substring(6);
                }

                service = makeServiceCallByID("http://cs.furman.edu/~csdaemon/FUNow/buildingGet.php",buildingID);
                if (!service.equals("]")) {
                    JSONArray locArray = new JSONArray(service);
                    JSONObject locObject = locArray.getJSONObject(0);
                    locString = locObject.getString("location");
                    locString = "Location: " + locString;
                }

                service = makeServiceCallByID("https://cs.furman.edu/~csdaemon/FUNow/hoursGet.php",buildingID);
                if(!service.equals("]")) {
                    List<HoursTime> list = new ArrayList<>();
                    JSONArray hoursArray = new JSONArray(service);
                    for (int i =0; i < hoursArray.length(); i++) {
                        JSONObject hoursObject = hoursArray.getJSONObject(i);
                        if (hoursObject.isNull("Start") || hoursObject.isNull("End")) {
                            list.add(new HoursTime("null","null", hoursObject.getString("dayorder"),
                                    hoursObject.getString("day")));
                        }
                        else {
                            list.add(new HoursTime(hoursObject.getString("Start"), hoursObject.getString("End"),
                                    hoursObject.getString("dayorder"), hoursObject.getString("day")));
                        }
                    }
                    Collections.sort(list);
                    for (int k = 0; k < hoursArray.length(); k++)
                        hoursList.add(new String[]{list.remove(0).toString(), buildingID});
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalPhoneString = phoneString;
            String finalLocString = locString;
            handler.post(() -> {
                recyclerView.setAdapter(new MyItemRecyclerViewAdapter(hoursList,1));
                phone.setText(finalPhoneString);
                location.setText(finalLocString);
            });
        });

        return view;
    }

    public static String makeServiceCallByID (String reqUrl, String key) {
        String response = "nada";
        String line = "No Data";
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
                if (jsonObject.getString("buildingID").equals(key)) {
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

    public static String makeServiceCallByName (String reqUrl, String key) {
        String response = "nada";
        String line = "No Data";
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
                if (jsonObject.getString("name").equals(key)) {
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