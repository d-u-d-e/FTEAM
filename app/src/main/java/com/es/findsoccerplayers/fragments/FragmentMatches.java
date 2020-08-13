package com.es.findsoccerplayers.fragments;

import androidx.fragment.app.Fragment;

import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

abstract class FragmentMatches extends Fragment {

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    List<Match> matches;
    MatchAdapter matchAdapter;

    synchronized void removeUI(String matchID){
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

    synchronized void addUI(Match m){
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

    public void sortByMatchDate(boolean ascending){
        matches.sort(new ComparatorByMatchDate(ascending));
        matchAdapter.notifyDataSetChanged();
    }

    public void sortByCreationDate(){

    }

    static class ComparatorByMatchDate implements Comparator<Match> {
        private boolean ascending;
        ComparatorByMatchDate(boolean ascending){
            this.ascending = ascending;
        }
        @Override
        public int compare(Match o1, Match o2) {
            if(ascending)
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            else
                return Long.compare(o1.getTimestamp(), o2.getTimestamp()) * (-1);
        }
    }
}
