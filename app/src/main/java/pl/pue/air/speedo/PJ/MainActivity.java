package pl.pue.air.speedo.PJ;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {


    private Integer data_points = 2;
    private Double[][] positions;
    private Long[] times;
    private Integer counter = 0;
    private boolean dataInitiated = false;

    private LocationManager lm;
    private LocationListener locationListener;

    private TextView textViewAppName;
    private TextView textViewCurrentSpeed;
    private TextView textViewMaxSpeed;
    private TextView textViewDistance;
    private TextView textViewNoOfSatellites;
    private Button buttonResetDistance;
    private Button buttonResetMaxSpeed;

    private int speed = -1;
    private float maxSpeed = -1.0F;
    private int distance = 0;
    private int satellites = 0;

    public static String LOG = "SPEEDO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewAppName = findViewById(R.id.textViewAppName);
        textViewDistance = findViewById(R.id.textViewDistance);
        textViewCurrentSpeed = findViewById(R.id.textViewCurrentSpeed);
        textViewMaxSpeed = findViewById(R.id.textViewMaxSpeed);
        textViewNoOfSatellites = findViewById(R.id.textViewNoOfSatellites);

        buttonResetDistance = findViewById(R.id.buttonResetDistance);
        buttonResetMaxSpeed = findViewById(R.id.buttonResetMaxSpeed);


        if (!dataInitiated) {
            positions = new Double[data_points][2];
            times = new Long[data_points];
            dataInitiated = true;
            Log.i(LOG, getString(R.string.on_data_init));
        }

        //asking for the rights
        // uncomment below code for API 23 and higher!
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 223344);//any integer different of the others!!
            }
        }

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(this);

        buttonResetDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distance = 0;
            }
        });

        buttonResetMaxSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxSpeed = 0.0f;
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Log.e(LOG, getString(R.string.access_rights_NOT_granted));
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int p = 0; p < permissions.length; p++) {
            if (checkSelfPermission(permissions[p]) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.d(LOG, getString(R.string.access_rights_granted) + ": " + p);
            } else {
                Log.e(LOG, getString(R.string.access_rights_NOT_granted) + ": " + p);
            }
        }
    }

    public void onLocationChanged(Location loc){
        if (loc != null) {
            Double d1;
            Long t1;
            Double speed = 0.0;
            d1 = 0.0;
            t1 = 0l;
            positions[counter][0] = loc.getLatitude();
            positions[counter][1] = loc.getLongitude();
            times[counter] = loc.getTime();
            try {
                d1 = distance( positions[counter][0],
                        positions[counter][1],
                        positions[(counter + (data_points - 1)) %
                                data_points][0],
                        positions[(counter + (data_points - 1)) %
                                data_points][1]);
                t1 = times[counter] - times[(counter + (data_points - 1)) %
                        data_points];
            } catch (NullPointerException e) {

            }
            if (loc.hasSpeed()) {
                speed = loc.getSpeed() * 1.0;
                // need to * 1.0 to get into a double for some reason...
            } else {
                speed = d1 / t1; // m/s
            }
            counter = (counter + 1) % data_points;
            // convert from m/s to kmh
            speed = speed * 3.6d;
            setAndDisplayCurrentSpeed(speed);
            addAndDisplayCurrentDistance(d1);
        } else {
            setAndDisplayCurrentSpeed(-1.0);
        }
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i(getResources().getString(R.string.app_name),
                getString(R.string.app_status_changed) + ": " + extras.toString());
        if (extras.get("satellites") != null) {
            setSatellitesAndDisplayTheirNumber(extras.getInt("satellites"));
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                        Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60;
        dist = dist * 1852;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void setAndDisplayCurrentSpeed(Double speed) {
        if (speed != null) {
            this.speed = speed.intValue();
            boolean maxSpeedChanged = false;
            if (this.speed >= maxSpeed) {
                maxSpeed = this.speed;
                maxSpeedChanged = true;
            }
            if (textViewCurrentSpeed != null) {
                textViewCurrentSpeed.setText(
                        getString(R.string.current_speed) + ": " + this.speed + ' '
                                + getString(R.string.kmh));
            }
            if (textViewMaxSpeed != null && maxSpeedChanged) {
                textViewMaxSpeed.setText(
                        getString(R.string.max_speed) + ": " + this.maxSpeed + ' '
                                + getString(R.string.kmh));
            }
        }
    }

    private void addAndDisplayCurrentDistance(Double distance) {
        if (distance != null) {
            this.distance = this.distance + distance.intValue();
            if (textViewDistance != null) {
                textViewDistance.setText(
                        getString(R.string.current_distance) +
                                ": " + this.distance + ' ' + getString(R.string.m));
            }
        }
    }

    private void setSatellitesAndDisplayTheirNumber(int satellites) {
        this.satellites = satellites;
        if (textViewNoOfSatellites != null) {
            //not all providers will notify this change
            textViewNoOfSatellites.setText(
                    getString(R.string.no_of_satellites) + ": " + this.satellites);
        }
    }

}


