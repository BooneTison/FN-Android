package com.myapp.fn_android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A fragment representing a list of Items.
 */
public class EventsCLPFragment extends Fragment {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventsCLPFragment() {
    }

    @SuppressWarnings("unused")
    public static EventsCLPFragment newInstance(int columnCount) {
        EventsCLPFragment fragment = new EventsCLPFragment();
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
        View view = inflater.inflate(R.layout.fragment_events_clp_list, container, false);

        // Get Dates
        TextView today = view.findViewById(R.id.todayDateText);
        TextView tomorrow = view.findViewById(R.id.tomorrowDateText);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,1);
        String tomDate = sdf.format(cal.getTime());

        // Set the adapter
        Context context = view.getContext();
        RecyclerView todayRecyclerView = view.findViewById(R.id.todayList);
        RecyclerView tomRecyclerView = view.findViewById(R.id.tomorrowList);
        RecyclerView weekRecyclerView = view.findViewById(R.id.thisweekList);
        if (mColumnCount <= 1) {
            todayRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            tomRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            weekRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            todayRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            tomRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            weekRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Use this to change content
            List<String[]> todayList = new ArrayList<>();
            List<String[]> tomList = new ArrayList<>();
            List<String[]> weekList = new ArrayList<>();
            try {
                String service = makeServiceCallByType("https://cs.furman.edu/~csdaemon/FUNow/clpGet.php","CLP");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        HoursTime hoursTime = new HoursTime(jsonObject.getString("start"),jsonObject.getString("end"));
                        // Check the date
                        // 0 is today, 1 is tomorrow, 2 is this week, -1 is in past or more than a week
                        int check = checkDate(jsonObject.getString("date"));
                        String date = jsonObject.getString("date");
                        cal.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,Integer.parseInt(date.substring(8)));
                        date = sdf.format(cal.getTime());
                        if (check == 0) todayList.add(new String[]{jsonObject.getString("title"),hoursTime.toStringHoursOnly()});
                        else if (check == 1) tomList.add(new String[]{jsonObject.getString("title"),hoursTime.toStringHoursOnly()});
                        else if (check == 2) weekList.add(new String[]{jsonObject.getString("title"),date + " " + hoursTime.toStringHoursOnly()});
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                todayRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                todayRecyclerView.setAdapter(new EventsRecyclerViewAdapter(todayList,1));
                tomRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                tomRecyclerView.setAdapter(new EventsRecyclerViewAdapter(tomList,1));
                weekRecyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                weekRecyclerView.setAdapter(new EventsRecyclerViewAdapter(weekList,1));

                today.setText(todayDate);
                tomorrow.setText(tomDate);
            });
        });

        return view;
    }

    public static String makeServiceCallByType (String reqUrl, String key) {
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
                if (jsonObject.getString("eventType").equals(key)) {
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
        // Navigate to website when clicked
        Button link = view.findViewById(R.id.linkButton);
        link.setOnClickListener(v -> {
            String url = "https://www.furman.edu/academics/cultural-life-program/upcoming-clp-events";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        // Navigate to CLP
        view.findViewById(R.id.syncdinButton).setOnClickListener(v -> NavHostFragment.findNavController(EventsCLPFragment.this)
                .navigate(R.id.action_eventsCLPFragment_to_EventsSyncDinFragment));
    }

    private int checkDate(String date) {
        // 0 is today, 1 is tomorrow, 2 is this week, -1 is past or more than a week
        SimpleDateFormat sdf = new SimpleDateFormat("DDD yyyy", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        String todayDate = sdf.format(Calendar.getInstance().getTime());
        int todayDay = Integer.parseInt(todayDate.substring(0,3));
        int todayYear = Integer.parseInt(todayDate.substring(4));

        Calendar calendar = new GregorianCalendar(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,
                Integer.parseInt(date.substring(8))); // Month starts at 0 for some reason?!
        String checkDate = sdf.format(calendar.getTime());
        int checkDay = Integer.parseInt(checkDate.substring(0,3));
        int checkYear = Integer.parseInt(date.substring(0,4));

        if (todayDay == checkDay && todayYear == checkYear) return 0; // Same day
        if (todayDay <= 358) { // Edge cases where adding goes into next year
            if (todayDay+1 == checkDay && todayYear == checkYear) return 1; // Tomorrow
            if (checkDay-todayDay <= 7 && checkDay-todayDay > 0 && todayYear == checkYear) return 2; // Within week
        }
        else { // Will be comparing into next year
            if (todayDay+1%365 == checkDay && (todayYear == checkYear || todayYear == checkYear+1)) return 1; // Tomorrow
            if (checkDay+365-todayDay <= 7 && checkDay+365-todayDay > 0 && (todayYear == checkYear || todayYear == checkYear+1))
                return 2; // Within week
        }
        return -1; // Not within the week
    }

}