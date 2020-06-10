package com.es.findsoccerplayers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentInfoBookedMatch extends Fragment {

    private String relatedMatch;
    private TextView field, day, time, money, missingPlayers, description;
    private Button retireBtn;
    private FirebaseDatabase db;
    private Match m;

    public FragmentInfoBookedMatch(String relatedMatch){
        super();
        this.relatedMatch = relatedMatch;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_info_booked_match, container, false);

        field = view.findViewById(R.id.frag_info_booked_match_fieldTV);
        day = view.findViewById(R.id.frag_info_booked_match_dayTV);
        time = view.findViewById(R.id.frag_info_booked_match_timeTV);
        money= view.findViewById(R.id.frag_info_booked_match_moneyTV);
        missingPlayers = view.findViewById(R.id.frag_info_booked_match_missingPlayersTV);
        description = view.findViewById(R.id.frag_info_booked_match_descriptionTV);
        retireBtn = view.findViewById(R.id.frag_info_booked_match_retireBtn);

        retireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "TODO", Toast.LENGTH_SHORT).show();
            }
        });

        db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("matches").child(relatedMatch);



        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                m = dataSnapshot.getValue(Match.class);
                field.setText(m.getPlaceName());
                day.setText(m.getMatchData());
                time.setText(m.getMatchHour());
                money.setText("----");                                              //TODO
                missingPlayers.setText(Integer.toString(m.getPlayersNumber()));
                description.setText(m.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }
}
