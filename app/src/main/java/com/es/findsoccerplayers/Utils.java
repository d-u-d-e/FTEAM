package com.es.findsoccerplayers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ProcessLifecycleOwner;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Utils {

    private static String  CHANNEL_ID = "findSoccerPlayerChannel";

    private static HashMap<String, WeakReference<Toast>> toastMap = new HashMap<>();

    static void cancelToast(String activityName){
        WeakReference<Toast> toast = toastMap.remove(activityName);
        if(toast != null && toast.get() != null)
            toast.get().cancel();
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
        String className = c.getClass().getName();
        WeakReference<Toast> toast = toastMap.remove(className);
        if(toast != null && toast.get() != null)
            toast.get().cancel();
        toast = new WeakReference<>(Toast.makeText(c, text, duration));
        toast.get().show();
        toastMap.put(className, toast);
    }

    private static void showToast(Context c, int resource, int duration){
        String className = c.getClass().getName();
        WeakReference<Toast> toast = toastMap.remove(className);
        if(toast != null && toast.get() != null)
            toast.get().cancel();
        toast = new WeakReference<>(Toast.makeText(c, resource, duration));
        toast.get().show();
        toastMap.put(className, toast);
    }

    static void showToast(Context c, String text){
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
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectivityManager != null;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null || !networkInfo.isConnectedOrConnecting();
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
            CharSequence name = "HostelMate notification";
            String description = "Hi";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);

            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
