package com.es.findsoccerplayers;

import com.es.findsoccerplayers.fragments.FragmentAvailableMatches;
import com.es.findsoccerplayers.fragments.FragmentBookedMatches;
import com.es.findsoccerplayers.fragments.FragmentChat;
import com.es.findsoccerplayers.fragments.FragmentYourMatches;

public class MyFragmentManager {

    private static MyFragmentManager myFragmentManager = null;
    private FragmentAvailableMatches availableMatches;
    private FragmentYourMatches yourMatches;
    private FragmentBookedMatches bookedMatches;
    private FragmentChat fragmentChat;

    public static MyFragmentManager getInstance(){
            if(myFragmentManager == null){
                myFragmentManager = new MyFragmentManager();
            }
            return myFragmentManager;
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

    public static void setFragment(FragmentChat frag){
        getInstance().fragmentChat = frag;
    }

    static FragmentChat getFragmentChat(){
        return getInstance().fragmentChat;
    }

    public static FragmentYourMatches getFragmentYourMatches(){
        return getInstance().yourMatches;
    }

    static FragmentAvailableMatches getFragmentAvailableMatches(){
        return getInstance().availableMatches;
    }
}
