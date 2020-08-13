package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class ActivityMain extends MyActivity {

    private long backPressedTime = 0;
    private Toast backToast;
    ViewPagerTabs adapter;
    ViewPager vp;

    MenuItem[] sortMenuItems = new MenuItem[FragmentMatches.SortType.values().length]; //one for each sort type

    FragmentAvailableMatches am;
    FragmentYourMatches ym;
    FragmentBookedMatches bm;

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

        Intent i = getIntent();
        boolean logIN = i.getBooleanExtra("Login", false);
        if(logIN){
            subscribe();
        }

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabs = findViewById(R.id.main_tabs);
        vp = findViewById(R.id.main_vp);
        adapter = new ViewPagerTabs(getSupportFragmentManager());

        am = new FragmentAvailableMatches();
        ym = new FragmentYourMatches();
        bm = new FragmentBookedMatches();

        MyFragmentManager.setFragment(am);
        MyFragmentManager.setFragment(ym);
        MyFragmentManager.setFragment(bm);

        adapter.addFragment(ym, getString(R.string.act_main_frag_yours_title));
        adapter.addFragment(bm, getString(R.string.act_main_frag_booked_title));
        adapter.addFragment(am, getString(R.string.act_main_frag_avail_title));

        vp.setOffscreenPageLimit(adapter.getCount() - 1); //2
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
            Utils.showOfflineReadToast(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_menu, menu);
        sortMenuItems[0] = menu.findItem(R.id.menu_sortByMatchDateAsc);
        sortMenuItems[1] = menu.findItem(R.id.menu_sortByMatchDateDesc);
        sortMenuItems[2] = menu.findItem(R.id.menu_noOrder);
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
                frag.setOrderNone();
                sortMenuItems[FragmentMatches.SortType.none.ordinal()].setChecked(true);
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

    private void subscribe(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref =
                db.getReference().child("users").child(user.getUid()).child("bookedMatches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    FirebaseMessaging.getInstance().subscribeToTopic(matchID);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference ref2 =
                db.getReference().child("users").child(user.getUid()).child("createdMatches");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    FirebaseMessaging.getInstance().subscribeToTopic(matchID);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void uncheckAll(){
        for(int i = 0; i < 3; i++)
            sortMenuItems[i].setChecked(false);
    }
}
