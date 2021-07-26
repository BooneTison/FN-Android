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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CampusMapFragment extends Fragment {
    double latitude = 34.9246422; // Default lat, Library
    double longitude = -82.4390771; // Default long, Library
    String buildingName = ""; // Default name
    LocationManager locationManager;
    private GoogleMap mMap;

    double userLatitude;
    double userLongitude;
    LatLng furman = new LatLng(latitude, longitude);
    final int BigWidth = 100;
    final int BigHeight = 100;
    final int medWidth= 75;
    final int medHeight= 75;
    final int smallWidth= 50;
    final int smallHeight= 50;
    //final int tinyHeight=35;
    //final int tinyWidth=35;
    List<Marker> mapMarkers = new ArrayList<>();
    List<Marker> deletedMarkers = new ArrayList<>();
    List<Marker> searchMarkers = new ArrayList<>();
    float zoom;

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
            googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation();
            }

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(furman));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(furman, 16));

            if (!buildingName.equals("")) {
                LatLng latLng = new LatLng(latitude,longitude);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                googleMap.addMarker(new MarkerOptions().position(latLng).title(buildingName).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE)));
            }
            else {
                LatLng curPos = new LatLng(userLatitude, userLongitude);
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.paladinx3);
                Bitmap sb = Bitmap.createScaledBitmap(b, 75, 75, false);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(sb);
                googleMap.addMarker(new MarkerOptions().position(curPos).title("Current Position").icon(icon));

                googleMap.setOnCameraIdleListener(() -> {
                    if (searchMarkers.isEmpty()) {
                        addBuildings(googleMap);
                        addFood(googleMap);
                    }
                });
            }
            mMap=googleMap;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals(""))
                    searchBAR(newText, mMap);
                else {
                    while (!searchMarkers.isEmpty()) {
                        searchMarkers.remove(0).remove();
                    }
                    addBuildings(mMap);
                    addFood(mMap);
                }
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campus_map, container, false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            latitude = bundle.getDouble("latitude");
            longitude = bundle.getDouble("longitude");
            buildingName = bundle.getString("name");
        }
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
        requireActivity().setTitle(R.string.map_text);
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
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                userLatitude = locationGPS.getLatitude();
                userLongitude = locationGPS.getLongitude();
                //String text = "Lat: " + latitude + " Long: " + longitude;
                //Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(requireContext(), "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchBAR(String text, GoogleMap map) {
        deletedMarkers.addAll(mapMarkers);
        while (!mapMarkers.isEmpty()) {
            mapMarkers.remove(0).remove();
        }
        while (!searchMarkers.isEmpty()) {
            searchMarkers.remove(0).remove();
        }
        List <MarkerOptions> mo = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                String service = makeServiceCall("http://cs.furman.edu/~csdaemon/FUNow/buildingGet.php");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getString("name").toLowerCase().contains(text.toLowerCase()) ||
                                jsonObject.getString("nickname").toLowerCase().contains(text.toLowerCase())) {
                            LatLng loc = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                            String name = jsonObject.getString("name");
                            String nickname = jsonObject.getString("nickname");
                            if (nickname.equals("null")) {
                                nickname = "";
                            }
                            MarkerOptions m = new MarkerOptions().position(loc).title(name).icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)).snippet(nickname);
                            mo.add(m);
                        }
                    }
                }
                service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/restaurantGet.php");
                if (!service.equals("]")) {
                    JSONArray jsonArray = new JSONArray(service);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getString("fullname").toLowerCase().contains(text.toLowerCase()) ||
                                jsonObject.getString("name").toLowerCase().contains(text.toLowerCase())) {
                            LatLng loc = new LatLng(jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude"));
                            String name = jsonObject.getString("fullname");
                            String nickname = jsonObject.getString("name");
                            if (nickname.equals("null")) {
                                nickname = "";
                            }
                            MarkerOptions m = new MarkerOptions().position(loc).title(name).icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_RED)).snippet(nickname);
                            mo.add(m);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> { // Update UI
                while (!mo.isEmpty()) {
                    searchMarkers.add(map.addMarker(mo.remove(0)));
                }
            });
        });
    }


    private void addBuildings(@NonNull GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> buildList = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/buildingGet.php");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {
                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"), stopObject.getDouble("longitude"));

                        String name = stopObject.getString("name");
                        String nickname = stopObject.getString("nickname");
                        String category = stopObject.getString("category");
                        String frequency = stopObject.getString("frequency");

                        if (nickname.equals("null")) {
                            nickname = "";
                        }
                        BitmapDescriptor icon;
                        switch (category) {
                            case "housing":
                                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.housing);
                                Bitmap sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;

                            case "academic":
                                b = BitmapFactory.decodeResource(getResources(), R.drawable.appicon512);
                                if (frequency.equals("10")) {
                                    sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                }
                                else if (frequency.equals("5")) {
                                    sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                }
                                else {
                                    sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                }
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;

                            case "auxiliary":
                                b = BitmapFactory.decodeResource(getResources(), R.drawable.star);
                                if (frequency.equals("10")) {
                                    sb = Bitmap.createScaledBitmap(b, 120, 120, false);
                                }
                                else if (frequency.equals("5")) {
                                    if (name.equals("Barnes & Noble")) {
                                        sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                    }
                                    else {
                                        sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                    }
                                }
                                else {
                                    sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                }
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;

                            case "athletics":
                                b = BitmapFactory.decodeResource(getResources(), R.drawable.furmandiamond);
                                sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
                                break;

                            default:
                                b = BitmapFactory.decodeResource(getResources(), R.drawable.purple_dot);
                                if (frequency.equals("10")) {
                                    sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                }
                                else if (frequency.equals("5")) {
                                    sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                }
                                else {
                                    sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                }
                                break;
                        }
                        buildList.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));
                    }
                }
            }

            catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                while (!buildList.isEmpty())
                    mapMarkers.add(googleMap.addMarker(buildList.remove(0)));
            });
        });
    }


    private void addFood(@NonNull GoogleMap googleMap) {
        int boo = getZoom(googleMap);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> foodList = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/restaurantGet.php");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {
                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"), stopObject.getDouble("longitude"));
                        String name = stopObject.getString("fullname");
                        String nickname = stopObject.getString("name");
                        String frequency = stopObject.getString("frequency");
                        if (nickname.equals("null")) {
                            nickname = "";
                        }
                        BitmapDescriptor icon;
                        if (boo == -1) {
                            switch (frequency) {
                                case "10":
                                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.dining);
                                    Bitmap sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                                case "5":
                                    b = BitmapFactory.decodeResource(getResources(), R.drawable.dining);
                                    sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                                default:
                                    b = BitmapFactory.decodeResource(getResources(), R.drawable.green_dining);
                                    if (name.equals("Bread and Bowl")) {
                                        sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                    }
                                    else {
                                        sb = Bitmap.createScaledBitmap(b, 1, 1, false);
                                    }
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                            }
                        }
                        else {
                            switch (frequency) {
                                case "10":
                                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.dining);
                                    Bitmap sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                                case "5":
                                    b = BitmapFactory.decodeResource(getResources(), R.drawable.dining);
                                    sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                                default:
                                    b = BitmapFactory.decodeResource(getResources(), R.drawable.green_dining);
                                    if (name.equals("Bread and Bowl")) {
                                        sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                    }
                                    else {
                                        sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                    }
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;
                            }
                        }
                        foodList.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                while (!foodList.isEmpty())
                    mapMarkers.add(googleMap.addMarker(foodList.remove(0)));
            });
        });
    }

    public boolean isZoomSupported(){
        return true;
    }

    public int getZoom(@NonNull GoogleMap googleMap){
        if (isZoomSupported()) {
            zoom = googleMap.getCameraPosition().zoom;
            if (zoom <= 16) {
                return -1;
            }
            else if (zoom > 16)
                return 1;
            //Toast.makeText(requireContext(), String.valueOf(zoom), Toast.LENGTH_SHORT).show();
        }
        return 0;
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

}





