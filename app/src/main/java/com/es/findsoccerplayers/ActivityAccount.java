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
 * Shows the account information for the current user
 * */

public class ActivityAccount extends AppCompatActivity {

    private FirebaseUser acct;
    private TextView id;
    private TextView email;
    private TextView name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_account);
        Toolbar toolbar = findViewById(R.id.acc_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = (TextView) findViewById(R.id.acc_username);
        final FirebaseAuth auth = FirebaseAuth.getInstance();

        id = (TextView) findViewById(R.id.acc_userId);
        email = (TextView) findViewById(R.id.acc_email);
        acct = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //shows user info
        id.setText(String.format(getString(R.string.act_account_id), acct.getUid()));
        email.setText(String.format(getString(R.string.act_account_email), acct.getEmail()));
        name.setText(String.format(getString(R.string.act_account_username), acct.getDisplayName()));
    }
}