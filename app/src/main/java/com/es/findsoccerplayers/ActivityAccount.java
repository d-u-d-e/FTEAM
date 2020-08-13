package com.es.findsoccerplayers;

import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Shows the account information for the current user.
 * TODO: Add the possibility to change the nickname, add an image profile, see the number of created games etc.
 * */

public class ActivityAccount extends MyActivity{

    FirebaseUser acct = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_account);
        Toolbar toolbar = findViewById(R.id.acc_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView nameTextView = findViewById(R.id.acc_username);
        nameTextView.setText(String.format(getString(R.string.act_account_username), acct.getDisplayName()));

        TextView id = findViewById(R.id.acc_userId);
        TextView email = findViewById(R.id.acc_email);

        id.setText(String.format(getString(R.string.act_account_id), acct.getUid()));
        email.setText(String.format(getString(R.string.act_account_email), acct.getEmail()));
    }
}