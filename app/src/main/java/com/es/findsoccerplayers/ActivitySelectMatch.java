package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

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
    ViewPager vp;

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
        }else if(type.equals("booked") || type.equals("yours")){
            Match m = i.getParcelableExtra("match");
            assert m != null;
            matchID = m.getMatchID();
            setTabLayout(m, type);
        }
        else { //activity started by messaging service when user taps on notification
            matchID = i.getStringExtra("match");
            DatabaseReference r = FirebaseDatabase.getInstance().getReference("matches/" + matchID);
            r.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Match m = snapshot.getValue(Match.class);
                    setTabLayout(m, "onNotificationClicked");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        Toolbar toolbar = findViewById(R.id.act_select_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onPause() {
        FragmentChat.isDisplayed = false;
        super.onPause();
    }

    private void setTabLayout(Match m, String type){
        setContentView(R.layout.act_select_match_tabs);
        TabLayout tabs = findViewById(R.id.info_match_booked_tabs);
        vp = findViewById(R.id.info_match_booked_vp);
        ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
        adapter.addFragment(new FragmentInfoMatch(m, type), "INFO");
        final FragmentChat fragmentChat = new FragmentChat(matchID, this);
        MyFragmentManager.setFragment(fragmentChat);
        adapter.addFragment(fragmentChat, "CHAT");
        vp.setAdapter(adapter);

        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(FragmentChat.isDisplayed && position == 0)//switch from 1 to 0
                    MyFragmentManager.getFragmentChat().onNewMessagesRead();
                FragmentChat.isDisplayed = position == 1;
            }
        });
        tabs.setupWithViewPager(vp);
    }

    @Override
    protected void onResume() {
        if(vp != null)
            FragmentChat.isDisplayed = vp.getCurrentItem() == 1;
        super.onResume();
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
}