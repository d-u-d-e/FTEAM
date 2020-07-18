package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.models.Match;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FragmentInfoMatch extends Fragment implements OnMapReadyCallback{

    private static final String TAG = "ActivityInfoMatch";

    private Match m;
    private String type;
    private GoogleMap map;

    public FragmentInfoMatch(Match m, String type) {
        this.m = m;
        this.type = type; //TODO change type string to enum (is it better?)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.frag_info_match, container, false);


        TextView place = view.findViewById(R.id.info_match_placeText);
        TextView date = view.findViewById(R.id.info_match_dayText);
        TextView time = view.findViewById(R.id.info_match_timeText);
        TextView money = view.findViewById(R.id.info_match_moneyText);
        TextView missingPlayers = view.findViewById(R.id.info_match_playersText);

        FragmentManager manager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.info_match_mapPreview);

        Button btn = view.findViewById(R.id.info_match_btn);
        EditText desc = view.findViewById(R.id.info_match_descriptionText);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showUnimplementedToast(getActivity());
            }
        });

        if (type.equals("your")) {
            btn.setText("DELETE"); //TODO add to strings
            place.setEnabled(true);
            date.setEnabled(true);
            time.setEnabled(true);
            money.setEnabled(true);
            missingPlayers.setEnabled(true);
            desc.setEnabled(true); //TODO set up listeners to trigger edit
        } else if (type.equals("available")) {
            btn.setText("JOIN");
        } else { //booked
            btn.setText("DROP OUT");
        }
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        place.setText(m.getPlaceName());
        date.setText(Utils.getDate(m.getTimestamp()));
        time.setText(Utils.getTime(m.getTimestamp()));
        money.setText("---"); //TODO add money field to matches
        missingPlayers.setText(Integer.toString(m.getPlayersNumber()));
        desc.setText(m.getDescription());
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);
        LatLng location = new LatLng(m.getLatitude(), m.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        map.addMarker(new MarkerOptions().position(location).title(m.getPlaceName()));
    }
}