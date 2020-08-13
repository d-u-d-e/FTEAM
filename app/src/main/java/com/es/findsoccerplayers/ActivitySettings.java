package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.es.findsoccerplayers.adapter.SettingsAdapter;
import com.es.findsoccerplayers.models.SettingsElement;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

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

        settElemList.add(new SettingsElement(R.drawable.ic_account_24, "Account", "View and modify account options"));
        settElemList.add(new SettingsElement(R.drawable.ic_edit_location_24, "Edit search range", "Modify the searching distance for the available matches"));
        settElemList.add(new SettingsElement(R.drawable.ic_log_out_24, "Log out", "Disconnect from your account"));

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
                    unsubscribe();
                    logOut();
                }
            }
        });

    }

    /**
     * Method to logout the user, and go back to the login activity
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
            Intent i = new Intent(this, ActivityLogin.class);
            startActivity(i);
    }

    /**
     * If the user logout from the app, he needs to unsubscribe from all matches, so he
     * will not get notification from chats.
     */
    private void unsubscribe(){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference ref =
                    db.getReference().child("users").child(user.getUid()).child("bookedMatches");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for(DataSnapshot booked : snapshot.getChildren()){
                        String matchID = booked.getKey();
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

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
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

    }
}