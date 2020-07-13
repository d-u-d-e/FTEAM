package com.es.findsoccerplayers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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


public class ActivityLogin extends AppCompatActivity{

    private static final int RC_GOOGLE_SIGN_IN = 100;
    private static final String TAG = "ActivityLogin";
    private FirebaseAuth fAuth;
    private long backPressedTime;
    private Toast backToast;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w(TAG, "LoginActivity created");
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
                            Utils.showSuccessLoginToast(ActivityLogin.this);
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
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
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
                                    checkIfUserExists(); //asynchronous
                                } else
                                    Utils.showErrorToast(ActivityLogin.this, task.getException());
                            }
                        });
            }
        } catch (ApiException e) {
            //The exception with code 12501 is a feedback from google that the sign in was cancelled by the user,
            //so it isn't an exception that requires to be handled or shown
            if(e.getStatusCode() == 12501){
                Log.w(TAG, "Exception: " + e.toString());
            }else {
                Log.w(TAG, "Exception: " + e.toString());
                Utils.showErrorToast(this, CommonStatusCodes.getStatusCodeString(e.getStatusCode()));
            }
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * This calls createUser() if the user does not exists anywhere in the database. Then login proceeds.
     */

    private void checkIfUserExists(){
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        DatabaseReference ref = db.child("users").child(user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                    createUser();
                Utils.showSuccessLoginToast(ActivityLogin.this);
                startActivity(new Intent(ActivityLogin.this, ActivityMain.class));
                finish();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Utils.showErrorToast(ActivityLogin.this, getString(R.string.unexpected_error));
            }
        });
    }

    /**
     * Creates a new user, saving his information in the database
     */

    private void createUser(){
        String fullName = fAuth.getCurrentUser().getDisplayName();
        if(fullName == null || fullName.isEmpty()){
            Utils.showErrorToast(ActivityLogin.this, getString(R.string.missing_google_username));
            Utils.dbStoreNewUser(TAG, "user", "", "");
        }
        else{
            String[] names = fullName.split(" ");
            //try to obtain both first and second names
            String first;
            String second = "";
            if(names.length == 2){
                first = names[0];
                second = names[1];
            }
            else
                first = names[0];
            Utils.dbStoreNewUser(TAG, first, second, "");
        }
    }
}