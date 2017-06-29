package com.satyre.cpmaptest;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
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

import static com.satyre.cpmaptest.MainActivity.LOG_TAG;

/**
 * Created by Satyre on 29/06/2017.
 */

public abstract class MapBoxClass extends AppCompatActivity {

    public MapView mapView;
    public GenericAdapter<CacheAddress> listAdapter;
    private GeocoderAutoCompleteView autocompleteView;
    public MapboxMap map;
    private MarkerView pickupMarker, arrivalMarker;

    public MapBoxClass() {
    }

    public void addMarker(final LatLng pinLocation, String address) {
        //Add Pick up marker and force focus on it
        IconFactory iconFactory = IconFactory.getInstance(MapBoxClass.this);

        if (pickupMarker == null) {

            Icon iconPickup = iconFactory.fromResource(R.drawable.pickup_img);

            // Build pickupMarker
            pickupMarker = map.addMarker(new MarkerViewOptions()
                    .position(pinLocation)
                    .title(getString(R.string.pick_up))
                    .snippet(address)
                    .icon(iconPickup));

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
            } else {
                Icon iconArrival = iconFactory.fromResource(R.drawable.flag_arrival);
                arrivalMarker = map.addMarker(new MarkerViewOptions()
                        .position(pinLocation)
                        .title(getString(R.string.arrival))
                        .icon(iconArrival));
            }

        }

        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                return false;
            }
        });
        addAddressToDB(address, pinLocation);
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

    //set Mapview parameters like : showing location and camera position
    void setMapViewAndAutoCompleteView() {
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

    private void hideOnScreenKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    MapboxMap getMap() {
        return map;
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

}
