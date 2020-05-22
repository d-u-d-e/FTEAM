package com.es.findsoccerplayers;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 100;
    private static final String TAG = "LoginActivity";
    private FirebaseAuth auth;
    private long backPressedTime;
    private Toast backToast;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(TAG, "LoginActivity creata");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.log_toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        FirebaseUser acct = auth.getCurrentUser();
        //if someone has already logged, start MainActivity
        if(acct !=null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
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
        //start MainActivity, otherwise show the error as a toast

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

                auth.signInWithEmailAndPassword(email, psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()){
                            Utils.showSuccessLoginToast(LoginActivity.this);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }else{
                            Utils.showErrorToast(LoginActivity.this, task.getException());
                        }

                    }
                });
            }
        });

        //start RegisterActivity if btnRegister is clicked
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
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

        //start RegisterActivity if btnRegister is clicked
        forgottenPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPassword.class));
                //do not finish this activity since it will be shown again after registration
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check for result of google sign in
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                auth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                progressBar.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Utils.showSuccessLoginToast(LoginActivity.this);
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                    finish();
                                } else
                                    Utils.showErrorToast(LoginActivity.this, task.getException());
                            }
                        });
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                //The exception with code 12501 is a feedback from google that the sign in was cancelled by the user,
                //so it isn't an exception that requires to be handled or shown
                if(e.toString().equals("com.google.android.gms.common.api.ApiException: 12501: ")){

                }else {
                    Log.w(TAG, "Cathccata: " + e.toString());
                    Utils.showErrorToast(this, e);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * requested 2 taps at most 2 seconds apart in order to exit
     */
    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed(); //the default finish the current activity
        }else{
            backToast = Toast.makeText(this, R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}