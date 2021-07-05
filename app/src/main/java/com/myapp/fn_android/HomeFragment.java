package com.myapp.fn_android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
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
}