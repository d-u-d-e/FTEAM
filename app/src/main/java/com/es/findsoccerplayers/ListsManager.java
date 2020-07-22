package com.es.findsoccerplayers;

import com.es.findsoccerplayers.fragments.FragmentAvailableMatches;
import com.es.findsoccerplayers.fragments.FragmentBookedMatches;
import com.es.findsoccerplayers.fragments.FragmentYourMatches;

public class ListsManager {

    private static ListsManager listsManager = null;
    private FragmentAvailableMatches availableMatches;
    private FragmentYourMatches yourMatches;
    private FragmentBookedMatches bookedMatches;

    public static ListsManager getInstance(){
            if(listsManager == null){
                listsManager = new ListsManager();
            }
            return listsManager;
    }

    public static void setFragment(FragmentAvailableMatches frag){
        getInstance().availableMatches = frag;
    }

    public static void setFragment(FragmentYourMatches frag){
        getInstance().yourMatches = frag;
    }

    public static void setFragment(FragmentBookedMatches frag){
        getInstance().bookedMatches = frag;
    }

    public static FragmentBookedMatches getFragmentBookedMatches(){
        return getInstance().bookedMatches;
    }

    public static FragmentYourMatches getFragmentYourMatches(){
        return getInstance().yourMatches;
    }

    public static FragmentAvailableMatches getFragmentAvailableMatches(){
        return getInstance().availableMatches;
    }

}
