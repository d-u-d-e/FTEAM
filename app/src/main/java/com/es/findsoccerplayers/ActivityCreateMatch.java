package com.es.findsoccerplayers;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.pickers.DatePickerFragment;
import com.es.findsoccerplayers.pickers.NumberPickerFragment;
import com.es.findsoccerplayers.pickers.TimePickerFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ActivityCreateMatch extends MyActivity implements DatePickerFragment.OnCompleteListener,
        TimePickerFragment.OnCompleteListener, NumberPickerFragment.OnCompleteListener{

    private TextView matchDate;
    private TextView matchTime;
    private TextView players;
    private TextView placeText;
    private EditText description;
    private double longitude;
    private double latitude;
    private String nameOfThePlace;

    private static final int MAPS_REQUEST_CODE = 42;
    private static final String MATCH_DATE = "matchDate";
    private static final String MATCH_HOUR = "matchHour";
    private static final String PLAYERS_NUMBER = "playerNumber";
    private static final String DESCRIPTION = "description";

    private Match match;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_create_match);

        Toolbar toolbar = findViewById(R.id.cr_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Find the views
        matchDate = findViewById(R.id.cr_match_dateText);
        matchTime = findViewById(R.id.cr_match_timeText);
        players = findViewById(R.id.cr_match_playersNumber);
        placeText = findViewById(R.id.cr_match_addPosition);
        description = findViewById(R.id.cr_match_descriptionField);
        FloatingActionButton matchFab = findViewById(R.id.cr_match_fab);

        if (savedInstanceState != null) {
            matchDate.setText(savedInstanceState.getString(MATCH_DATE));
            matchTime.setText(savedInstanceState.getString(MATCH_HOUR));
            placeText.setText(savedInstanceState.getString(ActivityMaps.PLACE_NAME));
            description.setText(savedInstanceState.getString(DESCRIPTION));
            longitude = savedInstanceState.getDouble(ActivityMaps.LONGITUDE);
            latitude = savedInstanceState.getDouble(ActivityMaps.LATITUDE);
            players.setText(savedInstanceState.getString(PLAYERS_NUMBER));

        } else {
            long timestamp = Calendar.getInstance().getTimeInMillis();
            matchDate.setText(Utils.getDate(timestamp));
            matchTime.setText(Utils.getTime(timestamp));
        }

        //Sets the date with a dialog
        matchDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment(ActivityCreateMatch.this, ActivityCreateMatch.this);
                datePicker.show(getSupportFragmentManager(), "datePicker");
            }
        });

        //Set the hour with a dialog
        matchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment(ActivityCreateMatch.this, ActivityCreateMatch.this);
                timePicker.show(getSupportFragmentManager(), "hourPicker");
            }
        });

        //Set the missing player number with dialog
        players.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment playerNumb = new NumberPickerFragment(ActivityCreateMatch.this, ActivityCreateMatch.this, getString(R.string.players_number));
                playerNumb.show(getSupportFragmentManager(), "playerPicker");
            }
        });

        //Starts MapsActivity for result (the user wants to add a position)
        placeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityCreateMatch.this, ActivityMaps.class);
                startActivityForResult(intent, MAPS_REQUEST_CODE);
            }
        });

        matchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(placeText.getText().toString().equals(getString(R.string.add_position)) ||
                    players.getText().toString().equals(getString(R.string.players))){
                    Utils.showToast(ActivityCreateMatch.this, getString(R.string.all_fields_required));
                }else{
                    match = new Match();
                    match.setDescription(description.getText().toString());

                    String dateTime = matchDate.getText().toString() + " " + matchTime.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

                    try {
                        Date d = sdf.parse(dateTime);
                        if(d == null)
                            throw new IllegalStateException("Parsing of date has failed");
                        match.setTimestamp(d.getTime());
                    } catch (ParseException e) {
                        throw new IllegalStateException("Parsing of date has failed");
                    }

                    match.setPlaceName(nameOfThePlace);
                    match.setLongitude(longitude);
                    match.setLatitude(latitude);
                    match.setPlayersNumber(Integer.parseInt(players.getText().toString()));
                    match.setCreatorID(ActivityLogin.currentUserID);
                    createMatch(match);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAPS_REQUEST_CODE && resultCode == RESULT_OK){
            longitude = data.getDoubleExtra(ActivityMaps.LONGITUDE, longitude);
            latitude = data.getDoubleExtra(ActivityMaps.LATITUDE, latitude);
            nameOfThePlace = data.getStringExtra(ActivityMaps.PLACE_NAME).replaceAll("\n", " ");
            placeText.setText(Utils.getPreviewDescription(nameOfThePlace));
        }
    }

    /**
     * Saves the state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(MATCH_DATE, matchDate.getText().toString());
        outState.putString(MATCH_HOUR, matchTime.getText().toString());
        outState.putString(ActivityMaps.PLACE_NAME, placeText.getText().toString());
        outState.putString(DESCRIPTION, description.getText().toString());
        outState.putDouble(ActivityMaps.LONGITUDE, longitude);
        outState.putDouble(ActivityMaps.LATITUDE, latitude);
        outState.putString(PLAYERS_NUMBER, players.getText().toString());
        super.onSaveInstanceState(outState);
    }

    /**
     * Set the Date
     * @param year int indicating the selected year
     * @param month int indicating the selected month
     * @param day int indicating the selected day
     */
    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        matchDate.setText(Utils.getDate(c.getTimeInMillis()));
    }

    /**
     * Set the hour
     * @param hour int indicating the selected hour
     * @param minute int indicating the selected minute
     */
    @Override
    public void onTimeSet(int hour, int minute) {
        matchTime.setText(String.format("%02d:%02d", hour, minute));
    }

    /**
     * Set the number of missing players
     * @param number selected number of missing players
     */
    @Override
    public void onNumberSet(int number) {
        players.setText(Integer.toString(number));
    }


    /**
     * Create a match and add it to the FirebaseDatabase. The match object is passed as a param.
     * @param m match object created
     */
    private void createMatch(final Match m){
      
        if(m.getTimestamp() < Calendar.getInstance().getTimeInMillis()){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage(R.string.error_time_set).setTitle(R.string.joke_title_for_time_error).setIcon(R.drawable.ic_access_time)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create().show();
        } else {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseDatabase db = FirebaseDatabase.getInstance();

            String path = "users/" + user.getUid() + "/createdMatches";

            DatabaseReference ref = db.getReference(path).push();
            final String key = ref.getKey();

            Map<String, Object> map = new HashMap<>();
            map.put(path + "/" + key, Calendar.getInstance().getTimeInMillis());
            m.setMatchID(key);
            map.put("matches/" + key, m);

            db.getReference().updateChildren(map, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null)
                        Utils.showErrorToast(ActivityCreateMatch.this, databaseError.getMessage());
                    else{ //match successfully created
                        Utils.showToast(ActivityCreateMatch.this, "Match successfully created");
                        MyFragmentManager.getFragmentYourMatches().registerForMatchEvents(key);
                    }
                }
            });
          
          FirebaseDatabase db = FirebaseDatabase.getInstance();
          String path = "users/" + ActivityLogin.currentUserID + "/createdMatches";
          DatabaseReference ref = db.getReference(path).push();
          final String key = ref.getKey();
         
          Map<String, Object> map = new HashMap<>();
          map.put(path + "/" + key, Calendar.getInstance().getTimeInMillis());
          m.setMatchID(key);
          map.put("matches/" + key, m);

          //Create and subscribe to a topic that we will use to send notification.
          FirebaseMessaging.getInstance().subscribeToTopic(key);

          if(Utils.isOffline(this))
            Utils.showOfflineWriteToast(this);
     
          Intent i = new Intent(ActivityCreateMatch.this, ActivityMain.class);
          //Activity Main is a singleton, no need to set flags (check manifest)
          startActivity(i);
          finish();
        }
    }
}