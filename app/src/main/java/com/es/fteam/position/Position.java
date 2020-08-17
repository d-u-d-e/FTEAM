package com.es.fteam.position;

import android.app.Activity;
import android.content.Context;

import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.es.fteam.ActivityLogin;
import com.es.fteam.ActivitySetLocation;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;


public class Position {

    private static final int GPS_REQUEST = 1001;

    public static class PositionSettings{
        public LatLng position;
        public double radius;
    }

    /*Location Request builder.
    * A data object that contains quality of service parameters for requests to the FusedLocationProviderClient.*/
    private static LocationRequest getLocationReq() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    /* FusedLocationProviderClient is the main entry point for interacting with the fused location provider.
    * Start or remove a LocationUpdate. When it starts, the result come in a LocationCallback. Needs a LocationRequest builder to
    * make locationRequest */
    public static void stopTrackingPosition(FusedLocationProviderClient fusedLocationProviderClient, LocationCallback locationCallback){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public static void startTrackingPosition(FusedLocationProviderClient fusedLocationProviderClient, LocationCallback locationCallback){
        fusedLocationProviderClient.requestLocationUpdates(getLocationReq(), locationCallback, null );
    }

    public static boolean isGpsOFF(Context context){
        /* context.getSystemService return the handle to a system-level service by name.
        * The class of the returned object varies by the requested name.
        * Context.LOCATION_SERVICE is the "location" string.*/
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        return !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void turnGPSon(final Context context){
        /*SettingClient is the main entry point for interacting with the location settings-enabler APIs.
        * When making a request to location services, the device's system settings may be in a state that
        * prevents an app from obtaining the location data that it needs.
        * For example, GPS or Wi-Fi scanning may be switched off.*/
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(context);

        //Build a LocationSettingRequest passing a LocationRequest.
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(Position.getLocationReq());
        LocationSettingsRequest mLocationSettingsRequest = builder.build(); //Creates a LocationSettingsRequest that can be used with SettingsApi.
        builder.setAlwaysShow(true); //Set this to true if location is required to continue and false if having location provides better results, but is not required. This changes the wording/appearance of the dialog accordingly.

        mSettingsClient
                /*checkLocationSettings checks if the relevant system settings are enabled on the device to carry out the desired location requests.
                * Return a Task<LocationSettingsResponse> */
                .checkLocationSettings(mLocationSettingsRequest)
                /* When the Task complete  the client can check the location settings by looking at the status code
                from the LocationSettingsResponse object. We check only and override the onFailureListener. */
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            /*If the status code is RESOLUTION_REQUIRED, the client can call startResolutionForResult(Activity, int)
                            to bring up a dialog, asking for the user's permission to modify the location settings to satisfy those requests.
                            The result of the dialog will be returned via onActivityResult(int, int, Intent).*/
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult((Activity) context, GPS_REQUEST);
                                } catch (IntentSender.SendIntentException sie) {
                                    //Google says to ignore this error
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                //An error in LocationSetting.
                                String errorMessage = "Location settings are inadequate";
                                Log.e("Position", errorMessage);
                                break;
                        }
                    }
                });
    }

    /**
     * Get the current preferred position from the preferences. As always each string key is prepended by
     * the current user + dot
     */
    public static PositionSettings getPositionSettings(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String pLng = sharedPreferences.getString(ActivityLogin.currentUserID + "." + ActivitySetLocation.LONGITUDE, null);
        String pLat = sharedPreferences.getString(ActivityLogin.currentUserID + "." + ActivitySetLocation.LATITUDE, null);
        String pRad =  sharedPreferences.getString(ActivityLogin.currentUserID + "." + ActivitySetLocation.RADIUS, null);

        if(pLat == null || pLng == null) return null;
        assert pRad != null;

        PositionSettings ps = new PositionSettings();
        ps.position = new LatLng(Double.parseDouble(pLat), Double.parseDouble(pLng));
        ps.radius = Double.parseDouble(pRad);
        return ps;
    }

    public static void showMyLocation(GoogleMap map){
        map.setMyLocationEnabled(true); // show a small blue circle for my position
        map.getUiSettings().setMyLocationButtonEnabled(false);// I create my own beautiful position floating action button. Don't need this
    }
}
