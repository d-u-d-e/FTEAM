package com.es.findsoccerplayers;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.es.findsoccerplayers.fragments.FragmentAvailableMatches;
import com.es.findsoccerplayers.fragments.FragmentBookedMatches;
import com.es.findsoccerplayers.fragments.FragmentYourMatches;
import com.es.findsoccerplayers.fragments.ViewPagerTabs;
import com.google.android.material.tabs.TabLayout;

public class ActivityMain extends MyActivity {

    private long backPressedTime = 0;
    private Toast backToast;
    ViewPagerTabs adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        //Start check location
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //Show only if the  user denied it, but not permanently
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                startActivity(new Intent(ActivityMain.this, LocationAccessActivity.class));
            }
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabs = findViewById(R.id.main_tabs);
        ViewPager vp = findViewById(R.id.main_vp);
        adapter = new ViewPagerTabs(getSupportFragmentManager());

        FragmentAvailableMatches am = new FragmentAvailableMatches();
        FragmentYourMatches ym = new FragmentYourMatches();
        FragmentBookedMatches bm = new FragmentBookedMatches();

        ListsManager.setFragment(am);
        ListsManager.setFragment(ym);
        ListsManager.setFragment(bm);

        adapter.addFragment(ym, getString(R.string.act_main_frag_yours_title));
        adapter.addFragment(bm, getString(R.string.act_main_frag_booked_title));
        adapter.addFragment(am, getString(R.string.act_main_frag_avail_title));

        vp.setOffscreenPageLimit(adapter.getCount()-1); //2
        vp.setAdapter(adapter);
        tabs.setupWithViewPager(vp);

        if(Utils.isOffline(this))
            Utils.showOfflineReadToast(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.acc_settings) {
            startActivity(new Intent(this, ActivitySettings.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Requested 2 taps at most 2 seconds apart in order to exit
     */
    @Override
    public void onBackPressed() {
        backToast.cancel();
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed(); //the default finishes the current activity
        }else{
            backToast = Toast.makeText(getApplicationContext(), R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
