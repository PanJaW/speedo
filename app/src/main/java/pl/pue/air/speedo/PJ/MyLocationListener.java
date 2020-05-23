package pl.pue.air.speedo.PJ;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class MyLocationListener implements LocationListener {

    private final MainActivity mainActivity;

    public MyLocationListener(MainActivity mainActivity){
        this.mainActivity=mainActivity;
    }

    @Override
    public void onLocationChanged(Location location) {
        mainActivity.onLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        mainActivity.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}