package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.es.findsoccerplayers.Models.Match;
import com.es.findsoccerplayers.MyPickers.DatePickerFragment;
import com.es.findsoccerplayers.MyPickers.NumberPickerDialog;
import com.es.findsoccerplayers.MyPickers.TimePickerFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class MatchActivity extends AppCompatActivity {

    private static TextView matchDate;
    private static TextView matchHour;
    private static TextView missingPlayer;
    private TextView placetext;
    private EditText description;
    private double longitude;
    private double latitude;
    private String nameOfTheplace;
    private FloatingActionButton matchFAB;


    private final String LOGITUDE = "longitude";
    private final String LATITUDE = "latitude";
    private final String PLACE_NAME = "place name";
    private final int MATCH_A_REQUEST_CODE = 42;
    private final String MATCH_DATE = "matchDate";
    private final String MATCH_HOUR = "matchHour";
    private final String PLAYER_NUMBER = "playerNumber";
    private final String DESCRIPTION = "description";

    private static Match mMatch = new Match();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);
        Toolbar toolbar = findViewById(R.id.match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Find the view
        matchDate = findViewById(R.id.date_text);
        matchHour = findViewById(R.id.time_text);
        missingPlayer = findViewById(R.id.player_number_set);
        placetext = findViewById(R.id.add_position);
        description = findViewById(R.id.description_field);
        matchFAB = findViewById(R.id.match_fab);


        if(savedInstanceState != null){
            matchDate.setText(savedInstanceState.getString(MATCH_DATE));
            matchHour.setText(savedInstanceState.getString(MATCH_HOUR));
            placetext.setText(savedInstanceState.getString(PLACE_NAME));
            description.setText(savedInstanceState.getString(DESCRIPTION));
            longitude = savedInstanceState.getDouble(LOGITUDE);
            latitude = savedInstanceState.getDouble(LATITUDE);
            missingPlayer.setText(savedInstanceState.getString(PLAYER_NUMBER));

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
        missingPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment playerNumb = new NumberPickerDialog();
                playerNumb.show(getSupportFragmentManager(), "player_picker");
            }
        });

        //Start MapsActivity for result
        placetext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Crea Activity Maps e ottieni il nome del posto oppure nome citta
                Intent intent = new Intent(MatchActivity.this, MapsActivity.class);
                startActivityForResult(intent, MATCH_A_REQUEST_CODE);
            }
        });

        matchFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(description.getText().toString().equals("")){
                    Toast.makeText(MatchActivity.this, "Insert a description", Toast.LENGTH_SHORT).show();
                }else{
                    mMatch.setDescription(description.getText().toString());
                    mMatch.setMatchData(matchDate.getText().toString()); //
                    mMatch.setMatchHour(matchHour.getText().toString());
                    mMatch.setPlaceName(placetext.getText().toString());
                    mMatch.setLongitude(longitude);
                    mMatch.setLatitude(latitude);
                    mMatch.setPlayerNumber(Integer.parseInt(missingPlayer.getText().toString()));

                    //TODO: Send all to the database and close this activity
                    Utils.showUnimplementedToast(MatchActivity.this);


                }

            }
        });






    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MATCH_A_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                longitude = data.getDoubleExtra(LOGITUDE, longitude);
                latitude = data.getDoubleExtra(LATITUDE, latitude);
                nameOfTheplace = data.getStringExtra(PLACE_NAME);
                placetext.setText(nameOfTheplace);
            }
        }
    }

    /**
     * hitting back will return to MainActivity
     */
    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        super.onBackPressed();
    }


    /**
     * Save the state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putString(MATCH_DATE, matchDate.getText().toString());
        outState.putString(MATCH_HOUR, matchHour.getText().toString());
        outState.putString(PLACE_NAME, placetext.getText().toString());
        outState.putString(DESCRIPTION, description.getText().toString());
        outState.putDouble(LOGITUDE, longitude);
        outState.putDouble(LATITUDE, latitude);
        outState.putString(PLAYER_NUMBER,missingPlayer.getText().toString());

        super.onSaveInstanceState(outState);
    }



}
