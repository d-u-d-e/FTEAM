package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivitySelectMatch;
import com.es.findsoccerplayers.ActivitySetLocation;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentAvailableMatches extends Fragment {

    private static final String TAG = "AvailableMatches";
    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private List<Match> matches;
    private MatchAdapter matchAdapter;

    private static class PositionSettings{
        LatLng position;
        double radius;
    }
    private PositionSettings positionSettings;

    //TODO show only matches for which missing players is not zero

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
        //recyclerView.setItemAnimator(null);

        positionSettings = getPositionSettings();

        if(positionSettings == null){
            //TODO launch activity set location?
            Toast.makeText(getActivity(), R.string.preferred_pos_not_set, Toast.LENGTH_SHORT).show();
        }
        sync(); //onChildAdded is called once for each element at the start
        return view;
    }

    private boolean isLocationNearby(double latitude, double longitude){

        /* this will show every match, regardless of its position in the world, if the user
           didn't set any preferred position */ //TODO is this ok?
        if(positionSettings == null) return true;

        float[] result = new float[1];
        Location.distanceBetween(positionSettings.position.latitude, positionSettings.position.longitude,
                latitude, longitude, result); //result in meters
        return (result[0] <= positionSettings.radius);
    }

    private PositionSettings getPositionSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String pLng = sharedPreferences.getString(ActivitySetLocation.LONGITUDE, null);
        String pLat = sharedPreferences.getString(ActivitySetLocation.LATITUDE, null);
        String pRad =  sharedPreferences.getString(ActivitySetLocation.RADIUS, null);

        if(pLat == null || pLng == null) return null;
        assert pRad != null;

        PositionSettings ps = new PositionSettings();
        ps.position = new LatLng(Double.parseDouble(pLat), Double.parseDouble(pLng));
        ps.radius = Double.parseDouble(pRad);
        return ps;
    }

    public void updateListWithNewPositionSettings(){ //triggered when position settings are updated

        //the user has updated the position settings
        positionSettings = getPositionSettings();

        assert positionSettings != null;

        DatabaseReference ref = db.getReference().child("matches");
        //this is expensive for huge data, but we
        //have no means to select matches distant x meters apart from the user position directly
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
                        assert m != null;
                        if(isLocationNearby(m.getLatitude(), m.getLongitude()))
                            matches.add(m);
                    }
                    matchAdapter.notifyDataSetChanged();
                } //release the object only when we have read everything
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sync(){
        DatabaseReference ref = db.getReference().child("matches");
        //this is expensive for huge data, but we have no means to select matches
        // distant x meters apart from the user position directly in the database

        ChildEventListener syncListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) { //match created by some user
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                if(isLocationNearby(m.getLatitude(), m.getLongitude()))
                    FragmentAvailableMatches.this.addUI(m);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //any user modifies a match
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                int i;
                if (isLocationNearby(m.getLatitude(), m.getLongitude()))
                    FragmentAvailableMatches.this.addUI(m); //update entry or add a new entry
                else
                    FragmentAvailableMatches.this.removeUI(m); //delete entry if exists
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //some user deleted this match from the database
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                FragmentAvailableMatches.this.removeUI(m); //delete entry if exists
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

    private synchronized void addUI(Match m){
        //check if we have this match in the list
        int i;
        for(i = 0; i < matches.size(); i++){
            if(matches.get(i).getMatchID().equals(m.getMatchID())){
                break;
            }
        }

        if(i == matches.size()) { //we don't have it
                matches.add(m);
                matchAdapter.notifyItemInserted(matches.size()-1);
        }
        else{
            //just update in this case, he might have changed the description for example
            matches.set(i, m);
            matchAdapter.notifyItemChanged(i);
        }
    }

    private synchronized void removeUI(Match m){
        //check if we have this match in the list
        int i;
        for(i = 0; i < matches.size(); i++){
            if(matches.get(i).getMatchID().equals(m.getMatchID())){
                break;
            }
        }

        if(i != matches.size()) { //we have it, so we must delete it
            matches.remove(i);
            matchAdapter.notifyItemRemoved(i);
        }
    }
}