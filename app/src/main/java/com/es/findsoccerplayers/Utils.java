package com.es.findsoccerplayers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static String  CHANNEL_ID = "findSoccerPlayer_channel";

    static void showErrorToast(Context c, Exception ex){
        String error = c.getString(R.string.unknown_error);
        if(ex != null) error = ex.getMessage();
        Toast.makeText(c, error, Toast.LENGTH_LONG).show();
    }

    public static void showErrorToast(Context c, String s){
        Toast.makeText(c, s, Toast.LENGTH_LONG).show();
    }

    static void showSuccessLoginToast(Context c){
        Toast.makeText(c, R.string.login_success, Toast.LENGTH_SHORT).show();
    }

    static void showSuccessRegisterToast(Context c){
        Toast.makeText(c, R.string.register_success, Toast.LENGTH_SHORT).show();
    }


    public static void showUnimplementedToast(Context c){
        Toast.makeText(c, R.string.unimplemented, Toast.LENGTH_SHORT).show();
    }

    public static void showOfflineWriteToast(Context c){
        Toast.makeText(c, R.string.offlineWrite, Toast.LENGTH_SHORT).show();
    }

    public static void showOfflineReadToast(Context c){
        Toast.makeText(c, R.string.offlineRead, Toast.LENGTH_SHORT).show();
    }

    static void showSuccessResetPswToast(Context c){
        Toast.makeText(c, R.string.reset_psw_success, Toast.LENGTH_SHORT).show();
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

    public static long getMillisTime(long timestamp){
        return timestamp % (24 * 60 * 60 * 1000);
    }

    public static boolean isOnline(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


    public static void displayNotification(Context context, String text){
        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_account_24);
        builder.setContentTitle("New Game");
        builder.setContentText(text);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hostelmate notification";
            String description = "Hi";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
