package com.es.findsoccerplayers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utils {

    private static WeakReference<Toast> toast = null;

    static void cancelToast(){

        if(toast != null && toast.get() != null) toast.get().cancel();
    }

    static void showErrorToast(Context c, Exception ex){
        String error = c.getString(R.string.unknown_error);
        if(ex != null) error = ex.getMessage();
        showToast(c, error, Toast.LENGTH_LONG);
    }

    public static void showErrorToast(Context c, String s){
        showToast(c, s, Toast.LENGTH_LONG);
    }

    private static void showToast(Context c, String text, int duration){

        if(c == null) return;

        if(toast != null && toast.get() != null)
            toast.get().cancel();

        Toast t = Toast.makeText(c, text, duration);
        toast = new WeakReference<>(t);
        t.show();
    }

    private static void showToast(Context c, int resource, int duration){

        if(c == null) return;

        if(toast != null && toast.get() != null)
            toast.get().cancel();

        Toast t = Toast.makeText(c, resource, duration);
        toast = new WeakReference<>(t);
        t.show();
    }

    public static void showToast(Context c, String text){
        showToast(c, text, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context c, int resource){
        showToast(c, resource, Toast.LENGTH_SHORT);
    }

    public static void showOfflineWriteToast(Context c){
        showToast(c, R.string.offlineWrite);
    }

    static void showOfflineReadToast(Context c){
        showToast(c, R.string.offlineRead);
    }

    public static String getPreviewDescription(String description) {
        if (description.length() <= 20)
            return description;
        else
            return (description.substring(0, 21) + "...");
    }

    public static String getDate(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(c.getTime());
    }

    public static String getTime(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(c.getTime());
    }

    public static String getDay(long timestamp){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return String.valueOf(c.get(Calendar.DAY_OF_MONTH));
    }

    public static String getMonth(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
    }

    public static boolean isOffline(Context context)
    {
        if(context == null) return true;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnectedOrConnecting();


    }

    /**
     * Users can logout and login any time. If the user logout, he unscribe from all match topics.
     * If he come back again, will subscribe again to all his matches, created by him, or booked, so he can get notification for new messages in chat
     *
     */
    static void subscribe(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref =
                db.getReference().child("users").child(user.getUid()).child("bookedMatches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    FirebaseMessaging.getInstance().subscribeToTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        DatabaseReference ref2 =
                db.getReference().child("users").child(user.getUid()).child("createdMatches");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    FirebaseMessaging.getInstance().subscribeToTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    /**
     * If the user logout from the app, he needs to unsubscribe from all matches, so he
     * will not get notification from chats.
     */
    static void unsubscribe(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref =
                db.getReference().child("users").child(user.getUid()).child("bookedMatches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        DatabaseReference ref2 =
                db.getReference().child("users").child(user.getUid()).child("createdMatches");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

    }
}
