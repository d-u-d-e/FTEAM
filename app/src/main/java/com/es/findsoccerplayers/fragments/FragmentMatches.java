package com.es.findsoccerplayers.fragments;

import androidx.fragment.app.Fragment;

import com.es.findsoccerplayers.adapter.MatchAdapter;
import com.es.findsoccerplayers.models.Match;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class FragmentMatches extends Fragment {

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    List<Match> matches;
    MatchAdapter matchAdapter;

    public enum SortType{dateMatchAsc, dateMatchDesc, none};

    private SortType sortType = SortType.none;

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
        int position = 0;
        int foundAt = -1;

        for(int i = 0; i < matches.size(); i++){
            Match match = matches.get(i);
            if((sortType == SortType.dateMatchDesc && m.getTimestamp() < match.getTimestamp()) ||
                    (sortType == SortType.dateMatchAsc && m.getTimestamp() > match.getTimestamp())){
                position++;
            }
            if(matches.get(i).getMatchID().equals(m.getMatchID()))
                foundAt = i;
        }

        if(foundAt == -1){ //we don't have it
            matches.add(position, m);
            matchAdapter.notifyItemInserted(position);
        }
        else{ //we have it, just update in this case, the creator might have changed the description for example
            if(sortType == SortType.none){
                matches.set(foundAt, m);
                matchAdapter.notifyItemChanged(foundAt);
            }
            else{
                matches.remove(foundAt);
                if(position <= foundAt)
                    matches.add(position, m);
                else
                    matches.add(position - 1, m);
                matchAdapter.notifyDataSetChanged();
            }
        }
    }

    public SortType getSortType(){
        return sortType;
    }

    public void setOrderNone(){
        sortType = SortType.none;
    }

    public void sortByMatchDate(boolean ascending){
        if(sortType == SortType.none){
            sortType = ascending? SortType.dateMatchAsc:SortType.dateMatchDesc;
            matches.sort(new ComparatorByMatchDate(ascending));
            matchAdapter.notifyDataSetChanged();
        }
        else if(sortType == SortType.dateMatchAsc && !ascending){
            sortType = SortType.dateMatchDesc;
            Collections.reverse(matches);
            matchAdapter.notifyDataSetChanged();
        }
        else if(sortType == SortType.dateMatchDesc && ascending){
            sortType = SortType.dateMatchAsc;
            Collections.reverse(matches);
            matchAdapter.notifyDataSetChanged();
        }
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
