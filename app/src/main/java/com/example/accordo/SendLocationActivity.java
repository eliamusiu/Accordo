package com.example.accordo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;

public class SendLocationActivity extends AppCompatActivity {
    private static final String TAG = SendLocationActivity.class.toString();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = false;
    private MapView mapView;
    private MapboxMap myMapboxMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createMap(savedInstanceState);
        //getLastLocation();
        getLocation();
    }

    private void createMap(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_send_location);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                myMapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                    }
                });
                getLastLocation();      // Prende l'ultima posizione nota dell'utente e aggiorna la camera della mappa
                myMapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        UiSettings uiSettings = myMapboxMap.getUiSettings();

                        uiSettings.setCompassEnabled(true);
                        uiSettings.setAllGesturesEnabled(true);
                        uiSettings.setZoomGesturesEnabled(true);
                        uiSettings.setQuickZoomGesturesEnabled(true);
                    }
                });
            }
        });
    }

    private void getLocation() {
        int index = getIntent().getIntExtra("postIndex", -1);
        String lat = ((LocationPost) Model.getInstance().getPost(index)).getLat();
        String lon = ((LocationPost) Model.getInstance().getPost(index)).getLon();

        Double dLat = Double.valueOf(lat);
        Double dLon = Double.valueOf(lon);

        setCameraPosition(new LatLng(dLat, dLon));

    }

    private void setMarker(LatLng latLng) {
        IconFactory iconFactory = IconFactory.getInstance(SendLocationActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);

        // Add the marker to the map
        myMapboxMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(icon));
        myMapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Log.d(TAG, "Marker Cliccato");
                return true;
            }
        });

    }

    private void getLastLocation() {
        // L'utente non ha concesso i permessi
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            Log.d(TAG, "Permessi richiesti");
            setCameraPosition(new LatLng(45.476297, 9.231994));
        } else {                                                    // L'utente ha concesso i permessi
            Log.d(TAG, "Permessi concessi");
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            requestingLocationUpdates = true;
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    });
        }
    }

    private void setCameraPosition(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(14)
                .tilt(0)
                .bearing(0)
                .build();
        myMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000);
        setMarker(latLng);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}