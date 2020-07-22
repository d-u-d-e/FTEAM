package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

    private static final String TAG = "YourMatches";
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

    public void onMatchCreated(Match m){
        synchronized (this){
            matches.add(m);
            matchAdapter.notifyItemInserted(matches.size()-1);
        }
    }

    public void onMatchDeleted(int position){
        synchronized (this){
            matches.remove(position);
            matchAdapter.notifyItemRemoved(position);
        }
    }

    void onMatchEdited(int position, Match newMatch){
        synchronized (this){
            matches.set(position, newMatch);
            matchAdapter.notifyItemChanged(position);
        }
    }

    private void readMatches() { //done only at beginning

        String path = "users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/createdMatches";

        DatabaseReference ref = db.getReference(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    DatabaseReference r = db.getReference("matches/" + key);
                    r.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Match m = snapshot.getValue(Match.class);
                            assert  m!= null;
                            synchronized (FragmentYourMatches.this){
                                matches.add(m);
                                matchAdapter.notifyItemInserted(matches.size()-1);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

