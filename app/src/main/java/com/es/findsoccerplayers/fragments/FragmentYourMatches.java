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
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
                Utils.showUnimplementedToast(getActivity());
            }
        });


        View view = inflater.inflate(R.layout.frag_your_matches, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.frag_yours_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(matchAdapter);
        //recyclerView.setItemAnimator(null);

        FloatingActionButton fabCreateMatch = view.findViewById(R.id.fab_create_match);
        fabCreateMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ActivityCreateMatch.class));
            }
        });

        sync();
        return view;
    }

    private void sync(){
        //called every time the "user" create a new match
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref =
                db.getReference().child("users").child(user.getUid()).child("createdMatches");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //user creates a match or joins a match
                final String matchKey = dataSnapshot.getKey();
                DatabaseReference ref = db.getReference().child("matches").child(matchKey);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Match m = dataSnapshot.getValue(Match.class);
                        synchronized (FragmentYourMatches.this){
                            matches.add(m);
                            matchAdapter.notifyItemInserted(matches.size()-1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { //match deleted
                String matchKey = dataSnapshot.getKey();
                int i = 0;
                synchronized (FragmentYourMatches.this){
                    for(i = 0; i < matches.size(); i++){
                        if(matches.get(i).getMatchID().equals(matchKey))
                            break;
                    }
                    matches.remove(i);
                    matchAdapter.notifyItemRemoved(i);
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

