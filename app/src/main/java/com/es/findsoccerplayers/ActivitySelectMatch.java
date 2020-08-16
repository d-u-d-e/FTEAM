package com.es.findsoccerplayers;

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
    public String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        assert extras != null;
        type = i.getStringExtra("type");
        assert type != null;

        if(type.equals("available")){
            Match m = extras.getParcelable("match");
            assert m != null;
            matchID = m.getMatchID();
            setNoTabLayout(m, type);

        }else if(type.equals("booked") || type.equals("yours")){
            Match m = i.getParcelableExtra("match");
            assert m != null;
            matchID = m.getMatchID();
            setTabLayout(m, type, 0);
        }
        else { //activity started by messaging service when user taps on notification
                //type is "notification"
            matchID = i.getStringExtra("match");
            DatabaseReference r = FirebaseDatabase.getInstance().getReference("matches/" + matchID);
            r.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Match m = snapshot.getValue(Match.class);
                    assert m != null;
                    String type = "booked";
                    if(m.getCreatorID().equals(ActivityLogin.currentUserID))
                        type = "yours";
                    setTabLayout(m, type, 1);
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });

        }
    }

    @Override
    protected void onPause() {
        FragmentChat.isDisplayed = false;
        super.onPause();
    }

    /**
     * If the user selects a match from the available matches only, then a layout without tabs is created
     * @param m the Match Object selected
     * @param type type of view
     */
    private void setNoTabLayout(Match m, String type){
        setContentView(R.layout.act_select_match_notabs);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.act_select_match_fragContainer, new FragmentInfoMatch(m, type), null);
        transaction.commit();
        Toolbar toolbar = findViewById(R.id.act_select_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * If the user selects a match from the booked matches or from his created matches , then a layout with tabs is created
     * @param m the Match Object selected
     * @param type type of view
     * @param position position of starting ViewPager, info or chat
     */
    private void setTabLayout(Match m, String type, int position){
        setContentView(R.layout.act_select_match_tabs);
        TabLayout tabs = findViewById(R.id.info_match_booked_tabs);
        vp = findViewById(R.id.info_match_booked_vp);
        ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
        adapter.addFragment(new FragmentInfoMatch(m, type), getString(R.string.info_frag_title));
        final FragmentChat fragmentChat = new FragmentChat(this);
        MyFragmentManager.setFragment(fragmentChat);
        adapter.addFragment(fragmentChat, getString(R.string.chat_frag_title));
        vp.setAdapter(adapter);

        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //switch from chat to info tab: new messages are considered read now
                if(FragmentChat.isDisplayed && position == 0)
                    MyFragmentManager.getFragmentChat().onNewMessagesRead();
                FragmentChat.isDisplayed = position == 1;
            }
        });

        vp.setCurrentItem(position);

        tabs.setupWithViewPager(vp);
        Toolbar toolbar = findViewById(R.id.act_select_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        //this intent with this particular action is sent from FragmentBookedMatches or FragmentAvailableMatches when the user is browsing
        // a match which got deleted
        if(action != null && action.equals("finishOnMatchDeleted")){
            finish();
        }
        else if(action != null && action.equals("onNotificationClicked")){
            //in this case our firebase service started this activity, while this activity was running
            String matchID = intent.getStringExtra("match");
            //this activity gets replaced with another one, displaying the correct match
            Intent i = new Intent(ActivitySelectMatch.this, ActivitySelectMatch.class);
            i.putExtra("type", "notification");
            i.putExtra("match", matchID);
            finish();
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        if(type.equals("notification")){
            Intent i = new Intent(this, ActivityMain.class);
            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            //we need this because if the user taps on a notification while the app is in background or closed
            //then only this activity is running, so hitting the back button will close the app, unless main is started
            startActivity(i);
            finish();
        } else {
            super.onBackPressed();
        }

    }
}