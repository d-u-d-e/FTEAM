package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivitySelectMatch;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FragmentBookedMatches extends FragmentMatches {

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
                i.putExtra("type", "booked");
                startActivity(i);
            }
        });

        View view = inflater.inflate(R.layout.frag_booked_matches, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.frag_booked_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(matchAdapter);
        recyclerView.setMotionEventSplittingEnabled(false);
        //recyclerView.setItemAnimator(null);

        sync();
        return view;
    }

    private void sync(){

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference ref =
                    db.getReference().child("users").child(user.getUid()).child("bookedMatches");

            ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                    //user joins a match
                    final String matchKey = dataSnapshot.getKey();
                    assert matchKey != null;
                    final DatabaseReference ref = db.getReference().child("matches").child(matchKey);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                //TODO any user is browsing this match, what happens? Crash?
                                removeUI(matchKey);
                                ref.removeEventListener(this);
                            }
                            else{
                                Match m = snapshot.getValue(Match.class);
                                addUI(m);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) { //drop out
                    String matchKey = dataSnapshot.getKey();
                    removeUI(matchKey);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
}
