package com.example.singingoogletest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText edNome, edCognome, edEmail, edConfEmail, edPsw, edConfPsw;
    private TextView txSelect;
    private ProgressBar prgBar;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private Toolbar toolbar;
    private String nome, cognome, email, confEmail, psw, confPsw, birthDate, uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        edNome=findViewById(R.id.regNome);
        edCognome=findViewById(R.id.regCognome);
        edEmail=findViewById(R.id.regEmail);
        edConfEmail=findViewById(R.id.regConfEmail);
        edPsw=findViewById(R.id.regPsw);
        edConfPsw=findViewById(R.id.regConfPsw);
        btnRegister=findViewById(R.id.btnRegister);
        prgBar=findViewById(R.id.progBar);
        txSelect=findViewById(R.id.selectDate);
        mAuth=FirebaseAuth.getInstance();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        database=FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        database.setFirestoreSettings(settings);


        txSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nome = edNome.getText().toString();
                cognome = edCognome.getText().toString();
                email = edEmail.getText().toString();
                confEmail = edConfEmail.getText().toString();
                psw = edPsw.getText().toString();
                confPsw = edConfPsw.getText().toString();

                if(txSelect.getText().toString().equals("Seleziona")){
                    Toast.makeText(RegisterActivity.this, "Seleziana la data di nascita", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    edEmail.setError("Campo obbligatorio");
                    return;
                }
                if(TextUtils.isEmpty(confEmail)){
                    edConfEmail.setError("Campo obbligatorio");
                    return;
                }
                if(!email.equals(confEmail)){
                    edEmail.setError("Le email non coincidono");
                    return;
                }else{
                    edEmail.setError(null);
                }
                if(TextUtils.isEmpty(psw)){
                    edPsw.setError("Campo obbligatorio");
                    return;
                }
                if(psw.length() < 6) {
                    edPsw.setError("La password deve avere minimo 6 caratteri");
                    return;
                }
                if(TextUtils.isEmpty(confPsw)){
                    edConfPsw.setError("Campo obbligatorio");
                    return;
                }
                if(!psw.equals(confPsw)){
                    edPsw.setError("Le password non coincidono");
                    return;
                }else{
                    edPsw.setError(null);
                }

                prgBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email,psw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                            uid=mAuth.getCurrentUser().getUid();
                            //DocumentReference documentReference = database.collection("users").document(uid);
                            Map<String,Object> user = new HashMap<>();
                         /* user.put("Nome",nome);
                            user.put("Cognome",cognome);
                            user.put("Email",email);
                            user.put("Password",psw);
                            user.put("Data di nascita", birthDate);*/
                            Log.w("RegisterAct","son dentro!");
                            user.put("test", "test");

                            database.collection("users").document(uid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.w("RegisterAct","Dati memorizzati con successo!");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("RegisterAct", "errorrrrrrrrrrrrrrrrrrrrrrrrrrr: "+e.toString());
                                }
                            });

                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        }else{
                            prgBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    private void showDatePickerDialog(){
        DatePickerDialog datePickerDialog =  new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        month+=1;
        birthDate = dayOfMonth+"/"+month+"/"+year;
        txSelect.setTextColor(getResources().getColor(R.color.nero));
        txSelect.setText(birthDate);
    }
}