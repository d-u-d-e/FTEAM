package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;

import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ActivitySetLocation extends AppCompatActivity implements OnMapReadyCallback {

    public final String LONGITUDE = "longitude";
    public final String LATITUDE = "latitude";
    public final String RADIUSVALUE = "radiusValue";


    private GoogleMap mMap;
    private String latitude;
    private String longitude;
    private FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderClient mFusedLastLocationClient;
    LocationManager mLocationManager;
    LocationCallback mLocationCallback;
    private LatLng myPosition;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FloatingActionButton position_fab;

    //Layout elements
    private SeekBar radius;
    private Button save;
    private TextView distance;
    private String distaceValue;
    private boolean mapReady = false;
    private boolean isTracking = false;
    private double i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_location);

        Toolbar toolbar = findViewById(R.id.location_maps_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.setting_map);
        mapFragment.getMapAsync(this);


        position_fab = findViewById(R.id.setting_position_fab);
        distance = findViewById(R.id.radius);
        radius = findViewById(R.id.seek_bar);
        save = findViewById(R.id.save_button);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //create a callback when receive a location result
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                myPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                if(mapReady){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
                }
            }
        };


        //check if the GPS is disabled
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //if the GPS is disabled show a Toast
            Toast.makeText(ActivitySetLocation.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
            position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
        } else {
            position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
        }


        position_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    //if the GPS is disabled show a Toast or an AlertDialog
                    Toast.makeText(ActivitySetLocation.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
                    position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
                }else {
                    // Go to the new view in the map and change the color
                    if(!isTracking){
                        startTrackingLocation();
                    }
                    mMap.clear();
                    position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                    radius.setProgress(0);
                }

            }
        });



        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isTracking){
                    stopTrackingLocation();
                }
                if(myPosition == null){
                    Toast.makeText(ActivitySetLocation.this, "Enable GPS to set your position", Toast.LENGTH_SHORT).show();
                } else{
                    drawCircle(mMap, myPosition, progress * 500);
                    if(progress <= 10){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
                    } else if(progress > 10 && progress < 20){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 11));
                    } else if(progress >= 20 && progress < 40){
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 10));
                    }else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 9));
                    }
                    i = progress * 0.5;
                    distance.setText(i + " km");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myPosition == null || i <= 0){
                    Toast.makeText(ActivitySetLocation.this, R.string.complete_preferences, Toast.LENGTH_SHORT).show();
                } else {
                    latitude = String.valueOf(myPosition.latitude);
                    longitude = String.valueOf(myPosition.longitude);
                    distaceValue = String.valueOf(i * 1000);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(LATITUDE, latitude);
                    editor.putString(LONGITUDE, longitude);
                    editor.putString(RADIUSVALUE, distaceValue);

                    editor.commit();
                    //TODO: delete the toast and add a finish() to go back in ActivityMain
                    Toast.makeText(ActivitySetLocation.this, "Preferences Saved!", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        enableMyLocation();
        onMapMoving(mMap);
        setMapLongClick(mMap);
        if(myPosition != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
        }
        if(!isTracking){
            startTrackingLocation();
        }

    }

    // onMapMoving stops the tracking location if the user starts moving the camera.
    private void onMapMoving(GoogleMap map){
        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                    if(isTracking){
                        stopTrackingLocation();
                    }
                }
            }
        });
    }


    //If the user make a long click on the map, add in that position a marker and show an info window
    private void setMapLongClick(final GoogleMap map){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(isTracking){
                    stopTrackingLocation();
                }
                map.clear();
                radius.setProgress(0);
                map.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                myPosition = latLng;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));

            }
        });

    }



    //Location Request to build the Location Callback.
    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Draw my position in the map
                enableMyLocation();
            }
        }
    }

    //enable showing my location. Check self permission first
    private void enableMyLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);// show a small blue circle for my position
            mMap.getUiSettings().setMyLocationButtonEnabled(false);// I create my own beautiful position floating action button
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

    }



    //start tracking location
    private void startTrackingLocation(){
        isTracking = true;
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback, null);
    }

    //stop tracking location
    private void stopTrackingLocation(){
        isTracking = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }



    public void drawCircle(GoogleMap map, LatLng latlng, int radius){
        map.clear();
        Circle circle = map.addCircle(new CircleOptions()
        .center(latlng)
        .strokeColor(Color.BLUE)
        .strokeWidth(1)
        .radius(radius)
        .fillColor(Color.parseColor("#500084d3")));

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(isTracking){
            stopTrackingLocation();
        }
    }


}
