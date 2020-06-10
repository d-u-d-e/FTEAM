package com.es.findsoccerplayers.position;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import android.content.IntentSender;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.ContentValues.TAG;

public class PositionClient {

    public static final int GPS_REQUEST = 1001;

    //Location Request to build the Location Callback.
    public static LocationRequest getLocationReq() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public static void stopTrackingPosition(FusedLocationProviderClient fusedLocationProviderClient, LocationCallback locationCallback){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    public static void startTrackingPosition(FusedLocationProviderClient fusedLocationProviderClient, LocationCallback locationCallback){
        fusedLocationProviderClient.requestLocationUpdates(getLocationReq(), locationCallback, null );
    }

    public static boolean isGpsOFF(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        }else {
            return false;
        }
    }

    public static void turnGPSon(final Context context){
        SettingsClient mSettingsClient = LocationServices.getSettingsClient(context);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(PositionClient.getLocationReq());
        LocationSettingsRequest mLocationSettingsRequest = builder.build();
        builder.setAlwaysShow(true);

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener((Activity) context, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //do Nothing
                    }
                })
                .addOnFailureListener((Activity) context, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            /*If the status code is RESOLUTION_REQUIRED, the client can call startResolutionForResult(Activity, int)
                            to bring up a dialog, asking for the user's permission to modify the location settings to satisfy those requests.
                            The result of the dialog will be returned via onActivityResult(int, int, Intent).
                             */
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult((Activity) context, GPS_REQUEST);
                                } catch (IntentSender.SendIntentException sie) {
                                    //Google say to ignore this error
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate";
                                Log.e(TAG, errorMessage);
                                break;
                        }
                    }
                });
    }

}
