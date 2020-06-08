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

    public static String getPreviewDescription(String description){
        if(description.length()<=20)
            return description;
        else{
            return (description.substring(0,21)+"...");
        }
    }

    public static String extractDay(String date){
        if(date.substring(1,2).equals("/"))
            return date.substring(0,1);
        else
            return date.substring(0,2);
    }

    public static String extractMonth(String date) {
        String month;
        date = date.replaceAll("\\s", "");
        if(date.substring(2,3).equals("/")){
            switch (Integer.parseInt(date.substring(3, 5))) {
                case 1:
                    month = "JAN";
                    break;
                case 2:
                    month = "FEB";
                    break;
                case 3:
                    month = "MAR";
                    break;
                case 4:
                    month = "APR";
                    break;
                case 5:
                    month = "MAY";
                    break;
                case 6:
                    month = "JUN";
                    break;
                case 7:
                    month = "JUL";
                    break;
                case 8:
                    month = "AUG";
                    break;
                case 9:
                    month = "SET";
                    break;
                case 10:
                    month = "OCT";
                    break;
                case 11:
                    month = "NOV";
                    break;
                case 12:
                    month = "DIC";
                    break;
                default:
                    month = "ERR";
                    break;
            }
        }else {
            switch (Integer.parseInt(date.substring(2, 3))) {
                case 1:
                    month = "JAN";
                    break;
                case 2:
                    month = "FEB";
                    break;
                case 3:
                    month = "MAR";
                    break;
                case 4:
                    month = "APR";
                    break;
                case 5:
                    month = "MAY";
                    break;
                case 6:
                    month = "JUN";
                    break;
                case 7:
                    month = "JUL";
                    break;
                case 8:
                    month = "AUG";
                    break;
                case 9:
                    month = "SET";
                    break;
                case 10:
                    month = "OCT";
                    break;
                case 11:
                    month = "NOV";
                    break;
                case 12:
                    month = "DIC";
                    break;
                default:
                    month = "ERR";
                    break;
            }
        }
        return month;
    }
}
