package com.satyre.cpmaptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
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
import com.orm.SugarRecord;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "MAIN_ACTIVITY";
    public static final int ACCESS_LOCATION = 1;
    private final static String MAPBOX_KEY = "pk.eyJ1Ijoic2F0eXJlIiwiYSI6ImNqNGVpbW96aDE1N2UzM2x2aDRra28zeDUifQ.hZ5xMiJnOkqU2QR-muCiBQ";
    GeocoderAutoCompleteView autocompleteView;
    private MapView mapView;
    private MapboxMap map;
    private MarkerView pickupMarker, arrivalMarker;
    private GenericAdapter<CacheAddress> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), MAPBOX_KEY);

        setContentView(R.layout.activity_main);

        //Set toolbar and drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Set Map
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        checkLocationPermission();
        setMapViewAndAutoCompleteView();

        //Set history list
        List<CacheAddress> CacheAddresses = SugarRecord.listAll(CacheAddress.class);

        ListView drawerListView = (ListView) findViewById(R.id.historyList);
        listAdapter = new GenericAdapter<>(MainActivity.this, R.layout.drawer_cell, CacheAddresses, new GenericAdapter.AdapterListener<CacheAddress>() {

            @Override
            public void setView(View view, final CacheAddress object) {
                TextView tv = view.findViewById(R.id.drawer_cell_tv);

                tv.setText(object.getDisplayTxt());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //remove lock on markerPickup
                        map.setOnCameraChangeListener(null);

                        //Add marker from DB
                        addMarker(new LatLng(object.getLatitude(), object.getLongitude()), object.getDisplayTxt());

                        // Animate camera to geocoder result location
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(object.getLatitude(), object.getLongitude()))
                                .zoom(15)
                                .build();
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
                    }
                });
            }
        });
        drawerListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
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
                    public void onMyLocationChange(@Nullable final Location location) {
                        if (location != null) {
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude())) // Sets the new camera position
                                    .zoom(17) // Sets the zoom
                                    .build();

                            mapboxMap.animateCamera(CameraUpdateFactory
                                    .newCameraPosition(position), 4000); // move camera on position in desire time (ms)
                        }

                        // Set up autocompleteView widget
                        autocompleteView = (GeocoderAutoCompleteView) findViewById(R.id.autoCompleteView);
                        autocompleteView.setAccessToken(Mapbox.getAccessToken());
                        autocompleteView.setType(GeocodingCriteria.TYPE_ADDRESS);
                        autocompleteView.setCountry(getString(R.string.fr));
                        if (location != null)
                            autocompleteView.setProximity(Position.fromCoordinates(location.getLongitude(), location.getLatitude()));
                        autocompleteView.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
                            @Override
                            public void onFeatureClick(CarmenFeature feature) {
                                //On feature item click
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

    private void updateMap(double latitude, double longitude, String address) {
        final LatLng pinLocation = new LatLng(latitude, longitude);
        addMarker(pinLocation, address);
        onMapclick();
    }

    private void onMapclick() {
        map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                //Get address from marker location
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listAddresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                    if (null != listAddresses && listAddresses.size() > 0) {
                        String address = listAddresses.get(0).getAddressLine(0) + " "
                                + listAddresses.get(0).getPostalCode() + " " + listAddresses.get(0).getLocality();
                        addMarker(point, address);
                        autocompleteView.setText(address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void addMarker(final LatLng pinLocation, String address) {
        //Add Pick up marker and force focus on it
        if (pickupMarker == null) {
            // Build pickupMarker
            pickupMarker = map.addMarker(new MarkerViewOptions()
                    .position(pinLocation)
                    .title(getString(R.string.pick_up))
                    .snippet(address));

            // Animate camera to geocoder result location
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(pinLocation)
                    .zoom(15)
                    .build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);

            //Lock camera center on pickupMarker
            map.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition position) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(pinLocation)
                            .build();
                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 0, null);
                }
            });
            autocompleteView.setHint(R.string.select_drop_off);
        } else {
            //Add arrivalMarker or update is position
            if (arrivalMarker != null) {
                arrivalMarker.setPosition(pinLocation);
                arrivalMarker.setSnippet(address);
            } else
                arrivalMarker = map.addMarker(new MarkerViewOptions()
                        .position(pinLocation)
                        .title(getString(R.string.arrival)));
        }
        addAddressToDB(address, pinLocation);
    }

    private void addAddressToDB(String addressTxt, LatLng latLng) {
        //Remove previous address
        List<CacheAddress> cacheAddresses = SugarRecord.listAll(CacheAddress.class);
        if (cacheAddresses.size() >= 14) {
            listAdapter.clear();
            cacheAddresses.remove(SugarRecord.first(CacheAddress.class));
            listAdapter.addAll(cacheAddresses);
            listAdapter.notifyDataSetChanged();
            SugarRecord.delete(SugarRecord.first(CacheAddress.class));

            Log.d(LOG_TAG, "first item : " + SugarRecord.first(CacheAddress.class).getDisplayTxt());
        }

        //Add address to DB
        CacheAddress address = new CacheAddress(addressTxt, latLng.getLatitude(), latLng.getLongitude());
        listAdapter.add(address);
        listAdapter.notifyDataSetChanged();
        SugarRecord.save(address);


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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

