package com.es.findsoccerplayers;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.es.findsoccerplayers.fragments.FragmentAvailableMatches;
import com.es.findsoccerplayers.fragments.FragmentBookedMatches;
import com.es.findsoccerplayers.fragments.FragmentMatches;
import com.es.findsoccerplayers.fragments.FragmentYourMatches;
import com.es.findsoccerplayers.fragments.ViewPagerTabs;
import com.google.android.material.tabs.TabLayout;

public class ActivityMain extends MyActivity {

    private long backPressedTime = 0;
    private Toast backToast;
    ViewPagerTabs adapter;
    ViewPager vp;
    private final String FIRST_TIME = "First_time";


    FragmentYourMatches ymFrag;
    MenuItem[] sortMenuItems = new MenuItem[FragmentMatches.SortType.values().length]; //one for each sort type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = sharedPreferences.getBoolean(FIRST_TIME, true);

        //Start check location
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(firstTime){
                //show only if is the first time we open the app
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(FIRST_TIME, false);
                editor.commit();
                startActivity(new Intent(ActivityMain.this, LocationAccessActivity.class));
            }else if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                //Show only if the  user denied it, but not permanently
                startActivity(new Intent(ActivityMain.this, LocationAccessActivity.class));
            }
        }

        Intent i = getIntent();
        if(i.getBooleanExtra("Login", false))
            Utils.subscribe();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabs = findViewById(R.id.main_tabs);
        vp = findViewById(R.id.main_vp);
        adapter = new ViewPagerTabs(getSupportFragmentManager());

        FragmentAvailableMatches am = new FragmentAvailableMatches();
        ymFrag = new FragmentYourMatches();
        FragmentBookedMatches bm = new FragmentBookedMatches();

        MyFragmentManager.setFragment(am);
        MyFragmentManager.setFragment(ymFrag);
        MyFragmentManager.setFragment(bm);

        adapter.addFragment(ymFrag, getString(R.string.act_main_frag_yours_title));
        adapter.addFragment(bm, getString(R.string.act_main_frag_booked_title));
        adapter.addFragment(am, getString(R.string.act_main_frag_avail_title));

        //each fragment is kept in memory, and its view is not created again when another tab is selected
        vp.setOffscreenPageLimit(adapter.getCount() - 1); // 2
        vp.setAdapter(adapter);
        tabs.setupWithViewPager(vp);

        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                FragmentMatches frag = (FragmentMatches) adapter.getItem(position);
                uncheckAll();
                sortMenuItems[frag.getSortType().ordinal()].setChecked(true);
                super.onPageSelected(position);
            }
        });

        if(Utils.isOffline(this))
            Utils.showOfflineReadToast(this, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_menu, menu);
        sortMenuItems[0] = menu.findItem(R.id.menu_sortByMatchDateAsc);
        sortMenuItems[1] = menu.findItem(R.id.menu_sortByMatchDateDesc);
        sortMenuItems[2] = menu.findItem(R.id.menu_noOrder);
        sortMenuItems[ymFrag.getSortType().ordinal()].setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentMatches frag = (FragmentMatches) adapter.getItem(vp.getCurrentItem());
        switch (item.getItemId()){
            case R.id.acc_settings:
                startActivity(new Intent(this, ActivitySettings.class));
                return true;
            case R.id.menu_sortByMatchDateAsc:
                uncheckAll();
                sortMenuItems[FragmentMatches.SortType.dateMatchAsc.ordinal()].setChecked(true);
                frag.sortByMatchDate(true);
                return true;
            case R.id.menu_sortByMatchDateDesc:
                uncheckAll();
                sortMenuItems[FragmentMatches.SortType.dateMatchDesc.ordinal()].setChecked(true);
                frag.sortByMatchDate(false);
                return true;
            case R.id.menu_noOrder:
                uncheckAll();
                frag.setOrderLastUpdated();
                sortMenuItems[FragmentMatches.SortType.lastUpdated.ordinal()].setChecked(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Requested 2 taps at most 2 seconds apart in order to exit
     */
    @Override
    public void onBackPressed() {
        if(backToast != null) backToast.cancel();
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed(); //the default finishes the current activity
        }else{
            backToast = Toast.makeText(getApplicationContext(), R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    /**
     * Uncheck all checkbox
     */
    private void uncheckAll(){
        for(int i = 0; i < 3; i++)
            sortMenuItems[i].setChecked(false);
    }
}
