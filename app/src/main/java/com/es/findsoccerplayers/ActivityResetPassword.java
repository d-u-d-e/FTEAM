package com.es.findsoccerplayers;

import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ActivityResetPassword extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_reset_password);

        Toolbar toolbar = findViewById(R.id.rst_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        Button reset = findViewById(R.id.rst_btn);
        final EditText email = findViewById(R.id.rst_email);

         /* When the reset button is pressed, an email is sent to the user to reset his password.
          * If the email is not found, an error is shown */

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = email.getText().toString();

                if(emailAddress.isEmpty()){
                    Utils.showErrorToast(ActivityResetPassword.this, getString(R.string.field_missing));
                    return;
                }

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Utils.showToast(ActivityResetPassword.this, R.string.reset_psw_success);
                                    Intent i = new Intent(ActivityResetPassword.this, ActivityLogin.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivity(i);
                                    finish();
                                }
                                else
                                    Utils.showErrorToast(ActivityResetPassword.this, task.getException());
                            }
                        });
            }
        });
    }
}