package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.es.findsoccerplayers.ActivityCreateMatch;
import com.es.findsoccerplayers.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FragmentAvailableMatches extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_available_matches, container, false);

        FloatingActionButton fab_create_match = view.findViewById(R.id.fab_create_match);
        fab_create_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ActivityCreateMatch.class));
            }
        });
        return view;
    }
}