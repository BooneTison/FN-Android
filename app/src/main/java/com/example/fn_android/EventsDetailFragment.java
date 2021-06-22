package com.example.fn_android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventsDetailFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String eventName;

    public EventsDetailFragment() {
        // Required empty public constructor
    }

    public static EventsDetailFragment newInstance(String param1, String param2) {
        EventsDetailFragment fragment = new EventsDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_events_detail, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            eventName = bundle.getString("eventName");
        }

        // Find all the texts to set
        TextView title = view.findViewById(R.id.eventTitle);
        TextView date = view.findViewById(R.id.dateTime);
        TextView desc = view.findViewById(R.id.description);
        TextView location = view.findViewById(R.id.locationText);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            String titleText = "";
            String dateText = "";
            String descText = "";
            String locText = "";
            try {
                // Get the event
                String service = makeServiceCallByName("https://cs.furman.edu/~csdaemon/FUNow/clpGet.php",eventName);
                if(!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    titleText = jsonObject.getString("title");
                    dateText = formatTime(jsonObject.getString("date"),jsonObject.getString("start"),jsonObject.getString("end"));
                    descText = jsonObject.getString("description");
                    locText = "Location: " + jsonObject.getString("location");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            String finalTitle = titleText;
            String finalDate = dateText;
            String finalDesc = descText;
            String finalLoc = locText;
            handler.post(() -> { // UI updates
                title.setText(finalTitle);
                date.setText(finalDate);
                desc.setText(finalDesc);
                location.setText(finalLoc);
            });

        });
        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Set the calendar button action
        view.findViewById(R.id.calendarButton).setOnClickListener(v -> {
            Intent insertCalendarIntent = new Intent(Intent.ACTION_INSERT);
            insertCalendarIntent.setData(CalendarContract.Events.CONTENT_URI);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                try { // Functionality with calendar app
                    // Get the event
                    String service = makeServiceCallByName("https://cs.furman.edu/~csdaemon/FUNow/clpGet.php",eventName);
                    if(!service.equals("]")) {
                        JSONArray jsonArray = new JSONArray(service);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        insertCalendarIntent.putExtra(CalendarContract.Events.TITLE,jsonObject.getString("title"));
                        insertCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY,false);
                        Calendar calendar = Calendar.getInstance();
                        String date = jsonObject.getString("date");
                        String start = jsonObject.getString("start");
                        String end = jsonObject.getString("end");
                        calendar.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,Integer.parseInt(date.substring(8)),
                                Integer.parseInt(start.substring(0,2)),Integer.parseInt(start.substring(3,5))); // Start time
                        insertCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,calendar.getTimeInMillis());
                        calendar.set(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,Integer.parseInt(date.substring(8)),
                                Integer.parseInt(end.substring(0,2)),Integer.parseInt(end.substring(3,5))); // End time
                        insertCalendarIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,calendar.getTimeInMillis());
                        insertCalendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION,jsonObject.getString("location"));
                        insertCalendarIntent.putExtra(CalendarContract.Events.DESCRIPTION,jsonObject.getString("description"));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.post(() -> { // UI updates

                });
                startActivity(insertCalendarIntent);
            });
        });

        // Check if the description has a url and make it clickable
        TextView textView = view.findViewById(R.id.description);
        textView.setOnClickListener(v -> {
            String text = textView.getText().toString();
            if (text.contains("http")) {
                text = text.substring(text.indexOf("http")); // Cut beginning of desc
                text = text.substring(0,text.indexOf(" ")); // Cut end of desc
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(text));
                startActivity(i);
            }
        });
    }

    public static String makeServiceCallByName (String reqUrl, String key) {
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
                if (jsonObject.getString("title").equals(key)) {
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

    private String formatTime(String date, String start, String end) {
        String str ;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd", Locale.US);
        Calendar calendar = new GregorianCalendar(Integer.parseInt(date.substring(0,4)),Integer.parseInt(date.substring(5,7))-1,
                Integer.parseInt(date.substring(8))); // Month starts at 0 for some reason?!
        str = sdf.format(calendar.getTime()) + "    ";
        HoursTime hoursTime = new HoursTime(start,end);
        str += hoursTime.toStringHoursOnly();
        return str;
    }
}