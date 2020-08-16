package com.es.findsoccerplayers.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.es.findsoccerplayers.ActivityLogin;
import com.es.findsoccerplayers.ActivityMain;
import com.es.findsoccerplayers.ActivityMaps;
import com.es.findsoccerplayers.MyFragmentManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.messaging.FirebaseMessaging;

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
    //can be yours, available, booked depending on which tab it is from. Type can also assume another value, see below.
    private String type;
    private GoogleMap map;
    private static final int MAPS_REQUEST_CODE = 42;

    private TextView place, date, time, missingPlayers, desc;
    private Marker marker;

    private Button editBtn;
    //to keep track of each edit done, in case this match is from the "your matches" tab
    private boolean[] edits = new boolean[5];

    public FragmentInfoMatch(Match m, String type) {
        originalMatch = m;
        editedMatch = new Match(m);
        this.type = type;
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

        //This custom view map is perfect for maps inside a scrollView, see class for details
        CustomMapView mapView = view.findViewById(R.id.info_match_mapPreview);

        final FragmentManager manager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.info_match_mapFragment);
        assert mapFragment != null;
        mapView.setScrollView((ScrollView)view.findViewById(R.id.info_match_scrollview));
        mapFragment.getMapAsync(this);

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type) {
                    case "yours":  //delete match case
                        deleteMatch(originalMatch.getMatchID());
                        break;
                    case "available":
                        joinMatch();
                        break;
                    case "booked":  //booked
                        dropOut();
                        break;
                }
                getActivity().finish(); //terminate selectMatch
                Intent i = new Intent(getContext(), ActivityMain.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
            }
        });

        switch (type){
            case "yours":
                actionBtn.setText(R.string.frag_info_match_delete);
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
                break;
            case "available":
                actionBtn.setText(R.string.frag_info_match_join);
                break;
            case "booked":  //booked
                actionBtn.setText(R.string.frag_info_match_drop);
                break;
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
                DialogFragment playerNumb = new NumberPickerFragment(getActivity(), FragmentInfoMatch.this, getString(R.string.frag_info_match_how_many_players));
                playerNumb.show(manager, "playerPicker");
            }
        });

        editDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditDescriptionDialogue dialogue = new EditDescriptionDialogue(getString(R.string.insert_description), FragmentInfoMatch.this, editedMatch.getDescription());
                dialogue.show(getChildFragmentManager(), null);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng location = new LatLng(originalMatch.getLatitude(), originalMatch.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
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

    /**
     * If an edit is made to the original match, editBtn will be set to enabled
     * If no edit has been made, then editBtn will be set to disabled
     * The param condition tells whether an edit has been made or not to the specified field at position
     * So if at least one condition is true (or count > 0) editBtn is enabled (so on)
     */
    private void updateEdits(boolean condition, int position){
        edits[position] = condition;
        int count = 0;

        for(boolean b: edits)
            if(b) count++;

        if(count > 0)
            editBtn.setEnabled(true);
        else
            editBtn.setEnabled(false);
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

    /**
     * Update the match on the db if some values are changed by the user. No useless update is done
     * because editBtn is intelligent (see updateEdits())
     * */
    private void editMatch(final Match m){
        if(m.getTimestamp() < Calendar.getInstance().getTimeInMillis()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setMessage(R.string.error_time_set).setTitle(R.string.joke_title_for_time_error).setIcon(R.drawable.ic_access_time)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
        } else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("matches/" + m.getMatchID());

            ref.setValue(m, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError error, DatabaseReference ref) {
                    Context context = MyFragmentManager.getFragmentYourMatches().getActivity(); //we can't use getContext(), because the activity
                    //hosting this fragment will be destroyed, and could happen well before this method is called. Hence getContext() will be null,
                    //and the application will crash as soon as tries to make the toast
                    //context is the main activity now
                    if(error != null)
                        Utils.showErrorToast(context, error.getMessage(), true);
                    else //match successfully updated
                        Utils.showToast(context, getString(R.string.match_updated_success), true);
                }
            });
            if(Utils.isOffline(getActivity()))
                Utils.showOfflineWriteToast(getActivity(), false);
        }
    }

    private void deleteMatch(String matchID){

        HashMap<String, Object> map = new HashMap<>();
        map.put("matches/" + matchID, null);
        map.put("users/" + ActivityLogin.currentUserID + "/createdMatches/" + matchID, null);
        map.put("chats/" + matchID, null);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                Context context = MyFragmentManager.getFragmentYourMatches().getActivity(); //we can't use getContext(), because the activity
                //hosting this fragment will be destroyed, and could happen well before this method is called. Hence getContext() will be null,
                //and the application will crash as soon as this code tries to make the toast
                //context is the main activity now
                if(error != null)
                    Utils.showErrorToast(context, error.getMessage(), true);
                else//match successfully deleted
                    Utils.showToast(context, getString(R.string.match_deleted_success), false);
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(ActivityLogin.currentUserID + "." + FragmentChat.LAST_VIEWED_MESSAGE);
        editor.apply();
        FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

        if(Utils.isOffline(getActivity()))
            Utils.showOfflineWriteToast(getActivity(), false);
    }

    @Override
    public void onDescriptionSet(String desc) {
        if(!editedMatch.getDescription().equals(desc)){
            this.desc.setText(desc);
            editedMatch.setDescription(desc);
            updateEdits(!desc.equals(originalMatch.getDescription()), 4);
        }
    }

    /**
     * Called when the user joins a new match. This checks whether the players number in the match info
     * is at least 1. Otherwise the match has reached the maximum amount of needed players, and the
     * user is prompted with a failure toast
     * */
    private void joinMatch(){

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
                    map.put("users/" + ActivityLogin.currentUserID + "/bookedMatches/" + key, originalMatch.getTimestamp());
                    map.put("matches/" + key + "/members/" + ActivityLogin.currentUserID, true);
                    db.getReference().updateChildren(map);
                    FirebaseMessaging.getInstance().subscribeToTopic(key);
                    return Transaction.success(currentData);
                }
                else
                    return Transaction.abort();
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                Context context = MyFragmentManager.getFragmentBookedMatches().getActivity(); //we can't use getContext(), because the activity
                //hosting this fragment will be destroyed, and could happen well before this method is called. Hence getContext() will be null,
                //and the application will crash as soon as tries to make the toast
                //context is the main activity now
                if(committed){
                    Utils.showToast(context, getString(R.string.join_success), false);
                } else {
                    Utils.showToast(context, R.string.join_fail, false);
                }
            }
        });
    }

    /**
     * Called when the user drops out from a booked match.
     * */
    private void dropOut(){

        final FirebaseDatabase db = FirebaseDatabase.getInstance();
        final String key = originalMatch.getMatchID();

        db.getReference("matches/" + key + "/playersNumber").runTransaction(new Transaction.Handler(){
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {

                Integer players = currentData.getValue(Integer.class);
                assert players != null;
                currentData.setValue(players + 1);

                HashMap<String, Object> map = new HashMap<>();
                map.put("users/" + ActivityLogin.currentUserID + "/bookedMatches/" + key, null);
                map.put("matches/" + key + "/members/" + ActivityLogin.currentUserID, null); //remove
                db.getReference().updateChildren(map);
                FirebaseMessaging.getInstance().unsubscribeFromTopic(key);
                NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(key, 1);

                //remove any preference for this match, if exists
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(ActivityLogin.currentUserID + "." + FragmentChat.LAST_VIEWED_MESSAGE);
                editor.apply();
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
                Context context = MyFragmentManager.getFragmentBookedMatches().getActivity(); //we can't use getContext(), because the activity
                //hosting this fragment will be destroyed, and could happen well before this method is called. Hence getContext() will be null,
                //and the application will crash as soon as this code tries to make the toast
                //context is the main activity now
                if(committed){
                    Utils.showToast(context, getString(R.string.drop_success), false);
                } else {
                    Utils.showToast(context, getString(R.string.unexpected_error), false);
                }
            }
        });
    }

}