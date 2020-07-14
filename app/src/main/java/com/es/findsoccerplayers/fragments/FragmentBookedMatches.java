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

import com.es.findsoccerplayers.ActivityInfoBookedMatch;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FragmentBookedMatches extends Fragment {

    private FirebaseDatabase db = FirebaseDatabase.getInstance();
    private MatchAdapter matchAdapter;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    private List<Match> matches = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_booked_matches, container, false);

        recyclerView = view.findViewById(R.id.frag_booked_list);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getContext());
        matchAdapter = new MatchAdapter(matches);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(matchAdapter);
        recyclerView.setItemAnimator(null);

        matchAdapter.setOnItemClickListener(new MatchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getContext(), ActivityInfoBookedMatch.class);
                i.putExtra("match", matches.get(position).getMatchID());
                startActivity(i);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref =
                db.getReference().child("users").child(user.getUid()).child("matches");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                matches.clear();
                DatabaseReference ref;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    String key = snapshot.getKey();
                    Boolean isMember = snapshot.child("member").getValue(Boolean.class);
                    if(isMember == null) throw new IllegalStateException("missing member field in database");
                    ref = db.getReference("matches/" + key);
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Match m = dataSnapshot.getValue(Match.class);
                            if(m != null){
                                matches.add(m);
                                matchAdapter.notifyItemInserted(matches.size()-1);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
