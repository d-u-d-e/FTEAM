package com.es.findsoccerplayers.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivityLogin;
import com.es.findsoccerplayers.ActivitySelectMatch;
import com.es.findsoccerplayers.ActivitySetLocation;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class FragmentAvailableMatches extends FragmentMatches {

    private static class PositionSettings{
        LatLng position;
        double radius;
    }
    private PositionSettings positionSettings;
    private TextView message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        matches = new ArrayList<>();
        matchAdapter = new MatchAdapter(matches);
        matchAdapter.setOnItemClickListener(new MatchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getContext(), ActivitySelectMatch.class);
                i.putExtra("match", matches.get(position));
                i.putExtra("type", "available");
                startActivity(i);
            }
        });

        View view = inflater.inflate(R.layout.frag_available_matches, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.frag_available_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(matchAdapter);
        recyclerView.setMotionEventSplittingEnabled(false);
        //recyclerView.setItemAnimator(null);

        positionSettings = getPositionSettings();

        //this message simply shows the current preference radius
        message = view.findViewById(R.id.availableMatchMessage);

        if(positionSettings == null){
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity()).setMessage(R.string.preferred_pos_not_set);
            b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(getActivity(), ActivitySetLocation.class);
                    startActivity(i); //setting a location will trigger the update of the list form the db
                }
            });
            b.setCancelable(false);
            b.show();
        }
        else{
            int km = (int) positionSettings.radius/1000;
            message.setText(String.format(getString(R.string.frag_avail_matches_preference_msg), km));
            sync();
        }

        sortType = SortType.dateMatchAsc;

        return view;
    }

    /**
     * Returns true if the position (latitude, longitude) is within the preferred radius from
     * the preferred position
     */
    private boolean isLocationNearby(double latitude, double longitude){

        assert positionSettings != null;

        float[] result = new float[1];
        Location.distanceBetween(positionSettings.position.latitude, positionSettings.position.longitude,
                latitude, longitude, result); //result in meters
        return (result[0] <= positionSettings.radius);
    }

    /**
     * Get the current preferred position from the preferences. As always each string key is prepended by
     * the current user + dot
     */
    private PositionSettings getPositionSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String pLng = sharedPreferences.getString(ActivityLogin.currentUserID + "." + ActivitySetLocation.LONGITUDE, null);
        String pLat = sharedPreferences.getString(ActivityLogin.currentUserID + "." + ActivitySetLocation.LATITUDE, null);
        String pRad =  sharedPreferences.getString(ActivityLogin.currentUserID + "." + ActivitySetLocation.RADIUS, null);

        if(pLat == null || pLng == null) return null;
        assert pRad != null;

        PositionSettings ps = new PositionSettings();
        ps.position = new LatLng(Double.parseDouble(pLat), Double.parseDouble(pLng));
        ps.radius = Double.parseDouble(pRad);
        return ps;
    }

    /**
     * This is triggered by ActivitySetLocation upon setting the new preferred position and radius
     */
    public void onNewPositionSet(){

        if(positionSettings == null){ //never set before
            positionSettings = getPositionSettings();
            sync();
        }
        else{
            positionSettings = getPositionSettings();
            readAllRelevantMatches(); //read all over with new preferences.
        }

        int km = (int) positionSettings.radius/1000;
        message.setText(String.format(getString(R.string.frag_avail_matches_preference_msg), km));
    }

    /**
     * Repopulate the list with new (possibly different) matches, according to the preferred position
     */
    private void readAllRelevantMatches(){

        DatabaseReference ref = db.getReference().child("matches");

        //TODO
        //this is expensive for a huge database, but we
        //can't select matches distant x meters from the user position directly
        //in the database

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //prevent the attached listener to update the list
                //in the middle of a full read
                synchronized (FragmentAvailableMatches.this){
                    matches.clear();
                    for(DataSnapshot data: dataSnapshot.getChildren()){
                        Match m = data.getValue(Match.class);
                        boolean booked = data.child("members/" + ActivityLogin.currentUserID).exists();
                        assert m != null;
                        //we show relevant matches only, this is self explaining
                        if(m.getPlayersNumber() > 0 && m.getTimestamp() > Calendar.getInstance().getTimeInMillis() && !booked && isLocationNearby(m.getLatitude(), m.getLongitude())
                                && !ActivityLogin.currentUserID.equals(m.getCreatorID()))
                        addUI(m);
                    }
                    matchAdapter.notifyDataSetChanged();
                } //release the object only when we have read everything
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Synchronize with the database. For a first read, onChildAdded is always called.
     */
    private void sync(){

        DatabaseReference ref = db.getReference().child("matches");

        //TODO
        //this is expensive for huge data, but we have no means to select matches
        // distant x meters apart from the user position directly in the database

        ChildEventListener syncListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) { //match created by some user
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                //see if it is relevant to the user
                boolean booked = dataSnapshot.child("members/" + ActivityLogin.currentUserID).exists();
                if(m.getPlayersNumber() > 0 && m.getTimestamp() > Calendar.getInstance().getTimeInMillis() && !booked && isLocationNearby(m.getLatitude(), m.getLongitude())
                        && !ActivityLogin.currentUserID.equals(m.getCreatorID()))
                    FragmentAvailableMatches.this.addUI(m);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { //any user modifies a match

                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                if(!ActivityLogin.currentUserID.equals(m.getCreatorID())){ //if I am the creator, then m is not even listed
                    boolean booked = dataSnapshot.child("members/" + ActivityLogin.currentUserID).exists();
                    //TODO notify select match to update in real time
                    if(booked || m.getPlayersNumber() == 0 || !isLocationNearby(m.getLatitude(), m.getLongitude()) || m.getTimestamp() < Calendar.getInstance().getTimeInMillis())
                        FragmentAvailableMatches.this.removeUI(m.getMatchID());
                    else
                        FragmentAvailableMatches.this.addUpdateUI(m);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //some user deleted this match from the database
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                FragmentAvailableMatches.this.removeUI(m.getMatchID()); //delete entry if exists
                String matchBrowsed = ActivitySelectMatch.matchID; //non null if this activity is running
                if(matchBrowsed != null && matchBrowsed.equals(m.getMatchID()) && !ActivityLogin.currentUserID.equals(m.getCreatorID())){
                    //this means the user is browsing a match which got deleted, so we notify him
                    Intent i = new Intent(FragmentAvailableMatches.this.getContext(), ActivitySelectMatch.class);
                    i.setAction("finishOnMatchDeleted");
                    startActivity(i);
                    Utils.showErrorToast(getActivity(), getString(R.string.match_deleted_by_creator), false);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.addChildEventListener(syncListener);
    }
}