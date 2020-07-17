package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.es.findsoccerplayers.models.Match;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ActivityInfoMatch extends AppCompatActivity {

    private static final String TAG = "ActivityInfoMatch";
    private String relatedMatch;
    private TextView field, date, time, money, missingPlayers, description;
    private Match m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_info_match);

        Toolbar toolbar = findViewById(R.id.info_match_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        field = findViewById(R.id.info_match_fieldTV);
        date = findViewById(R.id.info_match_dayTV);
        time = findViewById(R.id.info_match_timeTV);
        money= findViewById(R.id.info_match_moneyTV);
        missingPlayers = findViewById(R.id.info_match_missingPlayersTV);
        description = findViewById(R.id.info_match_descriptionTV);
        Button dropBtn = findViewById(R.id.info_match_dropBtn);

        dropBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showUnimplementedToast(getApplicationContext());
            }
        });

        Intent intent = getIntent();
        relatedMatch = intent.getStringExtra("match");
        Log.w(TAG, relatedMatch);

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference("matches").child(relatedMatch);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m = dataSnapshot.getValue(Match.class);
                assert m != null;
                field.setText(m.getPlaceName());

                long timestamp = m.getTimestamp();

                date.setText(Utils.getDate(timestamp));
                time.setText(Utils.getTime(timestamp));
                money.setText("----");                                              //TODO
                missingPlayers.setText(Integer.toString(m.getPlayersNumber()));
                description.setText(m.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_match_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.acc_edit) {
            Intent i = new Intent(getApplicationContext(), ActivityCreateMatch.class);
            i.putExtra("match", relatedMatch);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}