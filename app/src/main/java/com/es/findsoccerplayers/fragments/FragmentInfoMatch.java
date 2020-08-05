package com.es.findsoccerplayers.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.es.findsoccerplayers.ActivityMain;
import com.es.findsoccerplayers.ActivityMaps;
import com.es.findsoccerplayers.ListsManager;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.models.CustomMapView;
import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.pickers.DatePickerFragment;
import com.es.findsoccerplayers.pickers.NumberPickerFragment;
import com.es.findsoccerplayers.pickers.TimePickerFragment;
import com.es.findsoccerplayers.dialogues.EditDescriptionDialogue;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class FragmentInfoMatch extends Fragment implements OnMapReadyCallback, DatePickerFragment.OnCompleteListener,
        TimePickerFragment.OnCompleteListener, NumberPickerFragment.OnCompleteListener,
        EditDescriptionDialogue.onDescriptionListener {

    private Match editedMatch, originalMatch;
    private String type;
    private GoogleMap map;
    private static final int MAPS_REQUEST_CODE = 42;

    private TextView place, date, time, missingPlayers, desc;
    private Marker marker;

    private Button editBtn;
    private boolean[] edits = new boolean[6];

    public FragmentInfoMatch(Match m, String type) {
        originalMatch = m;
        editedMatch = new Match(m);
        this.type = type; //TODO change type string to enum (is it better?)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.frag_info_match, container, false);


        place = view.findViewById(R.id.info_match_placeText);
        date = view.findViewById(R.id.info_match_dayText);
        time = view.findViewById(R.id.info_match_timeText);
        missingPlayers = view.findViewById(R.id.info_match_playersText);

        ImageView editPlace = view.findViewById(R.id.info_match_imageEditPosition);
        ImageView editDay = view.findViewById(R.id.info_match_imageEditDay);
        ImageView editTime = view.findViewById(R.id.info_match_imageEditTime);
        ImageView editPlayers = view.findViewById(R.id.info_match_imageEditPlayers);
        final ImageView editDesc = view.findViewById(R.id.info_match_imageEditDescription);
        Button actionBtn = view.findViewById(R.id.info_match_actionBtn);
        editBtn = view.findViewById(R.id.info_match_editBtn);
        desc = view.findViewById(R.id.info_match_descriptionText);

        CustomMapView mapView = view.findViewById(R.id.info_match_mapPreview);

        final FragmentManager manager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.info_match_mapFragment);
        assert mapFragment != null;
        mapView.setScrollView((ScrollView)view.findViewById(R.id.info_match_scrollview));
        mapFragment.getMapAsync(this);

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("your")) { //delete match case
                    deleteMatch(originalMatch.getMatchID());
                }else if (type.equals("available")){
                    joinMatch();
                }else{ //booked
                    dropOut();
                }
                getActivity().finish(); //terminate selectMatch (TODO do we really need to go back to main?)
                Intent i = new Intent(getContext(), ActivityMain.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        if (type.equals("your")) {
            actionBtn.setText(R.string.delete); //TODO add to strings
            editBtn.setVisibility(Button.VISIBLE);
            editDay.setVisibility(View.VISIBLE);
            editDesc.setVisibility(View.VISIBLE);
            editPlace.setVisibility(View.VISIBLE);
            editTime.setVisibility(View.VISIBLE);
            editPlayers.setVisibility(View.VISIBLE);
            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editMatch(editedMatch);
                    editBtn.setEnabled(false);
                    originalMatch = new Match(editedMatch);
                    edits = new boolean[6];
                }
            });
        } else if (type.equals("available")) {
            actionBtn.setText(R.string.join);

        } else { //booked
            actionBtn.setText("DROP OUT");
        }

        place.setText(Utils.getPreviewDescription(originalMatch.getPlaceName()));
        date.setText(Utils.getDate(originalMatch.getTimestamp()));
        time.setText(Utils.getTime(originalMatch.getTimestamp()));
        missingPlayers.setText(Integer.toString(originalMatch.getPlayersNumber()));
        desc.setText(originalMatch.getDescription());

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

        editDesc.setOnClickListener(new View.OnClickListener() {
            //TODO
            @Override
            public void onClick(View v) {
                EditDescriptionDialogue dialogue = new EditDescriptionDialogue("Insert a description", FragmentInfoMatch.this, editedMatch.getDescription());
                dialogue.show(getChildFragmentManager(), null);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng location = new LatLng(originalMatch.getLatitude(), originalMatch.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
        marker = map.addMarker(new MarkerOptions().position(location).title(originalMatch.getPlaceName()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK){

            double lng = data.getDoubleExtra(ActivityMaps.LONGITUDE, originalMatch.getLongitude());
            double lat = data.getDoubleExtra(ActivityMaps.LATITUDE, originalMatch.getLatitude());

            String nameOfThePlace = data.getStringExtra(ActivityMaps.PLACE_NAME);
            assert nameOfThePlace != null;
            LatLng location = new LatLng(lat, lng);

            if(editedMatch.getLongitude() != lng || editedMatch.getLatitude() != lat){
                editedMatch.setLatitude(lat);
                editedMatch.setLongitude(lng);
                editedMatch.setPlaceName(nameOfThePlace);

                marker.remove();
                marker = map.addMarker(new MarkerOptions().position(location).title(nameOfThePlace));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
                place.setText(Utils.getPreviewDescription(nameOfThePlace));

                updateEdits(lng != originalMatch.getLongitude() || lat != originalMatch.getLatitude(), 0);
            }
        }
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        String dateStr = Utils.getDate(c.getTimeInMillis());
        date.setText(dateStr);

        String dateTime = dateStr + " " + time.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        try {
            Date date = sdf.parse(dateTime);
            assert date != null;
            long timestamp = date.getTime();

            if(timestamp != editedMatch.getTimestamp()){
                editedMatch.setTimestamp(timestamp);
                updateEdits(originalMatch.getTimestamp() != timestamp, 1);
            }

        } catch (ParseException e) {
            throw new IllegalStateException("Parsing of date failed");
        }
    }

    private void updateEdits(boolean condition, int position){
        edits[position] = condition;
        int count = 0;

        for(boolean b: edits)
            if(b) count++;

        editBtn.setEnabled(count > 0);
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        long editTimestamp = editedMatch.getTimestamp();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(editTimestamp);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.HOUR_OF_DAY, hour);
        long newTimestamp = c.getTimeInMillis();

        if(newTimestamp != editTimestamp){
            time.setText(Utils.getTime(newTimestamp));
            editedMatch.setTimestamp(newTimestamp);
            updateEdits(newTimestamp != originalMatch.getTimestamp(), 2);
        }
    }

    @Override
    public void onNumberSet(int number) {
        if(editedMatch.getPlayersNumber() != number){
            missingPlayers.setText(Integer.toString(number));
            editedMatch.setPlayersNumber(number);
            updateEdits(number != originalMatch.getPlayersNumber(), 3);
        }
    }

    private void editMatch(final Match m){
        //update the match on the db if some value are changed

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("matches/" + m.getMatchID());

        ref.setValue(m, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if(error != null)
                    Utils.showErrorToast(getActivity(), error.getMessage());
                else{ //match successfully updated
                    Toast.makeText(getActivity(), "Match successfully updated", Toast.LENGTH_SHORT).show();
                    if(Utils.isOffline(getContext()))
                        Utils.showOfflineWriteToast(getContext());
                }
            }
        });
    }

    private void deleteMatch(String matchID){

        HashMap<String, Object> map = new HashMap<>();
        map.put("matches/" + matchID, null);
        map.put("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/createdMatches/" + matchID, null);
        map.put("chats/" + matchID, null);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if(error != null)
                    Utils.showErrorToast(getActivity(), error.getMessage());
                else{ //match successfully deleted
                    Toast.makeText(getActivity(), "Match successfully deleted", Toast.LENGTH_SHORT).show();
                    if(Utils.isOffline(getContext()))
                        Utils.showOfflineWriteToast(getContext());
            }
        }});
    }

    @Override
    public void onDescriptionSet(String desc) {
        if(!editedMatch.getDescription().equals(desc)){
            this.desc.setText(desc);
            editedMatch.setDescription(desc);
            updateEdits(!desc.equals(originalMatch.getDescription()), 5);
        }
    }

    private void joinMatch(){
        //Add to the current user the matchID which he joined on the database
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        final String key = originalMatch.getMatchID();

        db.getReference("matches/" + key + "/playersNumber").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {

                Integer players = currentData.getValue(Integer.class);
                assert players != null;
                if(players > 0){
                    currentData.setValue(players-1);

                    HashMap<String, Object> map = new HashMap<>();
                    String uid = user.getUid();
                    map.put("users/" + uid + "/bookedMatches/" + key, Calendar.getInstance().getTimeInMillis());
                    map.put("matches/" + key + "/members/" + uid, true);
                    db.getReference().updateChildren(map);
                    return Transaction.success(currentData);
                }
                else
                    return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                Context context = ListsManager.getFragmentBookedMatches().getActivity(); //we can't use getContext(), because the activity
                //hosting this fragment will be destroyed, and could happen well before this method is called. Hence getContext() will be null,
                //and the application will crash as soon as tries to make the toast
                //context is the main activity now
                if(committed){
                    Toast.makeText(context, "You joined the match!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Either the match has been deleted or the maximum number of players have been reached", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void dropOut(){
        //Remove from the current user the matchID which he dropped out

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        final String key = originalMatch.getMatchID();


        db.getReference("matches/" + key + "/playersNumber").runTransaction(new Transaction.Handler(){
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {

                Integer players = currentData.getValue(Integer.class);
                assert players != null;
                currentData.setValue(players + 1);

                HashMap<String, Object> map = new HashMap<>();
                String uid = user.getUid();
                map.put("users/" + uid + "/bookedMatches/" + key, null);
                map.put("matches/" + key + "/members/" + uid, null); //remove
                db.getReference().updateChildren(map);

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                Context context = ListsManager.getFragmentBookedMatches().getActivity(); //we can't use getContext(), because the activity
                //hosting this fragment will be destroyed, and could happen well before this method is called. Hence getContext() will be null,
                //and the application will crash as soon as tries to make the toast
                //context is the main activity now
                if(committed){
                    Toast.makeText(context, "You successfully retired from the match!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, getString(R.string.unexpected_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}