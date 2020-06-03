package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;

import java.util.List;

public class FragmentYourMatches extends Fragment {

    private MatchAdapter matchAdapter;
    private List<Match> matches;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_matches, container, false);

        recyclerView = view.findViewById(R.id.frag_yours_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        matchAdapter = new MatchAdapter(getContext(), matches);
        recyclerView.setAdapter(matchAdapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
