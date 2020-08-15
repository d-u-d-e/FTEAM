package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivityLogin;
import com.es.findsoccerplayers.ActivitySelectMatch;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
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

        //default sort type is by match date ascending
        sortType = SortType.dateMatchAsc;

        sync();
        return view;
    }

    private void sync(){

            DatabaseReference ref =
                    db.getReference().child("users").child(ActivityLogin.currentUserID).child("bookedMatches");

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
                                String currentMatch = ActivitySelectMatch.matchID; //null if this activity is not running
                                if(currentMatch != null && currentMatch.equals(matchKey)){
                                    //this means the user is browsing a match which got deleted, so we notify him
                                    Intent i = new Intent(FragmentBookedMatches.this.getContext(), ActivitySelectMatch.class);
                                    i.setAction("finishOnMatchDeleted");
                                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(i);
                                    Utils.showErrorToast(getActivity(), getString(R.string.match_deleted_by_creator));
                                }
                                //otherwise the listener for this match is removed, since the user is not more interested in it
                                listenerHashMap.remove(matchKey);
                                ref.removeEventListener(this);
                                removeUI(matchKey);
                                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove(ActivityLogin.currentUserID + "." + FragmentChat.LAST_VIEWED_MESSAGE);
                                editor.apply();
                            }
                            else{
                                Match m = snapshot.getValue(Match.class);
                                //TODO
                                //notify select match to update in real time if the creator modifies the match
                                //and the user is browsing this match
                                assert m != null;
                                if(m.getTimestamp() > Calendar.getInstance().getTimeInMillis())
                                    addUpdateUI(m);
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
                public void onChildRemoved(DataSnapshot dataSnapshot) { //the user drops out
                    String matchKey = dataSnapshot.getKey();
                    DatabaseReference ref = db.getReference().child("matches/" + matchKey);
                    assert matchKey != null;
                    ValueEventListener listener = listenerHashMap.remove(matchKey);
                    assert listener != null;
                    ref.removeEventListener(listener); //not more interested in changes from the db
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
