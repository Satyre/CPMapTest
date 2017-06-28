package com.satyre.cpmaptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.commons.models.Position;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "MAIN_ACTIVITY";
    public static final int ACCESS_LOCATION = 1;
    private final static String MAPBOX_KEY = "pk.eyJ1Ijoic2F0eXJlIiwiYSI6ImNqNGVpbW96aDE1N2UzM2x2aDRra28zeDUifQ.hZ5xMiJnOkqU2QR-muCiBQ";
    private MapView mapView;
    private MapboxMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), MAPBOX_KEY);

        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        checkLocationPermission();
        setMapViewAndAutoCompleteView();

    }

    //check and grant location permissions
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_LOCATION);
        }
    }

    //Permissions callback
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMapViewAndAutoCompleteView();

                } else {

                    //if location permission wasn't granted show an explanation and request again
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.alert_dial_mess);
                    builder.setCancelable(true);

                    builder.setPositiveButton(
                            R.string.alert_btn_yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    checkLocationPermission();
                                }
                            });

                    builder.setNegativeButton(
                            R.string.alert_btn_no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(MainActivity.this, R.string.location_toast_alert, Toast.LENGTH_LONG).show();
                                    dialog.cancel();
                                }
                            });

                    builder.create().show();
                }
            }
        }
    }

    //set Mapview parameters like : showing location and camera position
    private void setMapViewAndAutoCompleteView() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {
                map = mapboxMap;
                mapboxMap.setMyLocationEnabled(true);

                mapboxMap.setOnMyLocationChangeListener(new MapboxMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(@Nullable Location location) {
                        if (location != null) {
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                                    .zoom(17) // Sets the zoom
                                    .build();

                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position), 4000); // move camera on position in desire time (ms)
                        }
                        // Set up autocomplete widget
                        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.autoCompleteView);
                        autocomplete.setAccessToken(Mapbox.getAccessToken());
                        autocomplete.setType(GeocodingCriteria.TYPE_ADDRESS);
                        autocomplete.setCountry("FR");
                        if (location != null)
                            autocomplete.setProximity(Position.fromCoordinates(location.getLongitude(), location.getLatitude()));
                        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
                            @Override
                            public void onFeatureClick(CarmenFeature feature) {
                                hideOnScreenKeyboard();
                                Position position = feature.asPosition();
                                updateMap(position.getLatitude(), position.getLongitude(), feature.getPlaceName());
                            }
                        });
                    }

                });


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
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

    private void hideOnScreenKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    private void updateMap(double latitude, double longitude, String address) {
        // Build marker
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(address));

        Log.d(LOG_TAG, "address : " + address);
        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }
}

