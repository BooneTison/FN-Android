package com.myapp.fn_android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class HoursDetailFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private String buildingID;
    private String buildingName;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HoursDetailFragment() {
    }

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
        ImageButton phoneButton = view.getRootView().findViewById(R.id.phoneButton);
        TextView specialText = view.getRootView().findViewById(R.id.specialText);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerView = view.findViewById(R.id.dailyHoursList);
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
                // Get the phone number
                String service = makeServiceCallByID("http://cs.furman.edu/~csdaemon/FUNow/contactsGet.php",buildingID);
                if (!service.equals("]")) {
                    JSONArray phoneArray = new JSONArray(service);
                    JSONObject phoneObject = phoneArray.getJSONObject(0);
                    phoneString = phoneObject.getString("number");
                    phoneString = phoneString.substring(0,3) + "." + phoneString.substring(3,6) + "." + phoneString.substring(6);
                }
                else { // no phone number found, so remove the phone button
                    phoneButton.setColorFilter(Color.TRANSPARENT);
                }

                // Get the location
                service = makeServiceCallByID("http://cs.furman.edu/~csdaemon/FUNow/buildingGet.php",buildingID);
                if (!service.equals("]")) {
                    JSONArray locArray = new JSONArray(service);
                    JSONObject locObject = locArray.getJSONObject(0);
                    locString = locObject.getString("location");
                    locString = "Location: " + locString;
                }

                if (buildingName.equals("ATM")) buildingID = "2"; // ATM does not have listed hours, uses Trone's hours

                // Get the hours
                service = makeServiceCallByID("https://cs.furman.edu/~csdaemon/FUNow/hoursGet.php",buildingID);
                if(!service.equals("]")) {
                    List<HoursTime> list = new ArrayList<>();
                    JSONArray hoursArray = new JSONArray(service);
                    for (int i =0; i < hoursArray.length(); i++) {
                        JSONObject hoursObject = hoursArray.getJSONObject(i);
                        if (hoursObject.isNull("Start") || hoursObject.isNull("End")) { // Closed
                            list.add(new HoursTime("null","null", hoursObject.getString("dayorder"),
                                    hoursObject.getString("day")));
                        }
                        else if (hoursObject.getString("Start").equals("00:00:00") || hoursObject.getString("End").equals("00:00:00")) { // Closed
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
                recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                recyclerView.setAdapter(new HoursRecyclerViewAdapter(hoursList,1));
                phone.setText(finalPhoneString);
                location.setText(finalLocString);
                // Set the special text, only applies to Earle
                if (buildingName.equals("Earle Student Health Center"))
                    specialText.setText(R.string.earle_special_text);
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
            line = line.substring(brack,line.length()-1);
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("buildingID").equals(key)) {
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

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ImageButton button = view.findViewById(R.id.phoneButton);
        button.setOnClickListener(v -> { // Phone call
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            TextView textView = requireView().findViewById(R.id.phonenumber);
            String s = textView.getText().toString();
            s = s.substring(0,3) + s.substring(4,7) + s.substring(8);
            s = "tel:" + s;
            callIntent.setData(Uri.parse(s));
            startActivity(callIntent);
        });

        TextView phoneText = view.findViewById(R.id.phonenumber);
        phoneText.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            String s = phoneText.getText().toString();
            s = s.substring(0,3) + s.substring(4,7) + s.substring(8);
            s = "tel:" + s;
            callIntent.setData(Uri.parse(s));
            startActivity(callIntent);
        });

        // Navigate to website when clicked
        TextView specialText = view.findViewById(R.id.specialText);
        specialText.setOnClickListener(v -> {
            if (buildingName.equals("Earle Student Health Center")) {
                String url = "https://mychart.ghs.org/mychart/Authentication/Login?";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

}