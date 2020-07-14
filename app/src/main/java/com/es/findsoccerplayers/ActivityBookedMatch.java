package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import com.es.findsoccerplayers.fragments.FragmentChat;
import com.es.findsoccerplayers.fragments.FragmentInfoBookedMatch;
import com.es.findsoccerplayers.fragments.ViewPagerTabs;
import com.google.android.material.tabs.TabLayout;

public class ActivityBookedMatch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_booked_match);
        TabLayout tabs = findViewById(R.id.info_match_booked_tabs);
        ViewPager vp = findViewById(R.id.info_match_booked_vp);
        Toolbar toolbar = findViewById(R.id.info_match_booked_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String matchID = i.getStringExtra("match");

        ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
        adapter.addFragment(new FragmentInfoBookedMatch(matchID), "INFO");
        adapter.addFragment(new FragmentChat(matchID), "CHAT");
        vp.setAdapter(adapter);
        tabs.setupWithViewPager(vp);
    }
}