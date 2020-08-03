package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
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
import java.util.List;

public class FragmentYourMatches extends Fragment {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private List<Match> matches;
    private MatchAdapter matchAdapter;

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
                i.putExtra("type", "your");
                i.putExtra("position", position);
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

        readMatches();

        //recyclerView.setItemAnimator(null);

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
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    registerForMatchEvents(key);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }


    private synchronized void removeUI(String matchID){
        //check if we have this match in the list
        int i;
        for(i = 0; i < matches.size(); i++){
            if(matches.get(i).getMatchID().equals(matchID)){
                break;
            }
        }

        if(i != matches.size()) { //we have it, so we must delete it
            matches.remove(i);
            matchAdapter.notifyItemRemoved(i);
        }
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
            //just update in this case, the creator might have changed the description for example
            matches.set(i, m);
            matchAdapter.notifyItemChanged(i);
        }
    }

}

