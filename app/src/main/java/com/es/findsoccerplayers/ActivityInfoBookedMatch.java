package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class ActivityInfoBookedMatch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_match_booked);
        TabLayout tabs = findViewById(R.id.info_match_booked_tabs);
        ViewPager vp = findViewById(R.id.info_match_booked_vp);
        Toolbar toolbar = findViewById(R.id.info_match_booked_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
        adapter.addFragment(new FragmentInfoBookedMatch(),"INFO");
        adapter.addFragment(new FragmentChat(),"CHAT");
        vp.setAdapter(adapter);
        tabs.setupWithViewPager(vp);

    }
}