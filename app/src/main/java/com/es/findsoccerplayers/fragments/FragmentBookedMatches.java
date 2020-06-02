package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.es.findsoccerplayers.ActivityInfoBookedMatch;
import com.es.findsoccerplayers.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentBookedMatches extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booked_matches, container, false);

        TextView es = view.findViewById(R.id.esempio);
        es.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ActivityInfoBookedMatch.class));
            }
        });

        return view;
    }
}
