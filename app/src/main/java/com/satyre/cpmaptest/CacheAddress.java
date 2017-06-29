package com.satyre.cpmaptest;

import com.orm.dsl.Table;
import com.orm.dsl.Unique;

/**
 * Created by Satyre on 29/06/2017.
 */

@Table
public class CacheAddress {

    @Unique
    Long id;
    private String displayTxt;
    private double latitude;
    private double longitude;

    public CacheAddress() {
    }

    public CacheAddress(String displayTxt, double latitude, double longitude) {
        this.displayTxt = displayTxt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDisplayTxt() {
        return displayTxt;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
