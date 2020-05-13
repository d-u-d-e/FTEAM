package com.es.findsoccerplayers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.w(TAG,"Activity Creata");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser acct = mAuth.getCurrentUser();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Visualizzo a schermo la barra con il menu
     *
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.layout_menu, menu);
        return true;
    }

    /**
     * Gestisce i listener del menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            //Se viene premuta l'icona account, lancio AccountActivity
            case R.id.account:
                startActivity(new Intent(getApplicationContext(),AccountActivity.class));
                return true;
            //Se viene premuto il tasto "Impostazioni" nel menu, viene lanciata ....
            case R.id.settings:
                Toast.makeText(getApplicationContext(), R.string.todoCAPS, Toast.LENGTH_SHORT).show();
                //Todo: creare entity delle impostazioni
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Viene richiesto un doppio tap a distanza di 2 secondi sul tasto back per uscire dall'app
     */
    @Override
    public void onBackPressed() {
        //backPressedTime è settato a 0 per default
        if(backPressedTime + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
            backToast.cancel();
            finishAffinity();
        }else{
            backToast=Toast.makeText(getApplicationContext(), R.string.double_back, Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime=System.currentTimeMillis();
    }
}
