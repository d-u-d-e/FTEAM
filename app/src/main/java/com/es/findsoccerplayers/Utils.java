package com.es.findsoccerplayers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.es.findsoccerplayers.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

class Utils {

    static void showErrorToast(Context c, Exception ex){
        String error = c.getString(R.string.unknown_error);
        if(ex != null) error = ex.getMessage();
        Toast.makeText(c, error, Toast.LENGTH_LONG).show();
    }

    static void showSuccessLoginToast(Context c){
        Toast.makeText(c, R.string.login_success, Toast.LENGTH_SHORT).show();
    }

    static void showUnimplementedToast(Context c){
        Toast.makeText(c, R.string.unimplemented, Toast.LENGTH_SHORT).show();
    }

    static void showSuccessResetPswToast(Context c){
        Toast.makeText(c, R.string.reset_psw_success, Toast.LENGTH_SHORT).show();
    }

    static void dbStoreUser(final String tag, String name, String surname, String date) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth fAuth = FirebaseAuth.getInstance();
        User user = new User(fAuth.getCurrentUser().getUid(), name, surname, date);
        db.child("users").child(user.getId()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(tag, "Database successfully updated with new user info");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(tag, "Database update error: " + e.toString());
            }
        });
    }

    static void showCannotSendMessage(Context c){
        Toast.makeText(c, R.string.send_empty_message, Toast.LENGTH_SHORT).show();
    }
}
