package com.es.fteam.fragments;

import androidx.fragment.app.Fragment;

import com.es.fteam.adapter.MatchAdapter;
import com.es.fteam.models.Match;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Super class for each fragment listing matches. It contains useful methods to add or delete a match
 * taking into account the order set by the user. These methods however deal with the UI only, not with
 * database coherence or user settings.
 */
public abstract class FragmentMatches extends Fragment {

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    List<Match> matches;
    MatchAdapter matchAdapter;

    public enum SortType{dateMatchAsc, dateMatchDesc, lastUpdated};

    SortType sortType = SortType.lastUpdated;

    /**
     * Removes the specified match from the UI list, if present.
     */
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

    /**
     * Adds the specified match to the UI list; sort order is taken into account. Note that this assumes
     * that the match hasn't already been added: this will increase the performance in some cases, because
     * addUpdateUI always checks for a duplicate, thus making the search always omega(n) where n is the
     * current dimension of the list.
     */
    synchronized void addUI(Match m){

        if(sortType == SortType.lastUpdated){
            matches.add(0, m);
            matchAdapter.notifyItemInserted(0);
        }
        else{
            int position = 0;
            for(int i = 0; i < matches.size(); i++){
                Match match = matches.get(i);
                if((sortType == SortType.dateMatchDesc && m.getTimestamp() >= match.getTimestamp()) ||
                        (sortType == SortType.dateMatchAsc && m.getTimestamp() <= match.getTimestamp())){
                    break;
                }
                position++;
            }
            matches.add(position, m);
            matchAdapter.notifyItemInserted(position);
        }
    }

    /**
     * Adds or updates the specified match to the UI list; sort order is taken into account.
     * Use this method when uncertain of the presence of m in the list.
     */
    synchronized void addUpdateUI(Match m){
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

        if(foundAt == -1){ //we don't have it; match is added on top if no order is set
            matches.add(position, m);
            matchAdapter.notifyItemInserted(position);
        }
        else{ //we have it, just update in this case: the creator might have changed the description for example
            //if no order is set, this is added on top
            matches.remove(foundAt);
            if(position <= foundAt)
                matches.add(position, m);
            else
                matches.add(position - 1, m);
            matchAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Gets the current sortType for this fragment
     */
    public SortType getSortType(){
        return sortType;
    }

    /**
     * Sets the current sortType to SortType.lastUpdated
     */
    public void setOrderLastUpdated(){
        sortType = SortType.lastUpdated;
    }

    /**
     * Sorts matches by match date, ascending or descending according as the parameter is true or false
     */
    public synchronized void sortByMatchDate(boolean ascending){
        if(sortType == SortType.lastUpdated){
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

    /**
     * Comparator for sorting by match date
     */
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
