package com.es.findsoccerplayers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.es.findsoccerplayers.models.Match;
import com.es.findsoccerplayers.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Month;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Utils {

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

    static boolean dbStoreMatch(final String tag, Match m){
        //TODO gestire caso in cui il match non viene correttamente salvato --> return false
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().
                getReference("users/" + user.getUid() + "/matches");
        DatabaseReference newRef = ref.push();
        String key = newRef.getKey();
        m.setMatchID(key);
        Map<String, Boolean> map = new HashMap<>();
        //TODO maybe this is not always the default
        map.put("member", true);
        map.put("creator", true);
        newRef.setValue(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.w(tag, "Database successfully updated with new match info for the user");
            };
        });

        ref = FirebaseDatabase.getInstance().getReference();
        ref.child("matches").child(key).setValue(m).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(tag, "Database successfully updated with new general match info");
            }
        });
        return true;
    }

    public static void showCannotSendMessage(Context c){
        Toast.makeText(c, R.string.send_empty_message, Toast.LENGTH_SHORT).show();
    }

    public static String getPreviewDescription(String description) {
        if (description.length() <= 20)
            return description;
        else {
            return (description.substring(0, 21) + "...");
        }
    }
}
