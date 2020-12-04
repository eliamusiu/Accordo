package com.example.accordo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

import org.json.JSONException;

public class SendLocationActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = SendLocationActivity.class.toString();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean requestingLocationUpdates = false;
    private MapView mapView;
    private MapboxMap myMapboxMap;
    private LocationCallback locationCallback;
    private LatLng currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createMap(savedInstanceState);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "Nessuna posizione");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        setCameraPosition(currentLocation);
                        findViewById(R.id.sendLocationFab).setVisibility(View.VISIBLE);

                    } else {
                        Log.d(TAG, "ultima posizione non disponibile");
                    }

                }
            }
        };

        findViewById(R.id.sendLocationFab).setOnClickListener(v -> {
            CommunicationController cc = new CommunicationController(this);
            try {
                cc.addPost(getIntent().getStringExtra("ctitle"),
                        String.valueOf(currentLocation.getLatitude()),
                        String.valueOf(currentLocation.getLongitude()),
                        "l",
                        response -> super.onBackPressed(),
                        error -> {
                            Context context = getApplicationContext();
                            CharSequence text = "Errore invio posizione"; //TODO: fare strings
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context, text, duration);toast.show();
                            Log.e(TAG, "Errore invio posizione: " + error.networkResponse);
                            super.onBackPressed();
                        });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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
                //getLastLocation();      // Prende l'ultima posizione nota dell'utente e aggiorna la camera della mappa
                setMapBehavior(); // in base al contenuto dell'intent viene mostrata la posizione attuale dell'utente o quella di un post
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

    private void setMapBehavior() {
        int index = getIntent().getIntExtra("postIndex", -1);
        if (index == -1) {
            getLastLocation(); // imposta la mappa sulla posizione dell'utente per l'invio
        } else {
            setCameraAtPostPosition(index); // imposta la mappa sulla posizione del post
        }
    }

    private void setCameraAtPostPosition(int index) {
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
        } else {  // L'utente ha concesso i permessi
            Log.d(TAG, "Permessi concessi");
            setFusedLocation();
        }
    }

    private void setFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestingLocationUpdates = true;
        startLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setFusedLocation();
                } else {
                    requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            }
        }
    }

    private void setCameraPosition(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(11)
                .tilt(0)
                .bearing(0)
                .build();
        myMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1000);
        setMarker(latLng);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();     // Serve per specificare i parametri della richiesta di posizione
        locationRequest.setInterval(1000); //in ms.
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationClient.requestLocationUpdates(     // Fa partire il calcolo della posizione:
                locationRequest,                        // 1) secondo questi parametri
                locationCallback,                       // 2) richiamando il metodo al suo interno
                Looper.getMainLooper());
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
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (requestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback);    // Interrompe gli aggiornamenti in background della posizione
            requestingLocationUpdates = false;
        }
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