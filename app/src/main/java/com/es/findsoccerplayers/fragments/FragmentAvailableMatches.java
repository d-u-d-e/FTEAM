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
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FragmentAvailableMatches extends Fragment {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private List<Match> matches;
    private MatchAdapter matchAdapter;
    private LatLng preferredPosition;
    private double preferredRadius;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        matches = new ArrayList<>();
        matchAdapter = new MatchAdapter(matches);
        matchAdapter.setOnItemClickListener(new MatchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Utils.showUnimplementedToast(FragmentAvailableMatches.this.getActivity());

                //Intent i = new Intent(getContext(), ActivityMatch.class);
                //i.putExtra("match", matches.get(position).getMatchID());
                //startActivity(i);
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        String lng = sharedPreferences.getString(ActivitySetLocation.LONGITUDE, null);
        String lat = sharedPreferences.getString(ActivitySetLocation.LATITUDE, null);
        String rad =  sharedPreferences.getString(ActivitySetLocation.RADIUS, null);

        if(lng == null || lat == null || rad == null){
            Toast.makeText(this.getActivity(), R.string.preferred_pos_not_set, Toast.LENGTH_SHORT).show();
            //TODO should open settings?
        }
        else{
            preferredPosition = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            preferredRadius = Double.parseDouble(rad); //it's already in meters
            sync();
        }
        return view;
    }

    private boolean isLocationNearby(double latitude, double longitude){
        float[] result = new float[1];
        Location.distanceBetween(latitude, longitude,
                preferredPosition.latitude, preferredPosition.longitude, result); //result in meters
        return (result[0] <= preferredRadius);
    }

    private void sync(){
        DatabaseReference ref = db.getReference().child("matches"); //this is expensive for huge data, but we
        //have no means to select matches distant x meters apart from the user position directly
        //in the database



        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //any user creates a match
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                if(isLocationNearby(m.getLatitude(), m.getLongitude())){ //is the location relevant?
                    synchronized (FragmentAvailableMatches.this){
                        matches.add(m);
                        matchAdapter.notifyItemInserted(matches.size()-1);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //any user modifies a match
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                int i;
                synchronized (FragmentAvailableMatches.this){
                    //check if we have this match in the list
                    for(i = 0; i < matches.size(); i++){
                        if(matches.get(i).getMatchID().equals(m.getMatchID())){
                            break;
                        }
                    }

                    if(i == matches.size()) { //we don't have it
                        if(isLocationNearby(m.getLatitude(), m.getLongitude())){
                            //it is relevant (this means some user changed the position of
                            //this match and it became relevant for us)
                            matches.add(m);
                            matchAdapter.notifyItemInserted(matches.size()-1);
                        }
                        //otherwise it's not relevant, so we don't want to add it
                    }
                    else{ //we have it and some user changed some information
                        if(!isLocationNearby(m.getLatitude(), m.getLongitude())){
                            //not relevant anymore, this means he changed the position
                            matches.remove(i);
                            matchAdapter.notifyItemRemoved(i);
                        }
                        else{ //just update in this case, he might have changed the description for example
                            matches.set(i, m);
                            matchAdapter.notifyItemChanged(i);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //some user deleted this match from the database
                Match m = dataSnapshot.getValue(Match.class);
                assert m != null;
                int i;
                synchronized (FragmentAvailableMatches.this){
                    //check if we have this match in the list
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

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}