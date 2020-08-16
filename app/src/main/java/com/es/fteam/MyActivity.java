package com.es.fteam;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;


/**
 * To avoid reimplement onPause on each activity, we need only to extend each activity from MyActivity
 */
@SuppressLint("Registered")
public class MyActivity extends AppCompatActivity {

    /**
     * Cancels the current displayed toast of this activity, when it goes on pause
     */
    @Override
    protected void onPause() {
        Utils.cancelToast();
        super.onPause();
    }
}
