package com.es.fteam;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
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
        //error should be displayed immediately, hence unique is true.
        showToast(c, error, Toast.LENGTH_LONG, true);
    }

    public static void showErrorToast(Context c, String s, boolean unique){
        showToast(c, s, Toast.LENGTH_LONG, unique);
    }

    /**
     * unique is used as a flag to indicate that any previously unique toast should be cancelled and
     * replaced by another toast. If unique is false, the toast is stacked. unique is set to true
     * usually when the user might generate the same toast more than once, for example when he tries to
     * create a match without entering all the required fields: so we avoid stacking the same toast again,
     * by cancelling the current one and replacing it with the same toast. Also unique is set to true when
     * we don't want to display the toast if another activity is started (meaning that the current one goes on pause).
     * My Activity overrides onPause() to cancel the current unique toast.
     */
    private static void showToast(Context c, String text, int duration, boolean unique){

        if(c == null) return;

        if(!unique){
            Toast.makeText(c, text, duration).show();
            return;
        }

        if(toast != null && toast.get() != null)
            toast.get().cancel();

        Toast t = Toast.makeText(c, text, duration);
        toast = new WeakReference<>(t);
        t.show();
    }

    static void showToast(Context c, int resource, int duration, boolean unique){

        if(c == null) return;

        if(!unique){
            Toast.makeText(c, resource, duration).show();
            return;
        }

        if(toast != null && toast.get() != null)
            toast.get().cancel();

        Toast t = Toast.makeText(c, resource, duration);
        toast = new WeakReference<>(t);
        t.show();
    }

    public static void showToast(Context c, String text, boolean unique){
        showToast(c, text, Toast.LENGTH_SHORT, unique);
    }

    public static void showToast(Context c, int resource, boolean unique){
        showToast(c, resource, Toast.LENGTH_SHORT, unique);
    }

    public static void showOfflineWriteToast(Context c, boolean unique){
        showToast(c, R.string.offline_write, unique);
    }

    static void showOfflineReadToast(Context c, boolean unique){
        showToast(c, R.string.offline_read, unique);
    }

    public static String getPreviewDescription(String description) {
        if(description == null)
            return  "";
        else if (description.length() <= 20)
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
     * Users can log out and log in any time. If the user logs out, he un-subscribes from all match topics.
     * If he logs in again, he will subscribe again to all his matches, created or booked, so he can get notification for new messages in chat
     */
    static void subscribe(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref =
                db.getReference().child("users").child(ActivityLogin.currentUserID).child("bookedMatches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    assert matchID != null;
                    FirebaseMessaging.getInstance().subscribeToTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        DatabaseReference ref2 =
                db.getReference().child("users").child(ActivityLogin.currentUserID).child("createdMatches");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    assert matchID != null;
                    FirebaseMessaging.getInstance().subscribeToTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    /**
     * If the user logs out from the app, he needs to unsubscribe from all matches, so he
     * will not get notification from chats
     */
    static void unsubscribe(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref =
                db.getReference().child("users").child(ActivityLogin.currentUserID).child("bookedMatches");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    assert matchID != null;
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
        DatabaseReference ref2 =
                db.getReference().child("users").child(ActivityLogin.currentUserID).child("createdMatches");
        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for(DataSnapshot booked : snapshot.getChildren()){
                    String matchID = booked.getKey();
                    assert matchID != null;
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(matchID);

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    static void showMyLocation(GoogleMap map){
        map.setMyLocationEnabled(true); // show a small blue circle for my position
        map.getUiSettings().setMyLocationButtonEnabled(false);// I create my own beautiful position floating action button. Don't need this
    }
}
