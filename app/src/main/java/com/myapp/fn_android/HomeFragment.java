package com.myapp.fn_android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Get the weather
            TextView highText = view.findViewById(R.id.highTemp);
            TextView lowText = view.findViewById(R.id.lowTemp);
            String low = "";
            String high = "";
            String emoji = "";
            TextView emojiText = view.findViewById(R.id.emoji);
            String precipitationPercent = "";
            TextView chanceText = view.findViewById(R.id.precipitationPercent);
            TextView alertText = view.findViewById(R.id.alertText);
            String alert = "";
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/weatherGet.php");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (withinDateRange(jsonObject.getString("start"),jsonObject.getString("end"))) {
                            // Get the low and high
                            low = jsonObject.getString("tempLo");
                            high = jsonObject.getString("tempHi");
                            low += "\u00B0";
                            high += "\u00B0" + " / ";
                            // Get the emoji
                            emoji = jsonObject.getString("emoji");
                            emoji = emoji.replace("0x", "");
                            // Get the rain chance
                            precipitationPercent = jsonObject.getString("precipitationPercent");
                            // Get the alert
                            alert = jsonObject.getString("alert");
                        }
                    }
                    if (emoji.equals("")) { // No data within database within date
                        low = "0" + "\u00B0";
                        high = "0" + "\u00B0" + " / ";
                        emoji = "1F31E";
                        precipitationPercent = "";
                        alert = "";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String finalLow = low;
            String finalHigh = high;
            int finalEmoji = Integer.parseInt(emoji,16);
            String finalChance = precipitationPercent;
            String finalAlert = alert;
            handler.post(() -> {
                highText.setText(finalHigh);
                lowText.setText(finalLow);
                emojiText.setText(new String(Character.toChars(finalEmoji)));
                chanceText.setText(finalChance);
                alertText.setText(finalAlert);
            });
        });

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.athletics_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_athleticsFragment));
        view.findViewById(R.id.athletics_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_athleticsFragment));

        view.findViewById(R.id.events_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_EventsSyncDinFragment));
        view.findViewById(R.id.events_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_EventsSyncDinFragment));

        view.findViewById(R.id.dining_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_diningFragment));
        view.findViewById(R.id.dining_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_diningFragment));

        view.findViewById(R.id.health_safety_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_healthSafetyFragment));
        view.findViewById(R.id.health_safety_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_healthSafetyFragment));

        view.findViewById(R.id.phone_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_phoneFragment));
        view.findViewById(R.id.phone_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_phoneFragment));

        view.findViewById(R.id.transportation_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_transportationFragment));
        view.findViewById(R.id.transportation_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_transportationFragment));

        view.findViewById(R.id.hours_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_hoursFragment));
        view.findViewById(R.id.hours_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_hoursFragment));

        view.findViewById(R.id.dates_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_datesFragment));
        view.findViewById(R.id.dates_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_datesFragment));

        view.findViewById(R.id.map_button).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_mapFragment));
        view.findViewById(R.id.map_button_text).setOnClickListener(v -> NavHostFragment.findNavController(HomeFragment.this)
                .navigate(R.id.action_homeFragment_to_mapFragment));


        // Handle home image
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            ImageView imageView = view.findViewById(R.id.homeImage);
            Drawable image = null;
            try {
                String imageUrl = "https://cs.furman.edu/~csdaemon/FUNow/monthImages/";
                SimpleDateFormat sdf = new SimpleDateFormat("MM", Locale.US);
                String month = sdf.format(Calendar.getInstance().getTime()); // Get the month for the url
                imageUrl += Integer.parseInt(month) + ".jpg"; // Remove leading zero if necessary
                InputStream URLcontent = (InputStream) new URL(imageUrl).getContent();
                image = Drawable.createFromStream(URLcontent,"month image");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Drawable finalImage = image;
            handler.post(() -> imageView.setImageDrawable(finalImage));
        });
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

    private boolean withinDateRange(String start, String end) {
        Date current = Calendar.getInstance().getTime();

        Calendar calendar = Calendar.getInstance();
        int year = Integer.parseInt(start.substring(0,4));
        int month = Integer.parseInt(start.substring(5,7));
        int day = Integer.parseInt(start.substring(8,10));
        int hr = Integer.parseInt(start.substring(11,13));
        int min = Integer.parseInt(start.substring(14,16));
        int sec = Integer.parseInt(start.substring(17));
        calendar.set(year,month-1,day,hr,min,sec);
        Date startDate = calendar.getTime();

        year = Integer.parseInt(end.substring(0,4));
        month = Integer.parseInt(end.substring(5,7));
        day = Integer.parseInt(end.substring(8,10));
        hr = Integer.parseInt(end.substring(11,13));
        min = Integer.parseInt(end.substring(14,16));
        sec = Integer.parseInt(end.substring(17));
        calendar.set(year,month-1,day,hr,min,sec);
        Date endDate = calendar.getTime();

        return current.after(startDate) && current.before(endDate);
    }
}