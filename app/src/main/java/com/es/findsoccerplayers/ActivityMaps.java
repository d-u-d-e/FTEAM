package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.es.findsoccerplayers.position.MapElements;
import com.es.findsoccerplayers.position.PositionClient;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

public class ActivityMaps extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String placeName;
    private double latitude;
    private double longitude;
    private LatLng initPosition;
    private LatLng myPosition;
    private boolean locationAccess;


    public final String LONGITUDE = "longitude";
    public final String LATITUDE = "latitude";
    public final String PLACE_NAME = "place name";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final int GPS_REQUEST = 1001;
    LocationCallback mLocationCallback;
    private boolean mapReady = false;
    private boolean isTracking = false;

    private FloatingActionButton position_fab;
    FloatingActionButton confirm_fab;

    //Location objects and parameters
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.maps_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getLocationPermission(); //get location permission if I don'have it

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String lat = preferences.getString(LATITUDE, null);
        String lng = preferences.getString(LONGITUDE, null);
        if(lat != null && lng != null){
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);
            initPosition = new LatLng(latitude, longitude);
        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.maps_map);
        mapFragment.getMapAsync(this);



        confirm_fab = findViewById(R.id.maps_confirm_fab);
        position_fab = findViewById(R.id.maps_position_fab);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //create a callback when receive a location result
        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                myPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                if(mapReady){
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 15));
                }
            }
        };


        //check if the GPS is disabled or locationAccess enabled and set the right configuration
        if(PositionClient.isGpsOFF(getApplicationContext()) || !locationAccess){
            position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
        }else {
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
        }

        //ConfirmPosition FAB listener
        confirm_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(placeName == null){
                    Toast.makeText(ActivityMaps.this, R.string.select_a_place, Toast.LENGTH_SHORT).show();
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(LATITUDE, latitude);
                    resultIntent.putExtra(LONGITUDE, longitude);
                    resultIntent.putExtra(PLACE_NAME, placeName);
                    setResult(RESULT_OK, resultIntent);
                    if(isTracking){
                        PositionClient.stopTrackingPosition(mFusedLocationClient, mLocationCallback);
                        isTracking = true;
                    }
                    finish();
                }

            }
        });

        //Position FAB listener
        position_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locationAccess){
                    getLocationPermission();
                }else {
                    if(PositionClient.isGpsOFF(getApplicationContext())){
                        PositionClient.turnGPSon(ActivityMaps.this);
                    } else{
                        // Go to the new view in the map and change the color
                        if(!isTracking){
                            PositionClient.startTrackingPosition(mFusedLocationClient, mLocationCallback);
                            isTracking = true;
                        }
                        position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
                    }
                }

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //If I have the permission to access to the location set locationAcces to true
                locationAccess = true;
                //if the GPS is turned off, ask to turn it on
                if(PositionClient.isGpsOFF(getApplicationContext())){
                    PositionClient.turnGPSon(ActivityMaps.this);
                } else if(!isTracking){
                    isTracking = true;
                    PositionClient.startTrackingPosition(mFusedLocationClient, mLocationCallback);
                    MapElements.showMyLocation(mMap);
                }
            }
        }
    }

    //Insert the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    //Using Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;


        if(locationAccess){
            if(PositionClient.isGpsOFF(ActivityMaps.this)){
                PositionClient.turnGPSon(ActivityMaps.this);
            } else {
                //If I have location access and GPS is on then show a small blue circle to identify my position
                MapElements.showMyLocation(mMap);
            }
        }




        //if I have an initial position from sharedPreferences and the GPS is Off, the start the map from that position
        if(initPosition != null && PositionClient.isGpsOFF(ActivityMaps.this)){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, 12));
            }


        if(!isTracking && locationAccess){
            PositionClient.startTrackingPosition(mFusedLocationClient, mLocationCallback);
            isTracking = true;
        }

        setMapLongClick(mMap); // Add a marker in a long position click
        setPoiClick(mMap);  //add a marker in a POI
        setInfoWindowClick(mMap); // choose the selected position if the user clicks in the info window
        onMapMoving(mMap); //if the camera moves because the user move it, stop tracking location. He's looking for a place.


    }



    // onMapMoving stops the tracking location if the user starts moving the camera.
    private void onMapMoving(GoogleMap map){
        map.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE){
                    if(isTracking){
                        PositionClient.stopTrackingPosition(mFusedLocationClient, mLocationCallback);
                        isTracking = false;
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
                map.clear();
                if(isTracking){
                    PositionClient.stopTrackingPosition(mFusedLocationClient, mLocationCallback);
                    isTracking = false;
                }
                String snippet = String.format(Locale.getDefault(), "Lat: %1.5f, Long: %2.5f",
                        latLng.latitude,
                        latLng.longitude);
                try {
                    Geocoder geo = new Geocoder(ActivityMaps.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.isEmpty()) {
                        placeName = getString(R.string.default_position_name);
                    } else {
                        placeName = addresses.get(0).getLocality();  // if the place is not a POI then use only the city name
                        latitude = latLng.latitude;  //get the selected place latitude
                        longitude = latLng.longitude; // //get the selected place longitude
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(ActivityMaps.this, R.string.gecodo_failed, Toast.LENGTH_SHORT).show();
                    placeName = getString(R.string.default_position_name);
                }
                Marker myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(placeName).snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                myMarker.showInfoWindow();
            }
        });

    }


    /*POI is the "Point Of Interest". Many structures already exist in
    * Google database with their names. We can set the position name name from the POI name */
    private void setPoiClick(final GoogleMap map){
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                mMap.clear();
                if(isTracking){
                    PositionClient.stopTrackingPosition(mFusedLocationClient, mLocationCallback);
                    isTracking = false;
                }
                Marker poiMarker = mMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                poiMarker.showInfoWindow();
                placeName = pointOfInterest.name; //get the poi name
                latitude = pointOfInterest.latLng.latitude;  // get the poi latitude
                longitude = pointOfInterest.latLng.longitude; // get the poi longitude
            }
        });
    }

    private void setInfoWindowClick(final GoogleMap map){
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(LATITUDE, latitude);
                resultIntent.putExtra(LONGITUDE, longitude);
                resultIntent.putExtra(PLACE_NAME, placeName);
                setResult(RESULT_OK, resultIntent);
                if(isTracking){
                    PositionClient.stopTrackingPosition(mFusedLocationClient, mLocationCallback);
                    isTracking = false;
                }
                finish();
            }
        });
    }




    private void getLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationAccess = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            //The response come from the turGPSon()
            if (requestCode == GPS_REQUEST) {
                //If the user choose to turn on the GPS then start tracking and set the widget in the correct colors etc
                // otherwise do nothing. He will zoom the map manually
                PositionClient.startTrackingPosition(mFusedLocationClient, mLocationCallback);
                MapElements.showMyLocation(mMap);
                position_fab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.blue)));
            }
        }
    }


}
