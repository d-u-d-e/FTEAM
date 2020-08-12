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
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class FragmentBookedMatches extends FragmentMatches {

    private ConcurrentHashMap<String, ValueEventListener> listenerHashMap;

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

        listenerHashMap = new ConcurrentHashMap<>();

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
                    final DatabaseReference ref = db.getReference().child("matches/" + matchKey);

                    ValueEventListener listener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if(!snapshot.exists()){
                                //TODO any user is browsing this match, what happens? Crash?
                                String currentMatch = ActivitySelectMatch.matchID; //null if this activity is not running
                                if(currentMatch != null && currentMatch.equals(matchKey)){
                                    Intent i = new Intent(FragmentBookedMatches.this.getContext(), ActivitySelectMatch.class);
                                    i.setAction("finishOnMatchDeleted");
                                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(i);
                                    Utils.showErrorToast(getActivity(), "The match has been deleted by the creator");
                                }
                                listenerHashMap.remove(matchKey);
                                ref.removeEventListener(this);
                                removeUI(matchKey);
                            }
                            else{
                                Match m = snapshot.getValue(Match.class);
                                //TODO here use a similar method as above to notify any current ActivitySelectMatch to
                                //update if the creator modifies the match
                                addUI(m);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    };

                    listenerHashMap.put(matchKey, listener);
                    ref.addValueEventListener(listener);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) { //drop out
                    String matchKey = dataSnapshot.getKey();
                    DatabaseReference ref = db.getReference().child("matches/" + matchKey);
                    assert matchKey != null;
                    ValueEventListener listener = listenerHashMap.remove(matchKey);
                    assert listener != null;
                    ref.removeEventListener(listener); //not more interested in changes from the db
                    removeUI(matchKey);
                    //Unsubscribe from topic.
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(matchKey);
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
