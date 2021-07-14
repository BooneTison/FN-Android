package com.myapp.fn_android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransportationFragment extends Fragment {
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    double latitude = 34.9246422; // Default lat, Library
    double longitude = -82.4390771; // Default long, Library
    final int userIconWidth = 75;
    final int userIconHeight = 75;
    final int shuttleIconWidth = 100;
    final int shuttleIconHeight = 100;

    TextView saferideText;
    TextView trolleyText;
    TextView walmartText;
    List<Marker> placedMarkers = new ArrayList<>();
    ConstraintLayout noDataLayout;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            googleMap.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.style_json))); // Remove built-in points of interest
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation();
            }

            LatLng currentPos = new LatLng(latitude, longitude);
            Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.paladinx3);
            Bitmap sb = Bitmap.createScaledBitmap(b,userIconWidth,userIconHeight,false);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(sb);
            googleMap.addMarker(new MarkerOptions().position(currentPos).title("Current Position").icon(icon));

            addStops(googleMap);

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 16));

            final Handler handler = new Handler();
            final int delay = 5000; // 1000 milliseconds == 1 second
            handler.postDelayed(new Runnable() { // Runs every interval according to delay
                @Override
                public void run() {
                    addShuttles(googleMap);
                    //Toast.makeText(requireContext(), "10 Seconds", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, delay);
                }
            }, delay);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transportation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        saferideText = view.findViewById(R.id.saferideText);
        walmartText = view.findViewById(R.id.walmartText);
        trolleyText = view.findViewById(R.id.trolleyText);
        noDataLayout = view.findViewById(R.id.NoDataLayout);
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", (dialog, which) ->
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("No", (dialog, which) ->
                dialog.cancel());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                latitude = locationGPS.getLatitude();
                longitude = locationGPS.getLongitude();
                //String text = "Lat: " + latitude + " Long: " + longitude;
                //Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addStops(@NonNull GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> list = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/503Get.php");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {
                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"),stopObject.getDouble("longitude"));
                        String name = stopObject.getString("stop");
                        String route = stopObject.getString("route");
                        BitmapDescriptor icon;
                        switch (route) {
                            case "Bus 503":
                            case "503 Bus":
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                                route = stopObject.getString("eta");
                                break;
                            case "Daily Shuttle":
                            case "Daily Shuttle ":
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                                break;
                            case "Walmart Shuttle":
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                break;
                            default:
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                                break;
                        }
                        list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(route));
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                while (!list.isEmpty())
                    googleMap.addMarker(list.remove(0));
            });
        });
    }

    private void addShuttles(@NonNull GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> list = new ArrayList<>();
            boolean noData = false;
            try {
                // Returns most recent location for each shuttle
                String service = makeServiceCallByVehicle("http://cs.furman.edu/~csdaemon/FUNow/shuttleGet.php?v=all");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {
                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"),stopObject.getDouble("longitude"));
                        String name = stopObject.getString("vehicle").toUpperCase();
                        String time = stopObject.getString("updated");
                        BitmapDescriptor icon;
                        switch (name) {
                            case "TROLLEY":
                                Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.appicon512);
                                Bitmap sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;
                            case "SAFERIDE":
                                b = BitmapFactory.decodeResource(getResources(),R.drawable.furmandiamond);
                                sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;
                            case "WALMART":
                                b = BitmapFactory.decodeResource(getResources(),R.drawable.walmart);
                                sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;
                            default:
                                b = BitmapFactory.decodeResource(getResources(),R.drawable.appicon512);
                                sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;
                        }
                        if (updatedRecently(time)) {
                            list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(time));
                            switch (name) {
                                case "SAFERIDE":
                                    saferideText.setText(R.string.saferide_running_text);
                                    break;
                                case "TROLLEY":
                                    trolleyText.setText(R.string.trolley_running_text);
                                    break;
                                case "WALMART":
                                    walmartText.setText(R.string.walmart_running_text);
                                    break;
                            }
                        }
                        else {
                            switch (name) {
                                case "SAFERIDE":
                                    saferideText.setText(R.string.saferide_not_running_text);
                                    break;
                                case "TROLLEY":
                                    trolleyText.setText(R.string.trolley_not_running_text);
                                    break;
                                case "WALMART":
                                    walmartText.setText(R.string.walmart_not_running_text);
                                    break;
                            }
                        }
                    }
                }
                else { // No data found, put up the splash screen
                    noData = true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            boolean finalNoData = noData;
            handler.post(() -> {
                if (finalNoData) noDataLayout.setVisibility(View.VISIBLE);
                else noDataLayout.setVisibility(View.INVISIBLE);
                while (!placedMarkers.isEmpty())
                    placedMarkers.remove(0).remove();
                while (!list.isEmpty())
                    placedMarkers.add(googleMap.addMarker(list.remove(0)));
            });
        });
    }

    public static String makeServiceCall (String reqUrl) {
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

    public static String makeServiceCallByVehicle (String reqUrl) {
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

            if (line.equals("{\"format\":\"shuttles\",\"results\":null,\"error\":\"ok\"}"))
                return "]";

            StringBuilder str = new StringBuilder("[");
            int brack = line.indexOf("[");
            line = line.substring(brack,line.length()-1);
            boolean foundSR = false;
            boolean foundTR = false;
            boolean foundWM = false;
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("vehicle").equals("saferide") && !foundSR) {
                    str.append(jsonObject.toString()).append(",");
                    foundSR = true;
                }
                else if (jsonObject.getString("vehicle").equals("trolley") && !foundTR) {
                    str.append(jsonObject.toString()).append(",");
                    foundTR = true;
                }
                else if (jsonObject.getString("vehicle").equals("walmart") && !foundWM) {
                    str.append(jsonObject.toString()).append(",");
                    foundWM = true;
                }
                if (foundSR && foundTR && foundWM)
                    break;
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            str.append("]");
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "I died";
        }
    }

    private boolean updatedRecently(String inputDate) {
        final int TIME_FRAME = 20;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
        String todayDate = sdf.format(Calendar.getInstance().getTime());
        int tYear = Integer.parseInt(todayDate.substring(0,4));
        int tMonth = Integer.parseInt(todayDate.substring(5,7));
        int tDay = Integer.parseInt(todayDate.substring(8,10));
        int tHour = Integer.parseInt(todayDate.substring(11,13));
        int tMin = Integer.parseInt(todayDate.substring(14,16));
        int tSec = Integer.parseInt(todayDate.substring(17));
        int secondsToday = (tHour * 60 * 60) + (tMin * 60) + tSec;

        int iYear = Integer.parseInt(inputDate.substring(0,4));
        int iMonth = Integer.parseInt(inputDate.substring(5,7));
        int iDay = Integer.parseInt(inputDate.substring(8,10));
        int iHour = Integer.parseInt(inputDate.substring(11,13));
        int iMin = Integer.parseInt(inputDate.substring(14,16));
        int iSec = Integer.parseInt(inputDate.substring(17));
        int secondsInput = (iHour * 60 * 60) + (iMin * 60) + iSec;

        if (tYear != iYear || tMonth != iMonth || tDay != iDay) return false; // not same day
        return secondsToday - secondsInput <= TIME_FRAME; // return if within time frame
    }
}