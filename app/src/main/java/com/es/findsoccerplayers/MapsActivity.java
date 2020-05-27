package com.es.findsoccerplayers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String placename;
    private double mlatitude;
    private double mlongitude;
    private LatLng initPosition;

    public final String LOGITUDE = "longitude";
    public final String LATITUDE = "latitude";
    public final String PLACE_NAME = "place name";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    LocationManager mLocationManager;
    private FloatingActionButton confirm_fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Intent intent = getIntent();
        mlatitude = intent.getDoubleExtra(LATITUDE, 0);
        mlongitude = intent.getDoubleExtra(LOGITUDE, 0);

        initPosition= new LatLng(mlatitude,mlongitude);
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(MapsActivity.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
        }

        confirm_fab = findViewById(R.id.confirm_fab);
        confirm_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(LATITUDE, mlatitude);
                resultIntent.putExtra(LOGITUDE, mlongitude);
                resultIntent.putExtra(PLACE_NAME, placename);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    enableMyLocation();
                    break;
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
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();

        //If the user have a last know location the map will start from there
        // if there is not a last know location the entire world map will appear and the user will find his location
        // by pressing the button in the top right corner of the map.
        if (mlongitude > 0 && mlatitude > 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initPosition, 15));
        }
        setMapLongClick(mMap);
        setPoiClick(mMap);
        setInfoWindowclick(mMap);
        setOnClick(mMap);
    }


    private void setMapLongClick(final GoogleMap map){
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                String snippet = String.format(Locale.getDefault(), "Lat: %1.5f, Long: %2.5f",
                        latLng.latitude,
                        latLng.longitude);
                try {
                    Geocoder geo = new Geocoder(MapsActivity.this.getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = geo.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses.isEmpty()) {
                        placename = getString(R.string.default_position_name);
                    } else {
                        if (addresses.size() > 0) {
                            placename = addresses.get(0).getLocality();  // if the place is not a POI the use only the City name
                            mlatitude = latLng.latitude;  //get the selected place latitude
                            mlongitude = latLng.longitude; // //get the selected place longitude
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MapsActivity.this, R.string.gecodo_failed, Toast.LENGTH_SHORT).show();
                    placename = getString(R.string.default_position_name);
                }
                Marker myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(placename).snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                myMarker.showInfoWindow();
            }
        });

    }


    /*POI is the "Point Of Interes". Many structure alredy exist in
    * Google database with their name. We can set the position name name from the POI name */
    private void setPoiClick(final GoogleMap map){
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                mMap.clear();
                Marker poiMarker = mMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                poiMarker.showInfoWindow();
                placename = pointOfInterest.name; //get the poi name
                mlatitude = pointOfInterest.latLng.latitude;  // get the poi latitude
                mlongitude = pointOfInterest.latLng.longitude; // get the poi longitude
            }
        });
    }

    private void setInfoWindowclick(final GoogleMap map){
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(LATITUDE, mlatitude);
                resultIntent.putExtra(LOGITUDE, mlongitude);
                resultIntent.putExtra(PLACE_NAME, placename);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void enableMyLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);


        }

    }

    private void setOnClick(final GoogleMap map){
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                enableMyLocation();
                if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(MapsActivity.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
