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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ActivityCreateMatch extends AppCompatActivity {

    private TextView matchDate;
    private TextView matchHour;
    private TextView missingPlayers;
    private TextView placeText;
    private EditText description;
    private double longitude;
    private double latitude;

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
        matchHour = findViewById(R.id.cr_match_timeText);
        missingPlayers = findViewById(R.id.cr_match_playersNumber);
        placeText = findViewById(R.id.cr_match_addPosition);
        description = findViewById(R.id.cr_match_descriptionField);
        FloatingActionButton matchFab = findViewById(R.id.cr_match_fab);

        if(savedInstanceState != null){
            matchDate.setText(savedInstanceState.getString(MATCH_DATE));
            matchHour.setText(savedInstanceState.getString(MATCH_HOUR));
            placeText.setText(savedInstanceState.getString(PLACE_NAME));
            description.setText(savedInstanceState.getString(DESCRIPTION));
            longitude = savedInstanceState.getDouble(LONGITUDE);
            latitude = savedInstanceState.getDouble(LATITUDE);
            missingPlayers.setText(savedInstanceState.getString(PLAYERS_NUMBER));

        } else {
            //Current value for default
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            matchDate.setText(day + "/" + month + "/" + year);
            matchHour.setText(hour + ":" + minute);
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
        matchHour.setOnClickListener(new View.OnClickListener() {
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
                if(description.getText().toString().equals("")){
                    Toast.makeText(ActivityCreateMatch.this, "Insert a description", Toast.LENGTH_SHORT).show();
                }else{
                    mMatch.setDescription(description.getText().toString());
                    mMatch.setMatchData(matchDate.getText().toString()); //
                    mMatch.setMatchHour(matchHour.getText().toString());
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
        outState.putString(MATCH_HOUR, matchHour.getText().toString());
        outState.putString(PLACE_NAME, placeText.getText().toString());
        outState.putString(DESCRIPTION, description.getText().toString());
        outState.putDouble(LONGITUDE, longitude);
        outState.putDouble(LATITUDE, latitude);
        outState.putString(PLAYERS_NUMBER, missingPlayers.getText().toString());

        super.onSaveInstanceState(outState);
    }
}