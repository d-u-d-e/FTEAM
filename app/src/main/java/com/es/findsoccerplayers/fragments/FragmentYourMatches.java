package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.es.findsoccerplayers.ActivityCreateMatch;
import com.es.findsoccerplayers.ActivitySelectMatch;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class FragmentYourMatches extends FragmentMatches {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private int count = 0;
    private Integer readCount = 0;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //this should be called once from the view pager, because the offscreen page limit is set to 2
        matches = new ArrayList<>();
        matchAdapter = new MatchAdapter(matches);
        matchAdapter.setOnItemClickListener(new MatchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getContext(), ActivitySelectMatch.class);
                i.putExtra("match", matches.get(position));
                i.putExtra("type", "yours");
                startActivity(i);
            }
        });

        View view = inflater.inflate(R.layout.frag_your_matches, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.frag_yours_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(matchAdapter);
        recyclerView.setMotionEventSplittingEnabled(false);
        //recyclerView.setItemAnimator(null);

        progressBar = view.findViewById(R.id.frag_yours_progressBar);

        readMatches();

        FloatingActionButton fabCreateMatch = view.findViewById(R.id.fab_create_match);
        fabCreateMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ActivityCreateMatch.class);
                startActivity(i);
            }
        });
        return view;
    }


    public void registerForMatchEvents(final String matchID){

        final DatabaseReference r = db.getReference("matches/" + matchID);
        r.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    removeUI(matchID);
                    r.removeEventListener(this);
                }
                else{
                    Match m = snapshot.getValue(Match.class);
                    long matchTime = m.getTimestamp();
                    synchronized (readCount){ //TODO ugly because it's done only at the start
                        if(readCount < count){
                            readCount++;
                            if(readCount == count){
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                    if(matchTime > Calendar.getInstance().getTimeInMillis()) // if the match is in the future, show it
                        addUI(m);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    private void readMatches() { //done only at beginning

        String path = "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/createdMatches";

        DatabaseReference ref = db.getReference(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<String> keys = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    keys.add(ds.getKey());
                    count++;
                }
                if(count > 0)
                    progressBar.setVisibility(View.VISIBLE);
                for(String key: keys)
                    registerForMatchEvents(key);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}

