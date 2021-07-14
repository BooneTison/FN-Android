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
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

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

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    double userLatitude;
    double userLongitude;
    List<String[]> searchBarList;
    LatLng furman = new LatLng(latitude, longitude);
    final int BigWidth = 100;
    final int BigHeight = 100;
    final int medWidth= 75;
    final int medHeight= 75;
    final int smallWidth= 50;
    final int smallHeight= 50;
    final int tinyHeight=35;
    final int tinyWidth=35;
    // Declare a variable for the cluster manager.
    //private ClusterManager<MyItem> clusterManager;
    List<Marker> placedMarkers = new ArrayList<>();
    float zoom;
    boolean level;


    SearchView searchView;


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
            boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
            .getString(R.string.style_json))); // Remove built-in points of interest
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                OnGPS();
            } else {
                getLocation();
            }

           //LatLng furman = new LatLng(latitude, longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(furman));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(furman, 16));

            if (!buildingName.equals("")) {
                googleMap.addMarker(new MarkerOptions().position(furman).title(buildingName).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE)));

            } else {

                final Handler handler = new Handler();
                final int delay = 1000; // 1000 milliseconds == 1 second
                handler.postDelayed(new Runnable() { // Runs every interval according to delay
                    @Override
                    public void run() {
                        addBuildings(googleMap);
                        addFood(googleMap);
                        //Toast.makeText(requireContext(), "10 Seconds", Toast.LENGTH_SHORT).show();
                        handler.postDelayed(this, delay);
                    }
                }, delay);



            }

            LatLng curPos = new LatLng(userLatitude, userLongitude);
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.paladinx3);
            Bitmap sb = Bitmap.createScaledBitmap(b, 75, 75, false);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(sb);
            googleMap.addMarker(new MarkerOptions().position(curPos).title("Current Position").icon(icon));





        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        setHasOptionsMenu(true);
    }

    @Override
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
                filter(newText,searchBarList, 0);
                return false;
            }
        });
    }
    private void filter(String text, List<String[]> list, int type) {
        List<String[]> filteredlist = new ArrayList<>();
        for (String[] arr : list) {
            if (arr[0].toLowerCase().contains(text.toLowerCase())) {
                filteredlist.add(arr);
            }    }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }


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
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                userLatitude = locationGPS.getLatitude();
                userLongitude = locationGPS.getLongitude();
                //String text = "Lat: " + latitude + " Long: " + longitude;
                //Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void searchBAR(@NonNull GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> list = new ArrayList<>();
            List<Marker> placedMarkers = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/buildingGet.php");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {
                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"),stopObject.getDouble("longitude"));
                        String name = stopObject.getString("fullname");
                        String nickname = stopObject.getString("name");
                        if (nickname.equals("null")){
                            nickname="";
                        }
                        BitmapDescriptor icon;
                        switch (nickname) {
                            default:
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                                break;
                        }
                        list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            handler.post(() -> {
                while (!list.isEmpty())
                    googleMap.addMarker(list.remove(0));
                while (!list.isEmpty())
                    placedMarkers.add(googleMap.addMarker(list.remove(0)));

            });
        });
    }


