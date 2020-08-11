package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.es.findsoccerplayers.fragments.FragmentInfoMatch;
import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ActivityLogin extends MyActivity{

    private static final int RC_GOOGLE_SIGN_IN = 100;
    private FirebaseAuth fAuth;
    private long backPressedTime;
    private Toast backToast;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras != null){
            final Intent intent = new Intent(ActivityLogin.this, ActivitySelectMatch.class);
            intent.putExtra("type", "msg");
            intent.putExtra("match", i.getStringExtra("match"));
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.act_login);
        Toolbar toolbar = findViewById(R.id.log_toolbar);
        setSupportActionBar(toolbar);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser autUser = fAuth.getCurrentUser();
        //if someone has already logged, start ActivityMain (auto login)
        if(autUser != null) {
            startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
            finish();
            return;
        }

        //set the text of sign in google button to common long
        SignInButton btnGoogle = findViewById(R.id.log_googleSignInBtn);
        btnGoogle.setSize(SignInButton.SIZE_STANDARD);
        ((TextView)btnGoogle.getChildAt(0)).setText(R.string.common_signin_button_text_long);

        final EditText logEmail = findViewById(R.id.log_email);
        final EditText logPsw = findViewById(R.id.log_pass);
        progressBar = findViewById(R.id.log_progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        Button btnRegister = findViewById(R.id.log_registerBtn);
        Button btnLogin = findViewById(R.id.log_loginBtn);
        final TextView forgottenPsw = findViewById(R.id.log_forgottenPsw);

        //options required to log in with Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        final GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

        //If btnLogin is pressed, login with standard firebase credentials. If login is successful,
        //start ActivityMain, otherwise show the error as a toast

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = logEmail.getText().toString();
                String psw = logPsw.getText().toString();
                if(TextUtils.isEmpty(email)){
                    logEmail.setError(getString(R.string.field_missing));
                }
                if(TextUtils.isEmpty(psw)){
                    logPsw.setError(getString(R.string.field_missing));
                }
                if ((logEmail.getError() != null) || (logPsw.getError() != null)) { //input error, so return
                    return;
                }

                progressBar.setVisibility(View.VISIBLE); //give some feeling of responsiveness to the user

                fAuth.signInWithEmailAndPassword(email, psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()){
                            Utils.showToast(ActivityLogin.this, R.string.login_success);
                            startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
                            finish();
                        }else{
                            Utils.showErrorToast(ActivityLogin.this, task.getException());
                        }

                    }
                });
            }
        });

        //start ActivityRegister if btnRegister is clicked
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivityRegister.class));
                //do not finish this activity since it will be shown again after registration
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
            }
        });

        //start ActivityResetPassword if forgottenPsw is clicked
        forgottenPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityLogin.this, ActivityResetPassword.class));
                //do not finish this activity since it will be shown again after registration
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check for result of google sign in
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            performGoogleLogin(data);
        }
    }

    /**
     * Requested 2 taps at most 2 seconds apart in order to exit
     */
    @Override
    public void onBackPressed() {
        if(backToast != null) backToast.cancel();
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed(); //the default finishes the current activity
        }else{
            backToast = Toast.makeText(this, R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    void performGoogleLogin(Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            final GoogleSignInAccount account = task.getResult(ApiException.class);
            if(account == null){
                Utils.showErrorToast(this, getString(R.string.unknown_error));
            }
            else{
                final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                fAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                progressBar.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    //check if we need to store the user info, maybe it's the first time that
                                    //the user signs in
                                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                                    FirebaseAuth fAuth = FirebaseAuth.getInstance();
                                    FirebaseUser user = fAuth.getCurrentUser();
                                    DatabaseReference ref = db.child("users").child(user.getUid());
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.exists())
                                                createGoogleUser();
                                            else{
                                                Utils.showToast(ActivityLogin.this, R.string.login_success);
                                                startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
                                                finish();
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                                    Utils.showErrorToast(ActivityLogin.this, getString(R.string.unexpected_error));
                                        }
                                    });
                                } else
                                    Utils.showErrorToast(ActivityLogin.this, task.getException());
                            }
                        });
            }
        } catch (ApiException e) {
            //The exception with code 12501 is a feedback from google that the sign in was cancelled by the user,
            //so it isn't an exception that requires to be handled or shown
            if(e.getStatusCode() != 12501){
                Utils.showErrorToast(this, CommonStatusCodes.getStatusCodeString(e.getStatusCode()));
                //TODO maybe show an even better description
                // for network errors is just a simple and ugly string "NETWORK_ERROR"
            }
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Creates a new user, saving his information in the database
     */

    private void createGoogleUser(){
        String username = fAuth.getCurrentUser().getDisplayName();
        if(username == null || username.isEmpty()){
            Utils.showErrorToast(ActivityLogin.this, getString(R.string.missing_google_username));
            username = "user";
        }

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid(), username, "");

        db.child("users").child(user.getId()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    Utils.showErrorToast(ActivityLogin.this, databaseError.getMessage());
                }
                else{
                    Utils.showToast(ActivityLogin.this, R.string.login_success);
                    startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
                    finish();
                }
            }
        });
    }
}