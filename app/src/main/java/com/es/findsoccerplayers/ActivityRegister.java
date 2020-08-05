package com.es.findsoccerplayers;

import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.es.findsoccerplayers.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class ActivityRegister extends MyActivity implements DatePickerDialog.OnDateSetListener{

    private TextView selectedDate;

    String name, surname, email, emailConf,pass, passConf;

    boolean userSkipsDate = false;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_register);
        Toolbar toolbar = findViewById(R.id.reg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final EditText nameEd = findViewById(R.id.reg_name);
        final EditText surnameEd = findViewById(R.id.reg_surname);
        final EditText emailEd = findViewById(R.id.reg_email);
        final EditText emailConfEd = findViewById(R.id.reg_emailConf);
        final EditText passEd = findViewById(R.id.reg_pass);
        final EditText passConfEd = findViewById(R.id.reg_passConf);
        Button registerBtn = findViewById(R.id.reg_registerBtn);
        final ProgressBar progressBar = findViewById(R.id.reg_progressBar);
        selectedDate = findViewById(R.id.reg_selectDate);

        fAuth = FirebaseAuth.getInstance();

        selectedDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameEd.getText().toString();
                surname = surnameEd.getText().toString();
                email = emailEd.getText().toString();
                emailConf = emailConfEd.getText().toString();
                pass = passEd.getText().toString();
                passConf = passConfEd.getText().toString();

                //check if fields are completed without input errors

                String missingField = getString(R.string.field_missing);

                nameEd.setError(null);
                surnameEd.setError(null);
                emailConfEd.setError(null);
                emailEd.setError(null);
                passEd.setError(null);
                passConfEd.setError(null);

                if(TextUtils.isEmpty(name))
                    nameEd.setError(missingField);

                if(TextUtils.isEmpty(surname))
                    surnameEd.setError(missingField);

                if(TextUtils.isEmpty(email))
                    emailEd.setError(missingField);

                if(TextUtils.isEmpty(emailConf))
                    emailConfEd.setError(missingField);

                if(TextUtils.isEmpty(pass))
                    passEd.setError(missingField);

                if(TextUtils.isEmpty(passConf))
                    passConfEd.setError(missingField);

                if(pass.length() < 6)
                    passEd.setError(getString(R.string.pass_too_short));

                if(!email.equals(emailConf))
                    emailEd.setError(getString(R.string.email_not_equal));

                if(!pass.equals(passConf))
                    passEd.setError(getString(R.string.pass_not_equal));

                if (emailEd.getError() != null || passEd.getError() != null ||
                        emailConfEd.getError() != null || passConfEd.getError() != null )
                    return;

                //birth date is not mandatory; user is prompted once to insert it if it's missing
                if(!userSkipsDate && selectedDate.getText().toString().equals(getString(R.string.act_register_select_date))){
                    Utils.showToast(ActivityRegister.this, R.string.select_birth_date);
                    userSkipsDate = true;
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //everything is fine, create an account
                fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        progressBar.setVisibility(View.INVISIBLE);
                        if(task.isSuccessful()){
                            createFirebaseUser(name + " " + surname, selectedDate.getText().toString());
                            UserProfileChangeRequest.Builder b = new UserProfileChangeRequest.Builder();
                            b.setDisplayName(name + " " + surname);
                            AuthResult result = task.getResult();
                            assert result != null;
                            FirebaseUser user = task.getResult().getUser();
                            assert user != null;
                            user.updateProfile(b.build());
                        }else
                            Utils.showErrorToast(ActivityRegister.this, task.getException());
                    }
                });
            }
        });
    }

    /**
     * Shows date picker dialog after user hits select date text view
     */
    private void showDatePickerDialog(){
        Calendar c = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, this,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * Sets the date after the user picked it
     */
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dayOfMonth);
        selectedDate.setTextColor(getResources().getColor(R.color.black));
        selectedDate.setText(Utils.getDate(c.getTimeInMillis()));
    }

    private void createFirebaseUser(String username, String date){

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        User user = new User(FirebaseAuth.getInstance().getCurrentUser().getUid(), username, date);

        db.child("users").child(user.getId()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null){
                    Utils.showErrorToast(ActivityRegister.this, databaseError.getMessage());
                }
                else{
                    Utils.showToast(ActivityRegister.this, R.string.register_success);
                    Intent i = new Intent(ActivityRegister.this, ActivityLogin.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                    finish();
                }
            }
        });
    }
}