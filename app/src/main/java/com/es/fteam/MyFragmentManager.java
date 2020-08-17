package com.es.fteam;

import com.es.fteam.fragments.FragmentAvailableMatches;
import com.es.fteam.fragments.FragmentBookedMatches;
import com.es.fteam.fragments.FragmentChat;
import com.es.fteam.fragments.FragmentYourMatches;

/**
 * This helps keep track of useful fragments; not every fragment can be created by using a tag, or id, hence
 * we cannot find it immediately with android api
 */
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

    public static FragmentChat getFragmentChat(){
        return getInstance().fragmentChat;
    }

    public static FragmentYourMatches getFragmentYourMatches(){
        return getInstance().yourMatches;
    }

    static FragmentAvailableMatches getFragmentAvailableMatches(){
        return getInstance().availableMatches;
    }
}
