package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.pickers.DatePickerFragment;
import com.es.findsoccerplayers.pickers.NumberPickerDialog;
import com.es.findsoccerplayers.pickers.TimePickerFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;


import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityCreateMatch extends AppCompatActivity implements DatePickerFragment.OnCompleteListener,
        TimePickerFragment.OnCompleteListener{

    private TextView matchDate;
    private TextView matchTime;
    private TextView missingPlayers;
    private TextView placeText;
    private EditText description;
    private double longitude;
    private double latitude;
    long millisTime = 0;
    long millisDate = 0;
    long matchTimestamp;

    private static final String TAG = "CreateMatchActivity";
    private final String LONGITUDE = "longitude";
    private final String LATITUDE = "latitude";
    private final String PLACE_NAME = "place name";
    private final int MATCH_A_REQUEST_CODE = 42;
    private final String MATCH_DATE = "matchDate";
    private final String MATCH_HOUR = "matchHour";
    private final String PLAYERS_NUMBER = "playerNumber";
    private final String DESCRIPTION = "description";

    private Match mMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_create_match);

        Toolbar toolbar = findViewById(R.id.cr_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Find the view
        matchDate = findViewById(R.id.cr_match_dateText);
        matchTime = findViewById(R.id.cr_match_timeText);
        missingPlayers = findViewById(R.id.cr_match_playersNumber);
        placeText = findViewById(R.id.cr_match_addPosition);
        description = findViewById(R.id.cr_match_descriptionField);
        FloatingActionButton matchFab = findViewById(R.id.cr_match_fab);

        if(savedInstanceState != null){
            matchDate.setText(savedInstanceState.getString(MATCH_DATE));
            matchTime.setText(savedInstanceState.getString(MATCH_HOUR));
            placeText.setText(savedInstanceState.getString(PLACE_NAME));
            description.setText(savedInstanceState.getString(DESCRIPTION));
            longitude = savedInstanceState.getDouble(LONGITUDE);
            latitude = savedInstanceState.getDouble(LATITUDE);
            missingPlayers.setText(savedInstanceState.getString(PLAYERS_NUMBER));

        } else {
            long timestamp = Calendar.getInstance().getTimeInMillis();
            matchDate.setText(Utils.getDate(timestamp));
            matchTime.setText(Utils.getTime(timestamp));
        }

        //Set the date number with dialog
        matchDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date_picker");
            }
        });

        //Set the hour number with dialog

        matchTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "hour_picker");
            }
        });

        //Set the missing player number with dialog
        missingPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment playerNumb = new NumberPickerDialog();
                playerNumb.show(getSupportFragmentManager(), "player_picker");
            }
        });

        //Start MapsActivity for result
        placeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Crea Activity Maps e ottieni il nome del posto oppure nome citta
                Intent intent = new Intent(ActivityCreateMatch.this, ActivityMaps.class);
                startActivityForResult(intent, MATCH_A_REQUEST_CODE);
            }
        });

        mMatch = new Match();

        matchFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(placeText.getText().toString().equals(getString(R.string.add_position)) ||
                    missingPlayers.getText().toString().equals(getString(R.string.players))){
                    Toast.makeText(ActivityCreateMatch.this, R.string.all_fields_required,
                            Toast.LENGTH_SHORT).show();
                }else{
                    mMatch.setDescription(description.getText().toString());

                    String dateTime = matchDate.getText().toString() + " " + matchTime.getText().toString();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());

                    try {
                        Date d = sdf.parse(dateTime);
                        if(d == null)
                            throw new IllegalStateException("Parsing of date has failed");
                        mMatch.setTimestamp(d.getTime());
                    } catch (ParseException e) {
                        throw new IllegalStateException("Parsing of date has failed");
                    }

                    mMatch.setPlaceName(placeText.getText().toString());
                    mMatch.setLongitude(longitude);
                    mMatch.setLatitude(latitude);
                    mMatch.setPlayersNumber(Integer.parseInt(missingPlayers.getText().toString()));
                    mMatch.setCreatorID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    if(Utils.dbStoreMatch(TAG, mMatch)){
                        Toast.makeText(ActivityCreateMatch.this, "Match successfully created", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(ActivityCreateMatch.this, ActivityMain.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(ActivityCreateMatch.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MATCH_A_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                longitude = data.getDoubleExtra(LONGITUDE, longitude);
                latitude = data.getDoubleExtra(LATITUDE, latitude);
                String nameOfThePlace = data.getStringExtra(PLACE_NAME);
                placeText.setText(nameOfThePlace);
            }
        }
    }

    /**
     * Save the state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(MATCH_DATE, matchDate.getText().toString());
        outState.putString(MATCH_HOUR, matchTime.getText().toString());
        outState.putString(PLACE_NAME, placeText.getText().toString());
        outState.putString(DESCRIPTION, description.getText().toString());
        outState.putDouble(LONGITUDE, longitude);
        outState.putDouble(LATITUDE, latitude);
        outState.putString(PLAYERS_NUMBER, missingPlayers.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDateSet(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, day);
        matchDate.setText(Utils.getDate(c.getTimeInMillis()));
    }

    @Override
    public void onTimeSet(int hour, int minute) {
        matchTime.setText(String.format("%02d:%02d", hour, minute));
    }
}