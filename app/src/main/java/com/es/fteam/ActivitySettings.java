package com.es.fteam;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.es.fteam.adapter.SettingsAdapter;
import com.es.fteam.models.SettingsElement;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings or logout activity. Here the user can logout, or can select a new range and place to search games or set new games.
 */
public class ActivitySettings extends MyActivity {
    final List<SettingsElement> settElemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        settElemList.add(new SettingsElement(R.drawable.ic_account_24,
                getString(R.string.act_sett_account), getString(R.string.act_sett_account_detailed)));
        settElemList.add(new SettingsElement(R.drawable.ic_edit_location_24,
                getString(R.string.act_sett_edit_pref_pos), getString(R.string.act_sett_edit_pref_pos_detailed)));
        settElemList.add(new SettingsElement(R.drawable.ic_log_out_24, getString(R.string.act_sett_logout), getString(R.string.act_sett_logout_detailed)));

        RecyclerView recyclerView = findViewById(R.id.settings_recyclerview);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        SettingsAdapter adapter = new SettingsAdapter(this, settElemList);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new SettingsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if(position == 0){
                    startActivity(new Intent(getApplicationContext(), ActivityAccount.class));
                }else if(position == 1){
                    Intent i = new Intent(getApplicationContext(), ActivitySetLocation.class);
                    startActivity(i);
                }else if(position == 2){
                    Utils.unsubscribe();
                    logOut();
                }
            }
        });

    }

    /**
     * Logs out the user, and goes back to the login activity
     */
    private void logOut(){
        //options required to log in with Google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            googleSignInClient.signOut();
            finishAffinity();
            Intent intent = new Intent(this, ActivityLogin.class);
            startActivity(intent);
    }
}