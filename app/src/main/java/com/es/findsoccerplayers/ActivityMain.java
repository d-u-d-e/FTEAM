package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
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

public class ActivityMain extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long backPressedTime = 0;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        Log.w(TAG, "MainActivity creata");
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabs = findViewById(R.id.main_tabs);
        ViewPager vp = findViewById(R.id.main_vp);
        ViewPagerTabs adapter = new ViewPagerTabs(getSupportFragmentManager());
        adapter.addFragment(new FragmentAvailableMatches(), "AVAILABLE MATCHES");
        adapter.addFragment(new FragmentBookedMatches(), "BOOKED MATCHES");
        adapter.addFragment(new FragmentYourMatches(), "YOUR MATCHES");
        vp.setAdapter(adapter);
        tabs.setupWithViewPager(vp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //show user account
            case R.id.acc_account:
                startActivity(new Intent(this, ActivityAccount.class));
                return true;
            //show settings
            case R.id.acc_settings:
                startActivity(new Intent(this, ActivitySetLocation.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * requested 2 taps at most 2 seconds apart in order to exit
     */
    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed(); //the default finish the current activity
        }else{
            backToast = Toast.makeText(getApplicationContext(), R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
