package com.es.findsoccerplayers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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

    static void storeUserInfoDB(final String TAG, FirebaseAuth fAuth, FirebaseFirestore fStore, String name, String surname, String date) {
        String userID = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userID);
        Map<String, Object> user = new HashMap<>();
        user.put("Nome", name);
        user.put("Cognome", surname);
        user.put("Data di nascita", date);
        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.w(TAG, "Memorizzato con successo nel db");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Erroe memorizzazione nel db: " + e.toString());
            }
        });
    }

}
