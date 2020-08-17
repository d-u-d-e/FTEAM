package com.es.fteam;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.es.fteam.position.Position;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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


public class ActivitySetLocation extends  MyActivity implements OnMapReadyCallback {

    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String RADIUS = "radius";

    private static final int GPS_REQUEST = 1001;


    private GoogleMap map;
    private String latitude;
    private String longitude;
    private FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    private LatLng myPosition;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private boolean locationAccess;
    private Circle circle;

    private FloatingActionButton position_fab;

    //Layout elements
    private SeekBar radiusBar;
    private TextView distanceView;
    private String distance;
    private boolean mapReady = false;
    private boolean isTracking = false;
    private double index;
    private Position.PositionSettings currentPosSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_set_location);

        Toolbar toolbar = findViewById(R.id.location_maps_toolbar);
        setSupportActionBar(toolbar);

        getLocationPermission(); //get location permission if I don'have it

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.setting_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        position_fab = findViewById(R.id.setting_position_fab);
        distanceView = findViewById(R.id.radius);
        radiusBar = findViewById(R.id.seek_bar);
        Button save = findViewById(R.id.save_button);

        currentPosSettings = Position.getPositionSettings(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //create a callback when receive a location result
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                myPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                if(mapReady){
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
                }
            }
        };

        //check if the GPS is disabled or locationAccess is not enabled and set the right configuration
        if(Position.isGpsOFF(getApplicationContext()) || !locationAccess){
            //if the GPS is disabled show a Toast
            Toast.makeText(ActivitySetLocation.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
            position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
        } else {
            position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
        }

        position_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locationAccess){
                    getLocationPermission();
                }else {
                    if(Position.isGpsOFF(getApplicationContext())){
                        Position.turnGPSon(ActivitySetLocation.this);
                    } else {
                        // Go to the new view in the map and change the color
                        if(!isTracking){
                            Position.startTrackingPosition(fusedLocationClient, locationCallback);
                            isTracking = true;
                            map.clear();
                            position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                            radiusBar.setProgress(0);
                        }
                    }

                }

            }
        });


        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(myPosition != null){
                    circle.setRadius(progress * 500);
                    if(progress <= 10){
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
                    } else if(progress < 20){
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 11));
                    } else if(progress < 40){
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 10));
                    }else {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 9));
                    }
                    index = progress * 0.5;
                    distanceView.setText(index + getString(R.string.km));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(myPosition != null){
                    circle = drawCircle(map, myPosition);
                }
                if(isTracking){
                    Position.stopTrackingPosition(fusedLocationClient, locationCallback);
                    isTracking = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(myPosition == null){
                    Utils.showToast(ActivitySetLocation.this, R.string.no_initial_position, true);
                    radiusBar.setProgress(0);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(myPosition == null || index <= 0){
                    Utils.showToast(ActivitySetLocation.this, R.string.complete_pos_preferences, true);
                } else {
                    latitude = String.valueOf(myPosition.latitude);
                    longitude = String.valueOf(myPosition.longitude);
                    distance = String.valueOf(index * 1000);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ActivityLogin.currentUserID + "." + LATITUDE, latitude);
                    editor.putString(ActivityLogin.currentUserID + "." + LONGITUDE, longitude);
                    editor.putString(ActivityLogin.currentUserID + "." + RADIUS, distance);

                    editor.apply();
                    Utils.showToast(ActivitySetLocation.this, R.string.preference_saved, false);
                    MyFragmentManager.getFragmentAvailableMatches().onNewPositionSet();
                    finish();
                }

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapReady = true;

        if(locationAccess){
            if(Position.isGpsOFF(ActivitySetLocation.this)){
                Position.turnGPSon(ActivitySetLocation.this);
            } else{
                Position.showMyLocation(map);
            }
        }


        if(!isTracking && locationAccess){
            Position.startTrackingPosition(fusedLocationClient, locationCallback);
            isTracking = true;
        }

        onMapMoving(map);//if the camera moves because the user move it, stop tracking location. He's looking for a place.
        setMapLongClick(map);// Add a marker in a long position click
    }

    /**
     * onMapMoving stops the tracking location if the user starts moving the camera.
     * @param map the map showed
     */
    private void onMapMoving(GoogleMap map){
        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                    if(isTracking){
                        Position.stopTrackingPosition(fusedLocationClient, locationCallback);
                        isTracking = false;
                    }
                }
            }
        });
    }

    /**
     * If the user make a long click on the map, add in that position a marker
     * @param map the map showed
     */
    private void setMapLongClick(final GoogleMap map){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                map.clear();
                radiusBar.setProgress(0);
                map.addMarker(new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                myPosition = latLng;
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 12));
                if(isTracking){
                    Position.stopTrackingPosition(fusedLocationClient, locationCallback);
                    isTracking = false;
                }
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //If I have the permission to access the location set locationAccess to true
                locationAccess = true;
                //if the GPS is turned off, ask to turn it on
                if(Position.isGpsOFF(getApplicationContext())){
                    Position.turnGPSon(ActivitySetLocation.this);
                }else if(!isTracking){
                    isTracking = true;
                    Position.startTrackingPosition(fusedLocationClient, locationCallback);
                    Position.showMyLocation(map);
                }
            } else{
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // now, user has denied permission (but not permanently!)
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage(R.string.alert_location_request)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getLocationPermission();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Utils.showToast(ActivitySetLocation.this, R.string.location_access_denied, true);
                                }
                            }).create().show();

                } else {
                    // now, user has denied permission permanently!
                    AlertDialog.Builder alertDialogPermanently = new AlertDialog.Builder(this);
                    alertDialogPermanently.setMessage(R.string.alert_location_denied_permanently)
                            .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Utils.showToast(ActivitySetLocation.this, R.string.access_denied_permanently, true);
                                }
                            }).create().show();

                }
            }
        }
    }


    /**
     * If the user start moving the seekBar, then draw a semi-transparent circle, so he can see the maximum distance he will
     * cover to go to play a match.
     * @param map the map where to draw the circle
     * @param latLng center of the circle
     */
    public Circle drawCircle(GoogleMap map, LatLng latLng){
        map.clear();
        return map.addCircle(new CircleOptions()
                .center(latLng)
                .strokeColor(Color.BLUE)
                .strokeWidth(1)
                .radius(0)
                .fillColor(Color.parseColor("#500084d3")));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {
                Position.startTrackingPosition(fusedLocationClient, locationCallback);
                isTracking = true;
                Position.showMyLocation(map);
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
            }
        }
    }


    /**
     * The user can deny permission in any time. There is no reason to do it, but he can.
     * If he will use again the map, it's better to get permission again.
     */
    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationAccess = true;
        } else {
            locationAccess = false;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onBackPressed() {
        if(currentPosSettings != null){
            if(isTracking){
                Position.stopTrackingPosition(fusedLocationClient, locationCallback);
                isTracking = false;
            }
            super.onBackPressed();
        }
        else{
            Utils.showToast(ActivitySetLocation.this, R.string.complete_pos_preferences, true);
        }
    }
}
