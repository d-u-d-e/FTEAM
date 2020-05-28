package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * shows the account info for the current user
 * */
public class ActivityAccount extends AppCompatActivity {

    private FirebaseUser acct;
    private TextView id;
    private TextView email;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.acc_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button btnLogOut = findViewById(R.id.acc_btnLogOut);
        name = (TextView) findViewById(R.id.acc_username);
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        id = (TextView) findViewById(R.id.acc_userId);
        email = (TextView) findViewById(R.id.acc_email);
        acct = FirebaseAuth.getInstance().getCurrentUser();

        //options required to log in with Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        //if log out button is clicked go back to the login activity
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //log out the current user
                auth.signOut();

                //google log out: this does nothing if the user did not log in with google
                //however in the other case, it clears the previous selected account, so next time the
                //user is asked to select a new account
                googleSignInClient.signOut();
                //back to login activity
                finishAffinity();
                Intent i = new Intent(ActivityAccount.this, ActivityLogin.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //shows user info
        id.setText(String.format(getString(R.string.act_account_id), acct.getUid()));
        email.setText(String.format(getString(R.string.act_account_email), acct.getEmail()));
        name.setText(String.format(getString(R.string.act_account_username), acct.getDisplayName()));
    }

    /**
     * hitting back will return to MainActivity
     */
    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, ActivityMain.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);
        super.onBackPressed();
    }
}