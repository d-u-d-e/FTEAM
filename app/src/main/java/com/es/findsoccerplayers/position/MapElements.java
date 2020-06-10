package com.es.findsoccerplayers.position;

import com.google.android.gms.maps.GoogleMap;

public class MapElements {

    //enable showing my location. Check self permission first
    public static void showMyLocation(GoogleMap map){
            map.setMyLocationEnabled(true); // show a small blue circle for my position
            map.getUiSettings().setMyLocationButtonEnabled(false);// I create my own beautiful position floating action button. Don't need this
    }


}
