package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public final String LOGITUDE = "longitude";
    public final String LATITUDE = "latitude";
    private long backPressedTime = 0;
    private Toast backToast;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    FusedLocationProviderClient mFusedLocationProviderClient;
    Location mlastLocation;
    private double lastLatitude;
    private double lastLongitude;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG, "MainActivity creata");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        fab = findViewById(R.id.main_fab);
        setSupportActionBar(toolbar);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocation();


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MatchActivity.class);
                //If we have a last know location of the user, send it to the MatchActivity
                if(lastLatitude > 0 && lastLongitude > 0){
                    intent.putExtra(LATITUDE, lastLatitude);
                    intent.putExtra(LOGITUDE, lastLongitude);
                    startActivity(intent);
                    // else just start the activity
                } else {
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //show user account
            case R.id.acc_account:
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            //show settings
            case R.id.acc_settings:
                Utils.showUnimplementedToast(this);
                //TODO: creare entity delle impostazioni
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * requested 2 taps at most 2 seconds apart in order to exit
     */
    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed(); //the default finish the current activity
        }else{
            backToast = Toast.makeText(getApplicationContext(), R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    private void getLocation(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        mlastLocation = location;
                        lastLatitude = mlastLocation.getLatitude();
                        lastLongitude = mlastLocation.getLongitude();
                    } else {
                        // To delete in definitive build
                        Toast.makeText(MainActivity.this, R.string.gps_disabled_toast, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getLocation();
                } else {
                    Toast.makeText(this, R.string.local_permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


}
