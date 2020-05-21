package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.es.findsoccerplayers.Models.Match;
import com.es.findsoccerplayers.MyPickers.DatePickerFragment;
import com.es.findsoccerplayers.MyPickers.NumberPickerDialog;
import com.es.findsoccerplayers.MyPickers.TimePickerFragment;

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
    private Button confirmButton;

    public final String LOGITUDE = "longitude";
    public final String LATITUDE = "latitude";
    public final String PLACE_NAME = "place name";
    public final int MATCH_A_REQUEST_CODE = 42;

    private static Match mMatch = new Match();
    private static String data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        Intent intent = getIntent();
        //If the MainActivity send us the last know location of the user
        // send it to the MapsActivity for starting point
        if(intent.hasExtra(LATITUDE)){
            longitude = intent.getDoubleExtra(LOGITUDE, 0 );
            latitude = intent.getDoubleExtra(LATITUDE, 0);
        }

        //Find the view
        matchDate = findViewById(R.id.date_text);
        matchHour = findViewById(R.id.time_text);
        missingPlayer = findViewById(R.id.player_number_set);
        placetext = findViewById(R.id.add_position);
        description = findViewById(R.id.description_field);
        confirmButton = findViewById(R.id.create_button);

        //Current value for default
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        matchDate.setText(day + "/" + month + "/" + year);
        matchHour.setText(hour + ":" + minute);

        //Set the date number with dialog
        matchDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datepicker = new DatePickerFragment();
                datepicker.show(getSupportFragmentManager(), "date_picker");
            }
        });

        //Set the hour number with dialog
        matchHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timepicker = new TimePickerFragment();
                timepicker.show(getSupportFragmentManager(), "hour_picker");
            }
        });

        //Set the missing player number with dialog
        missingPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment playernumb = new NumberPickerDialog();
                playernumb.show(getSupportFragmentManager(), "player_picker");
            }
        });

        //Start MapsActivity for result
        placetext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Crea Activity Maps e ottieni il nome del posto oppure nome citta
                Intent intent = new Intent(MatchActivity.this, MapsActivity.class);
                intent.putExtra(LOGITUDE, longitude);
                intent.putExtra(LATITUDE, latitude);
                startActivityForResult(intent, MATCH_A_REQUEST_CODE);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(description.getText().toString().equals("")){
                    Toast.makeText(MatchActivity.this, "Insert a description", Toast.LENGTH_SHORT).show();
                }else{
                    mMatch.setDescription(description.getText().toString());

                }
                //TODO: Send all to the database and close this activity
                Utils.showUnimplementedToast(MatchActivity.this);

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

                mMatch.setLatitude(latitude);
                mMatch.setLongitude(longitude);
                mMatch.setPlaceName(nameOfTheplace);
            }
        }
    }

    //set the choosen date
    public static void setTheDate(int day, int month, int year){
        matchDate.setText(String.format("%02d / %02d / %04d", day, month, year));
        mMatch.setMatchData(String.format("%02d / %02d / %04d", day, month, year));
    }

    //set the choosen hour
    public static void setTheHour(int hour, int minute){
        matchHour.setText(String.format("%02d : %02d", hour, minute));
        mMatch.setMatchHour(String.format("%02d : %02d", hour, minute));
    }

    //set the missing number player selected
    public static void setThePlayerNumber(int player){
        missingPlayer.setText("Missing players: " + player);
        mMatch.setPlayerNumber(player);
    }


    /**
     * hitting back will return to Login Activity
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        super.onBackPressed();
    }



}