private void addBuildings(@NonNull GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> list = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/buildingGet.php");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {
                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"),stopObject.getDouble("longitude"));

                        //String hours= stopObject.getString("hasHours");
                       // if(hours.equals("0")){
                            String name = stopObject.getString("name");
                            String nickname = stopObject.getString("nickname");
                            String category=  stopObject.getString("category");
                            String location= stopObject.getString("location");
                            String frequency= stopObject.getString("frequency");

                            if (nickname.equals("null")){
                                nickname="";
                            }
                            BitmapDescriptor icon;
                            switch (category) {
                                case "housing":
                                    Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.housing);
                                        Bitmap sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;
                                case "academic":
                                   b = BitmapFactory.decodeResource(getResources(),R.drawable.appicon512);
                                    if (frequency.equals("10")) {
                                        sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);

                                    }
                                    if(frequency.equals("5")){
                                        sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);

                                    }
                                    else{
                                        sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);

                                    }
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;
                                case "auxiliary":
                                  b = BitmapFactory.decodeResource(getResources(),R.drawable.star);
                                    if (frequency.equals("10")) {

                                        sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);

                                    }
                                    if(frequency.equals("5")){
                                        if(name.equals("Barnes & Noble")){
                                            sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);


                                        }
                                        else {
                                            sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);

                                        }

                                    }
                                    else{
                                       sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);

                                    }
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;
                                case "athletics":
                                    b = BitmapFactory.decodeResource(getResources(),R.drawable.furmandiamond);
                                       sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                                default:
                                     b = BitmapFactory.decodeResource(getResources(),R.drawable.purple_dot);
                                    if (frequency.equals("10")) {
                                        sb = Bitmap.createScaledBitmap(b, BigWidth, BigHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    }
                                    if(frequency.equals("5")){
                                        sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    }
                                    else{
                                        sb = Bitmap.createScaledBitmap(b, smallWidth, smallHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    }
                                    break;
                            }

                            list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));

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


    private void addFood(@NonNull GoogleMap googleMap) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            List<MarkerOptions> list = new ArrayList<>();
            try {
                String service = makeServiceCall("https://cs.furman.edu/~csdaemon/FUNow/restaurantGet.php");
                if (!service.equals("]")) {
                    JSONArray stopArray = new JSONArray(service);
                    for (int i = 0; i < stopArray.length(); i++) {

                        JSONObject stopObject = stopArray.getJSONObject(i);
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"), stopObject.getDouble("longitude"));
                        String name = stopObject.getString("fullname");
                        String nickname = stopObject.getString("name");
                        String location = stopObject.getString("location");
                        String frequency= stopObject.getString("frequency");
                        if (nickname.equals("null")) {
                            nickname = "";
                        }
                        BitmapDescriptor icon;

                            switch (frequency) {

                                case "10":
                                   Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.dining);
                                   Bitmap sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                   icon = BitmapDescriptorFactory.fromBitmap(sb);
                                   break;

                                case "5":
                                    b = BitmapFactory.decodeResource(getResources(), R.drawable.dining);
                                    sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;

                                default:
                                    b = BitmapFactory.decodeResource(getResources(), R.drawable.green_dining);
                                    if(name.equals("Bread and Bowl")){
                                        sb = Bitmap.createScaledBitmap(b, medWidth, medHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);

                                    }
                                    else {
                                        sb = Bitmap.createScaledBitmap(b, tinyWidth, tinyHeight, false);
                                        icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    }
                                    break;

                            }
                            list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));




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


    /*public boolean isZoomSupported(){

        return true;
    }


    public boolean getZoom(@NonNull GoogleMap googleMap){

        if (isZoomSupported()) {
            zoom = googleMap.getCameraPosition().zoom;
            if (zoom <= 16) {
                return true;
            }
        }
        return false;
    }
*/

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
   /* public static class MyItem implements ClusterItem {
        private final LatLng position;
        private final String title;
        private final String snippet;



        public MyItem(double lat, double lng, String title, String snippet) {
            position = new LatLng(lat, lng);
            this.title = title;
            this.snippet = snippet;
        }

        @Override
        public LatLng getPosition() {
            return position;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getSnippet() {
            return snippet;
        }
    }*/

    /*private void setUpClusterer(@NonNull GoogleMap googleMap) {
        // Position the map.
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(furman, 16));
        LatLng sydney = new LatLng(34.9245, -82.4405);
        googleMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        /*// Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        clusterManager = new ClusterManager<MyItem>(requireActivity(), googleMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        //clusterManager.setAnimation(false);

        // Add cluster items (markers) to the cluster manager.
        addItems();
    }

    private void addItems() {
        double lat=34.9245;
        double lng=-82.4405;
        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 10; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            // Set the title and snippet strings.
            String title = "This is the title";
            String snippet = "and this is the snippet.";
            // Create a cluster item for the marker and set the title and snippet using the constructor.
            MyItem infoWindowItem = new MyItem(lat, lng, title, snippet);
            MyItem offsetItem = new MyItem(lat, lng, title, snippet);
            clusterManager.addItem(offsetItem);
            // Add the cluster item (marker) to the cluster manager.
            clusterManager.addItem(infoWindowItem);
        }*/
    }





