package com.satyre.cpmaptest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.orm.SugarRecord;

import java.util.List;

public class MainActivity extends MapBoxClass {

    private final static String MAPBOX_KEY = "pk.eyJ1Ijoic2F0eXJlIiwiYSI6ImNqNGVpbW96aDE1N2UzM2x2aDRra28zeDUifQ.hZ5xMiJnOkqU2QR-muCiBQ";
    public static final String LOG_TAG = "MAIN_ACTIVITY";
    public static final int ACCESS_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        //Set Map
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        checkLocationPermission();
        setMapViewAndAutoCompleteView();

        drawerListView.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();
    }


    //check and grant location permissions
    public void checkLocationPermission() {
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

