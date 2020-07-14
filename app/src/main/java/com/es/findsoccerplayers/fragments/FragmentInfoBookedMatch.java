package com.es.findsoccerplayers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentInfoBookedMatch extends Fragment {

    private String relatedMatch;
    private TextView field, date, time, money, missingPlayers, description;
    private Match m;

    public FragmentInfoBookedMatch(String relatedMatch){
        this.relatedMatch = relatedMatch;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_info_booked_match, container, false);

        field = view.findViewById(R.id.frag_info_booked_match_fieldTV);
        date = view.findViewById(R.id.frag_info_booked_match_dayTV);
        time = view.findViewById(R.id.frag_info_booked_match_timeTV);
        money= view.findViewById(R.id.frag_info_booked_match_moneyTV);
        missingPlayers = view.findViewById(R.id.frag_info_booked_match_missingPlayersTV);
        description = view.findViewById(R.id.frag_info_booked_match_descriptionTV);
        Button dropBtn = view.findViewById(R.id.frag_info_booked_match_dropBtn);

        dropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showUnimplementedToast(getActivity());
            }
        });

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("matches").child(relatedMatch);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m = dataSnapshot.getValue(Match.class);
                field.setText(m.getPlaceName());

                long timestamp = m.getTimestamp();

                date.setText(Utils.getDate(timestamp));
                time.setText(Utils.getTime(timestamp));
                money.setText("----");                                              //TODO
                missingPlayers.setText(Integer.toString(m.getPlayersNumber()));
                description.setText(m.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }
}
