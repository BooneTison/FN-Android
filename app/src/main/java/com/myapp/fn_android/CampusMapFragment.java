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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    double userLatitude;
    double userLongitude;
    List<String[]> searchBarList;
    LatLng furman = new LatLng(latitude, longitude);
    final int shuttleIconWidth = 100;
    final int shuttleIconHeight = 100;



    AthleticsRecyclerViewAdapter buildAdapter;
    AthleticsRecyclerViewAdapter dinAdapter;
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

            if (!buildingName.equals("")) {
                googleMap.addMarker(new MarkerOptions().position(furman).title(buildingName).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE)));
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(furman, 16));
            } else {

                addBuildings(googleMap);
                addFood(googleMap);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(furman, 16));

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
                            if (nickname.equals("null")){
                                nickname="";
                            }
                            BitmapDescriptor icon;
                            switch (category) {
                                case "academic":
                                    Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.green_dot);
                                    Bitmap sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                   // academics=icon;
                                    break;
                                case "auxiliary":
                                  b = BitmapFactory.decodeResource(getResources(),R.drawable.blue_dot);
                                    sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    //auxiliary=icon;
                                    break;
                                case "athletics":
                                    b = BitmapFactory.decodeResource(getResources(),R.drawable.purple_dot);
                                    sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    //athletics=icon;
                                    break;
                                case "housing":
                                    b = BitmapFactory.decodeResource(getResources(),R.drawable.yellow_dot);
                                    sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    //housing=icon;
                                    break;
                                default:
                                     b = BitmapFactory.decodeResource(getResources(),R.drawable.orange_dot);
                                     sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                    icon = BitmapDescriptorFactory.fromBitmap(sb);
                                    break;
                            }

                            list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));

                            }
                        //}
                      /*  else {
                            String name = stopObject.getString("name");
                            String nickname = stopObject.getString("nickname");
                            if (nickname.equals("null")) {
                                nickname = "";
                            }
                            BitmapDescriptor icon;
                            switch (nickname) {
                                default:
                                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                                    break;
                            }
                            list.add(new MarkerOptions().position(loc).title(name).icon(icon).snippet(nickname));
                        }*/
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
                        LatLng loc = new LatLng(stopObject.getDouble("latitude"),stopObject.getDouble("longitude"));
                        String name = stopObject.getString("fullname");
                        String nickname = stopObject.getString("name");
                        String location=stopObject.getString("location");
                        if (nickname.equals("null")){
                            nickname="";
                        }
                        BitmapDescriptor icon;
                        switch (nickname) {
                            case "in the PalaDen, lower level of Trone":
                                Bitmap b = BitmapFactory.decodeResource(getResources(),R.drawable.red_dot);
                                Bitmap sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);

                            default:
                                 b = BitmapFactory.decodeResource(getResources(),R.drawable.red_dot);
                                 sb = Bitmap.createScaledBitmap(b,shuttleIconWidth,shuttleIconHeight,false);
                                icon = BitmapDescriptorFactory.fromBitmap(sb);
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




