package com.myapp.fn_android;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    List<Marker> placedMarkers = new ArrayList<>();
    ConstraintLayout noDataLayout;

    List<String[]> vehicleList;
    TransportationRecyclerViewAdapter adapter;

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
        View view = inflater.inflate(R.layout.fragment_transportation_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        RecyclerView recyclerMenuView = view.findViewById(R.id.vehicleList);
        recyclerMenuView.setLayoutManager(new LinearLayoutManager(context));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            // Get the list of contacts
            vehicleList = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/vehicleNamesGet.php");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String rgb = jsonObject.getString("colorRed") + "," + jsonObject.getString("colorGreen") + "," + jsonObject.getString("colorBlue");
                        vehicleList.add(new String[]{jsonObject.getString("name"),jsonObject.getString("iconName"),rgb});
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            handler.post(() -> { // Update UI
                recyclerMenuView.addItemDecoration(new DividerItemDecoration(requireActivity(), LinearLayoutManager.VERTICAL));
                adapter = new TransportationRecyclerViewAdapter(vehicleList, 0, this.getContext());
                recyclerMenuView.setAdapter(adapter);
            });
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        noDataLayout = view.findViewById(R.id.NoDataLayout);
        requireActivity().setTitle(R.string.transportation_text);
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
                        String name = stopObject.getString("vehicle");
                        String time = stopObject.getString("updated");
                        BitmapDescriptor icon;
                        switch (name) {
                            case "Downtown Trolley":
                                icon = vectorToBitmap(R.drawable.transport_arrow_down);
                                break;
                            case "SafeRide":
                                icon = vectorToBitmap(R.drawable.transport_shield);
                                break;
                            case "Walmart Shuttle":
                                icon = vectorToBitmap(R.drawable.transport_star);
                                break;
                            case "503 Bus":
                                icon = vectorToBitmap(R.drawable.transport_arrow_circle);
                                break;
                            case "Daily Shuttle":
                                icon = vectorToBitmap(R.drawable.transport_car);
                                break;
                            default:
                                icon = vectorToBitmap(R.drawable.appicon512);
                                break;
                        }
                        if (updatedRecently(time)) {
                            list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(time));
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
            if (brack == -1) return "]"; // Empty php file
            int brack2 = line.indexOf("]");
            line = line.substring(brack,brack2+1);
            boolean foundSR = false;
            boolean foundTR = false;
            boolean foundWM = false;
            boolean foundBU = false;
            boolean foundDS = false;
            JSONArray jsonArray = new JSONArray(line);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("vehicle").equals("SafeRide") && !foundSR) {
                    str.append(jsonObject.toString()).append(",");
                    foundSR = true;
                }
                else if (jsonObject.getString("vehicle").equals("Downtown Trolley") && !foundTR) {
                    str.append(jsonObject.toString()).append(",");
                    foundTR = true;
                }
                else if (jsonObject.getString("vehicle").equals("Walmart Shuttle") && !foundWM) {
                    str.append(jsonObject.toString()).append(",");
                    foundWM = true;
                }
                else if (jsonObject.getString("vehicle").equals("503 Bus") && !foundBU) {
                    str.append(jsonObject.toString()).append(",");
                    foundBU = true;
                }
                else if (jsonObject.getString("vehicle").equals("Daily Shuttle") && !foundDS) {
                    str.append(jsonObject.toString()).append(",");
                    foundDS = true;
                }
                if (foundSR && foundTR && foundWM && foundBU && foundDS)
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
        final int TIME_FRAME = 30;
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

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(),id,null);
        assert vectorDrawable != null;
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}