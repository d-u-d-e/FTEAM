package com.es.findsoccerplayers;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("Registered")
public class MyActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        Utils.cancelToast( /*getClass().getName()*/ );
        super.onPause();
    }
}
