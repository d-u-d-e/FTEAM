package com.es.findsoccerplayers.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.es.findsoccerplayers.pickers.NumberPickerFragment;
import com.es.findsoccerplayers.pickers.TimePickerFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

public class FragmentInfoMatch extends Fragment implements OnMapReadyCallback, DatePickerFragment.OnCompleteListener,
        TimePickerFragment.OnCompleteListener, NumberPickerFragment.OnCompleteListener{

    private static final String TAG = "ActivityInfoMatch";

    private Match m;
    private String type;
    private GoogleMap map;
    private static final int MAPS_REQUEST_CODE = 42;
    private double longitude;
    private double latitude;

    TextView place, date, time, money, missingPlayers, desc;

    Marker marker;

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
        final ImageView editDesc = view.findViewById(R.id.info_match_imageEditDescription);
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

                DatePickerFragment dialog = new DatePickerFragment(getActivity(), FragmentInfoMatch.this);
                dialog.show(manager,"datePicker");
            }
        });

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment(getActivity(), FragmentInfoMatch.this);
                timePicker.show(manager, "hourPicker");
            }
        });

        editPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment playerNumb = new NumberPickerFragment(getActivity(), FragmentInfoMatch.this, getString(R.string.how_many_players));
                playerNumb.show(manager, "playerPicker");
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
                Utils.showUnimplementedToast(getContext());
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng location = new LatLng(m.getLatitude(), m.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        marker = map.addMarker(new MarkerOptions().position(location).title(m.getPlaceName()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK){
            longitude = data.getDoubleExtra(ActivityMaps.LONGITUDE, longitude);
            latitude = data.getDoubleExtra(ActivityMaps.LATITUDE, latitude);
            String nameOfThePlace = data.getStringExtra(ActivityMaps.PLACE_NAME);
            LatLng location = new LatLng(latitude, longitude);
            marker.remove();
            marker = map.addMarker(new MarkerOptions().position(location).title(nameOfThePlace));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));

            place.setText(nameOfThePlace);
            m.setLatitude(latitude);
            m.setLongitude(longitude);
            m.setPlaceName(nameOfThePlace);
            updateMatch(m); //TODO not good here, only when user returns to main window
            Toast.makeText(getContext(), "Changes saved!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        date.setText(Utils.getDate(c.getTimeInMillis()));
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        time.setText(String.format("%02d:%02d", hour, minute));
    }

    @Override
    public void onNumberSet(int number) {
        missingPlayers.setText(Integer.toString(number));
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