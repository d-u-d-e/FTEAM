package com.es.findsoccerplayers.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.es.findsoccerplayers.ActivityMaps;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.pickers.DatePickerFragment;
import com.es.findsoccerplayers.pickers.NumberPickerDialog;
import com.es.findsoccerplayers.pickers.TimePickerFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class FragmentInfoMatch extends Fragment implements OnMapReadyCallback, DatePickerFragment.OnCompleteListener,
        TimePickerFragment.OnCompleteListener{

    private static final String TAG = "ActivityInfoMatch";

    private Match m;
    private String type;
    private GoogleMap map;
    private static final int MAPS_REQUEST_CODE = 42;
    private double longitude;
    private double latitude;

    TextView place, date, time, money, missingPlayers, desc;

    public FragmentInfoMatch(Match m, String type) {
        this.m = m;
        this.type = type; //TODO change type string to enum (is it better?)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.frag_info_match, container, false);


        place = view.findViewById(R.id.info_match_placeText);
        date = view.findViewById(R.id.info_match_dayText);
        time = view.findViewById(R.id.info_match_timeText);
        money = view.findViewById(R.id.info_match_moneyText);
        missingPlayers = view.findViewById(R.id.info_match_playersText);

        ImageView editPlace = view.findViewById(R.id.info_match_imageEditPosition);
        ImageView editDay = view.findViewById(R.id.info_match_imageEditDay);
        ImageView editTime = view.findViewById(R.id.info_match_imageEditTime);
        ImageView editPlayers = view.findViewById(R.id.info_match_imageEditPlayers);
        ImageView editMoney = view.findViewById(R.id.info_match_imageEditMoney);
        ImageView editDesc = view.findViewById(R.id.info_match_imageEditDescription);
        Button btn = view.findViewById(R.id.info_match_btn);
        desc = view.findViewById(R.id.info_match_descriptionText);

        final FragmentManager manager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.info_match_mapPreview);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo
                Utils.showUnimplementedToast(getActivity());
            }
        });

        if (type.equals("your")) {
            btn.setText("DELETE"); //TODO add to strings
        } else if (type.equals("available")) {
            btn.setText("JOIN");
            editDay.setVisibility(View.INVISIBLE);
            editDesc.setVisibility(View.INVISIBLE);
            editMoney.setVisibility(View.INVISIBLE);
            editPlace.setVisibility(View.INVISIBLE);
            editTime.setVisibility(View.INVISIBLE);
            editPlayers.setVisibility(View.INVISIBLE);
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

        editPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ActivityMaps.class);
                startActivityForResult(intent, MAPS_REQUEST_CODE);
            }
        });

        editDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getActivity().getSupportFragmentManager(),"datePicker");
            }
        });

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getActivity().getSupportFragmentManager(), "hourPicker");
            }
        });

        editPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment playerNumb = new NumberPickerDialog();
                playerNumb.show(getActivity().getSupportFragmentManager(), "playerPicker");
            }
        });

        editMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                Utils.showUnimplementedToast(getContext());
            }
        });

        editDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //map.setMyLocationEnabled(true);
        LatLng location = new LatLng(m.getLatitude(), m.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        map.addMarker(new MarkerOptions().position(location).title(m.getPlaceName()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK){
            longitude = data.getDoubleExtra(ActivityMaps.LONGITUDE, longitude);
            latitude = data.getDoubleExtra(ActivityMaps.LATITUDE, latitude);
            String nameOfThePlace = data.getStringExtra(ActivityMaps.PLACE_NAME);
            place.setText(nameOfThePlace);
            m.setLatitude(latitude);
            m.setLongitude(longitude);
            m.setPlaceName(nameOfThePlace);
            updateMatch(m);
            Toast.makeText(getContext(), "Change saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        date.setText(Utils.getDate(c.getTimeInMillis()));
        Toast.makeText(getContext(), "Change saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        time.setText(String.format("%02d:%02d", hour, minute));
        Toast.makeText(getContext(), "Change saved!", Toast.LENGTH_SHORT).show();
    }

    private void updateMatch(Match m){ //TODO this has to be moved to the proper class, when edit is working

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("matches/" + m.getMatchID());

        ref.setValue(m, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if(error != null)
                    Utils.showErrorToast(getActivity(), error.getMessage());
                else{ //match successfully updated
                    Toast.makeText(getActivity(), "Match successfully updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(!Utils.isOnline(getContext()))
            Utils.showOfflineToast(getContext());

    }

}