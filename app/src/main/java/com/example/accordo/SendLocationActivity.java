package com.example.accordo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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
    private Boolean locationServicesEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createMap(savedInstanceState);

        setToolbar();

        // Callback chiamata quando si cambia posizione. Sposta di conseguenza la camera
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(TAG, "Nessuna posizione");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        setCameraPosition(currentLocation);
                        findViewById(R.id.sendLocationFab).setVisibility(View.VISIBLE);
                    } else {
                        Log.e(TAG, "Ultima posizione non disponibile");
                    }
                }
            }
        };

        // Gestore evento di click sul bottone di invio della posizione
        findViewById(R.id.sendLocationFab).setOnClickListener(v -> {
            CommunicationController cc = new CommunicationController(this);
            try {
                cc.addPost(getIntent().getStringExtra("ctitle"),
                        String.valueOf(currentLocation.getLatitude()),
                        String.valueOf(currentLocation.getLongitude()), Post.LOCATION,
                        response -> super.onBackPressed(),
                        error -> {
                            Context context = getApplicationContext();
                            CharSequence text = getString(R.string.error_sending_location);
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

    /**
     * Inserisce nella toolbar il bottone per tornare indietro della ActionBar
     */
    private void setToolbar() {
        androidx.appcompat.widget.Toolbar myToolbar = findViewById(R.id.sendLocationToolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     * Imposta il titolo della toolbar con il nome dell'utente che ha inviato la posizione
     * @param post
     */
    private void setToolbarTitle(Post post) {
        if (post.getName() != null) {
            getSupportActionBar().setTitle("Posizione di " + post.getName());
        } else {
            getSupportActionBar().setTitle("Posizione di " + getResources().getString(R.string.unknown_author));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Prende la {@link MapView} e ne prende la mappa salvandola in {@link #myMapboxMap}.
     * Chiama {@link #setMapBehavior()} e {@link #mapStyle()}
     * @param savedInstanceState
     */
    private void createMap(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_send_location);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            myMapboxMap = mapboxMap;
            mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> { });
            setMapBehavior();
            myMapboxMap.setStyle(Style.MAPBOX_STREETS, style -> mapStyle());
        });
    }

    /**
     * Imposta lo stile della mappa
     */
    private void mapStyle() {
        UiSettings uiSettings = myMapboxMap.getUiSettings();

        uiSettings.setCompassEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomGesturesEnabled(true);
        //uiSettings.setQuickZoomGesturesEnabled(true);
    }

    /**
     * In base al contenuto dell'intent mostra la posizione attuale dell'utente o quella di un post
     */
    private void setMapBehavior() {
        int index = getIntent().getIntExtra("postIndex", -1);
        if (index == -1) {
            getSupportActionBar().setTitle(R.string.send_location_activity_title);
            askToTurnOnLocation();
        } else {
            setCameraAtPostPosition(index);     // Imposta la mappa sulla posizione del post
        }
    }

    private void askToTurnOnLocation() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) { }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) { }

        if (!gps_enabled && !network_enabled) {
            // Notifica l'utente
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
            // Setta l'interfaccia
            builder.setTitle(R.string.disabled_location_services);
            builder.setMessage(R.string.gps_network_not_enabled);
            // Gestori eventi sui pulsanti del dialog
            builder.setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) -> {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                locationServicesEnabled = true;
            });
            builder.setNegativeButton(R.string.Cancel,null);
            builder.show();
        } else {
            getLastLocation();
        }
    }

    /**
     * Prende latiudine e longitudine del post e chiama {@link #setCameraPosition(LatLng)}
     * @param index Indice del post di tipo posizione che è stato cliccato
     */
    private void setCameraAtPostPosition(int index) {
        Post post = Model.getInstance(this).getPost(index);

        setToolbarTitle(post);
        String lat = ((LocationPost) post).getLat();
        String lon = ((LocationPost) post).getLon();

        Double dLat = Double.valueOf(lat);
        Double dLon = Double.valueOf(lon);

        setCameraPosition(new LatLng(dLat, dLon));
    }

    /**
     * Mette il marker (icona) sulla posizione
     * @param latLng
     */
    private void setMarker(LatLng latLng) {
        IconFactory iconFactory = IconFactory.getInstance(SendLocationActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.mapbox_marker_icon_default);

        myMapboxMap.addMarker(new MarkerOptions()
                .position(latLng)
                .icon(icon));
    }

    /**
     * Controlla i permessi di posizione e mostra il dialog per concederli nel caso in cui non sia
     * già stato fatto. Se sono stati già concessi chiama {@link #setFusedLocation()}
     */
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

    /**
     * Inizializza il client provider per la posizione
     */
    private void setFusedLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestingLocationUpdates = true;
        startLocationUpdates();
    }

    /**
     * Chiamato quando l'utente concede i permessi tramite il dialog, di conseguenza chiama
     * {@link #setFusedLocation()}
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Sposta la camera della mappa nella latitudine e longitudine specificati
     * @param latLng
     */
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

    /**
     * Registrazione per ricevere gli aggiornamenti in tempo reale della posizione
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();     // Serve per specificare i parametri della richiesta di posizione
        locationRequest.setInterval(1000);                              // Intervallo in millisecondi
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

    /**
     * Fa ripartire gli aggiornamenti della posizione, se ci si era registrari per riceverli
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (locationServicesEnabled) {
            askToTurnOnLocation();
        }

        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Interrompe gli aggiornamenti della posizione, se ci si è registrati per riceverli
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (requestingLocationUpdates) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
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