/*
ACCOUNT ACTIVITY

Questa classe serve a visualizzare i dati dell'account attualmente loggato
 */


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

public class AccountActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser acct;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView id;
    private TextView email;
    private TextView nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Button btnLogOut = findViewById(R.id.btnLogOut);
        id = (TextView) findViewById(R.id.id);
        email = (TextView) findViewById(R.id.email);
        nome = (TextView) findViewById(R.id.nome);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
        acct = FirebaseAuth.getInstance().getCurrentUser();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Creo l'oggetto gso che tramite il parametro DEFAULT_SIGN_IN mi permette di creare un oggetto
        //mGoogleSignInClient nel quale vengono memorizzate le informazioni di base dell'utente google
        //loggato
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Se viene premuto il pulsante log out, viene chiamata la funzione singOut() e si
        //torna alla LoginActivity
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
                startActivity(new Intent(AccountActivity.this, LoginActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Visualizzo a schermo le informazioni dell'account
        id.setText("ID: "+acct.getUid());
        email.setText("email: "+acct.getEmail());
        nome.setText("nome: "+acct.getDisplayName());
    }

    private void signOut() {
        //Faccio il log out dall'account firebase attualmente collegato
        mAuth.signOut();

        //Faccio il log out dall'account google attualmente collegato.
        //Questo fa in modo che, quando voglio loggarmi nuovamente con un
        //account google, non uso l'account precendete ma mi si apre di nuovo
        //la lista degli account google
        mGoogleSignInClient.signOut();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Dall'activity account, se viene premuto back, torno alla MainActivity
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}