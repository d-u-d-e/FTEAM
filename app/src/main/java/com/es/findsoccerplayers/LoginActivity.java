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
    private static final String TAG = "LoginActivity";
    private EditText logPsw;
    private EditText logEmail;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser acct;
    private long backPressedTime;
    private Toast backToast;
    private ProgressBar prgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.w(TAG,"Activity Creata");
        mAuth = FirebaseAuth.getInstance();
        acct = mAuth.getCurrentUser();

        //Se c'è gia un utente loggato, lancio MainActivity
        if(acct!=null) {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        SignInButton btnGoogle = findViewById(R.id.googleSignInBtn);
        btnGoogle.setSize(SignInButton.SIZE_STANDARD);
        logEmail = findViewById(R.id.logEmail);
        logPsw = findViewById(R.id.logPsw);
        Button btnRegister = findViewById(R.id.btnRegister);
        prgBar = findViewById(R.id.progBar);
        Button btnLogin = findViewById(R.id.btnLogin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prgBar.setVisibility(View.INVISIBLE);

        //Creo l'oggetto gso che tramite il parametro DEFAULT_SIGN_IN mi permette di creare un oggetto
        //mGoogleSignInClient nel quale vengono memorizzate le informazioni di base dell'utente google
        //loggato
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Se viene premuto il pulsante btnLogin, controllo se su firebase è presente un account
        //con quelle credenziali: se è presente mi loggo e vado alla MainActivity, altrimenti
        //restituisco un messaggio di errore
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = logEmail.getText().toString();
                String psw = logPsw.getText().toString();
                if(TextUtils.isEmpty(email)){
                    logEmail.setError("Campo obbligatorio");
                }
                if(TextUtils.isEmpty(psw)){
                    logPsw.setError("Campo obbligatorio");
                }
                if ((logEmail.getError()!= null) || (logPsw.getError()!= null)) {
                    return;
                }

                prgBar.setVisibility(View.VISIBLE);

                mAuth.signInWithEmailAndPassword(email,psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Loggato con successo", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }else{
                            Toast.makeText(LoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            prgBar.setVisibility(View.INVISIBLE);
                        }

                    }
                });
            }
        });

        //Se viene premuto il pulsante btnRegister, viene lanciata la RegisterActivity
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        //Se viene premuto il pulsante btnGoogle, viene lanciata la funzione signIn() che mi permette di loggarmi
        //con Google
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            //Effettuo il login
            @Override
            public void onClick(View v) {
                prgBar.setVisibility(View.VISIBLE);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                //Chiama la funzione onActivityResult
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w(TAG, "Activity Startata");
    }


    /**
     * Funzione che gestisce il tentativo di login
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Se il requestCode è lo stesso di quello passato dalla funzione signIn()
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                    finish();
                                } else {
                                    prgBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(LoginActivity.this, (CharSequence) task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                Toast.makeText(LoginActivity.this, "Loggato con successo", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                prgBar.setVisibility(View.INVISIBLE);
            }
        }
    }


    /**
     * Viene richiesto un doppio tap a distanza di 2 secondi sul tasto back per uscire dall'app
     */
    @Override
    public void onBackPressed() {
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            backToast.cancel();
            finishAffinity();
        }else{
            backToast = Toast.makeText(getApplicationContext(), R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime=System.currentTimeMillis();
    }
}