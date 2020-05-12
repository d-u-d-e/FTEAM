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

    private static final int RC_SIGN_IN = 100;
    private SignInButton btnGoogle;
    private Button btnRegister, btnLogin;
    private EditText logPsw;
    private EditText logEmail;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser acct;
    private long backPressedTime;
    private Toast backToast;
    private ProgressBar prgBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.w("LoginAct","Act Startata");
        btnGoogle=findViewById(R.id.btnGoogle);
        btnGoogle.setSize(SignInButton.SIZE_STANDARD);
        logEmail=findViewById(R.id.logEmail);
        acct = FirebaseAuth.getInstance().getCurrentUser();
        logPsw=findViewById(R.id.logPsw);
        btnRegister=findViewById(R.id.btnRegister);
        prgBar=findViewById(R.id.progBar);
        mAuth=FirebaseAuth.getInstance();
        btnLogin=findViewById(R.id.btnLogin);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = logEmail.getText().toString();
                String psw = logPsw.getText().toString();

                if(TextUtils.isEmpty(email)){
                    logEmail.setError("Campo obbligatorio");
                    return;
                }
                if(TextUtils.isEmpty(psw)){
                    logPsw.setError("Campo obbligatorio");
                    return;
                }

                prgBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email,psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Loggato con successo", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }else{
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            prgBar.setVisibility(View.INVISIBLE);
                        }

                    }
                });
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                Toast.makeText(LoginActivity.this, "Loggato con successo", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                prgBar.setVisibility(View.INVISIBLE);
                // ...
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        prgBar.setVisibility(View.INVISIBLE);
        Log.w("LoginAct","Act Creata");
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            prgBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, (CharSequence) task.getException(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    private void signIn() {
        prgBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (acct==null){
            if(backPressedTime + 2000 > System.currentTimeMillis()){
                super.onBackPressed();
                backToast.cancel();
                finishAffinity();
            }else{
                backToast=Toast.makeText(getApplicationContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime=System.currentTimeMillis();
        }else{
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }
}