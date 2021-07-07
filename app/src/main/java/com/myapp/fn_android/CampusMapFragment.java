package com.myapp.fn_android;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class CampusMapFragment extends Fragment {
    double latitude = 34.9246422; // Default lat, Library
    double longitude = -82.4390771; // Default long, Library
    String buildingName = ""; // Default name

    private GoogleMap mMap;

    // creating a variable
    // for search view.
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
            LatLng furman = new LatLng(latitude, longitude);
            if (!buildingName.equals("")) {
                googleMap.addMarker(new MarkerOptions().position(furman).title(buildingName).icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE)));
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(furman));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(furman, 16));
        }
    };

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
}



