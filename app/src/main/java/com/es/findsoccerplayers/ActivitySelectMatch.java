package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.es.findsoccerplayers.fragments.FragmentChat;
import com.es.findsoccerplayers.fragments.FragmentInfoMatch;
import com.es.findsoccerplayers.fragments.ViewPagerTabs;
import com.es.findsoccerplayers.models.Match;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivitySelectMatch extends MyActivity {

    public static String matchID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        assert extras != null;
        String type = i.getStringExtra("type");
        assert type != null;

        if(type.equals("available")){
            Match m = extras.getParcelable("match");
            assert m != null;
            setContentView(R.layout.act_select_match_notabs);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.act_select_match_fragContainer, new FragmentInfoMatch(m, type), null);
            transaction.commit();
        } else if(type.equals("msg")){
            setContentView(R.layout.act_select_match_tabs);
            final TabLayout tabs = findViewById(R.id.info_match_booked_tabs);
            final ViewPager vp = findViewById(R.id.info_match_booked_vp);
            final ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
            DatabaseReference r = FirebaseDatabase.getInstance().getReference("matches/" + i.getStringExtra("match"));
            r.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Match theMatch = snapshot.getValue(Match.class);
                    adapter.addFragment(new FragmentInfoMatch(theMatch, "booked"), "INFO");
                    adapter.addFragment(new FragmentChat(theMatch.getMatchID()), "CHAT");
                    vp.setAdapter(adapter);
                    tabs.setupWithViewPager(vp);
                    vp.setCurrentItem(1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            Match m = extras.getParcelable("match");
            assert m != null;
            matchID = m.getMatchID();
            setContentView(R.layout.act_select_match_tabs);
            TabLayout tabs = findViewById(R.id.info_match_booked_tabs);
            ViewPager vp = findViewById(R.id.info_match_booked_vp);
            ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
            adapter.addFragment(new FragmentInfoMatch(m, type), "INFO");
            adapter.addFragment(new FragmentChat(m.getMatchID()), "CHAT");
            vp.setAdapter(adapter);
            tabs.setupWithViewPager(vp);
        }
        Toolbar toolbar = findViewById(R.id.act_select_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        matchID = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if(action != null && action.equals("finishOnMatchDeleted")){
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, ActivityMain.class));
        finish();
        return;
    }
}