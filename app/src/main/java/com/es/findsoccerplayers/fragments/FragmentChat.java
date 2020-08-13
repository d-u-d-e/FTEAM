package com.es.findsoccerplayers.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.models.Message;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MessageAdapter;
import com.google.android.gms.common.api.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.android.volley.AuthFailureError;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import com.es.findsoccerplayers.MySingleton;

public class FragmentChat extends Fragment {

    final private String TAG = "FragmentChat";

    private FirebaseDatabase db;
    private FirebaseUser currentUser;

    private EditText messageText;
    private String matchID;
    public static String openMatch;

    private MessageAdapter messageAdapter;
    private List<Message> chats;
    private RecyclerView recyclerView;
    private SharedPreferences preferences;
    private String lastViewedMessage;

    private Context context;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAA-4ONOMg:APA91bHdgJGWL2WfWG0Ql5FsRmXVzq_O4mC5i5q5x7BMImnCRkDlMbhIg6VL72Z5LOA861KF5_ZR7CJdzLV4mPOHw8t3DQ9rh8k-4ADyIeBsmzATx9mL_YLVHaBRNcVPv9xcS_2EmhL3";
    final private String contentType = "application/json";

    private ChildEventListener listener;

    public static boolean isDisplayed = false;
    public static boolean endReached;
    private Lock mutex = new ReentrantLock();

    private boolean seenMsgRetrieved = false;
    private int counter = 0;

    public FragmentChat(String matchID, Context context){
        super();
        this.matchID = matchID;
        this.context = context;
        openMatch = matchID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat, container, false);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        ImageButton sendButton = view.findViewById(R.id.chat_send_btn);
        messageText = view.findViewById(R.id.chat_message_text);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString();
                if(!message.equals(""))
                    sendMessage(message);
                else
                    Utils.showToast(getActivity(), R.string.send_empty_message);
                messageText.setText("");
            }
        });


        recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert manager != null;
                int position = manager.findLastVisibleItemPosition();
                int total = manager.getItemCount();
                if(position != RecyclerView.NO_POSITION)
                    endReached = position >= total - 1;
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        lastViewedMessage = preferences.getString(matchID + "-lastViewedMessage", null);

        endReached = false;
        chats = new ArrayList<>();
        messageAdapter = new MessageAdapter(context, chats);
        recyclerView.setAdapter(messageAdapter);
        if(lastViewedMessage == null)
            seenMsgRetrieved = true;

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(matchID, 1);

        sync2();
        return view;
    }

    /**
     * Method to compute the sendMessage. The param String is the message to delivery. This method write the message to the database and send a notification
     * to the right topic, to notiy other users, that e new message has come
     * @param message string to send
     */
    private void sendMessage(String message){
        DatabaseReference ref = db.getReference("chats").child(matchID);
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        SharedPreferences.Editor editor = preferences.edit();

        DatabaseReference r = ref.push();
        Message m = new Message(r.getKey(), currentUser.getUid(), username, message, System.currentTimeMillis());
        r.setValue(m);
        editor.putString(matchID + "-lastViewedMessage", m.getMessageID()); //this information is cleared when any match is deleted from every user
        lastViewedMessage = m.getMessageID();
        editor.apply();

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject notificationNoti = new JSONObject();
        try {
            //Notification Data used by our Service
            notificationBody.put("title", Utils.getPreviewDescription(username));
            notificationBody.put("body", Utils.getPreviewDescription(message));
            notificationBody.put("sender", currentUser.getUid());
            notificationBody.put("match", matchID);

            //Notification data used by the System
            notificationNoti.put("tag", matchID);
            notificationNoti.put("title", Utils.getPreviewDescription(username));
            notificationNoti.put("body", Utils.getPreviewDescription(message));
            notificationNoti.put("icon", "ic_message");

            //Construct of the JSONObject fields
            notification.put("to", "/topics/" + matchID);
            notification.put("notification", notificationNoti );
            notification.put("data", notificationBody);


        } catch (JSONException e) {

        }
        sendNotification(notification);
    }

    public void onNewMessagesRead(){
        mutex.lock();
        try {
            messageAdapter.onNewMessagesRead();
            if(chats.size() > 0)
                recyclerView.scrollToPosition(chats.size() - 1);
        }finally {
            mutex.unlock();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DatabaseReference ref = db.getReference("chats").child(matchID);
        ref.removeEventListener(listener);
        isDisplayed = false;
        openMatch = null;
        endReached = true;
    }

    private void sync2(){
        DatabaseReference ref = db.getReference("chats").child(matchID);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final SharedPreferences.Editor editor = preferences.edit();
        //Cancel the notification if we open the correlated chat

        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message m = snapshot.getValue(Message.class);
                assert m != null;
                String messageID = m.getMessageID();
                mutex.lock();
                try {
                    chats.add(m);
                    if(!seenMsgRetrieved){ //first pass
                        counter++;
                        //at the end of the first read counter indicates the point where new messages start
                        if(messageID.equals(lastViewedMessage)){
                            seenMsgRetrieved = true;
                            messageAdapter.setNewMsgStartingPosition(counter);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(counter);
                        }
                    }
                    else{
                        messageAdapter.notifyItemInserted(chats.size() - 1);

                        if(m.getSenderID().equals(user.getUid()))
                            onNewMessagesRead();
                        else{
                            messageAdapter.incrementNewMessagesCounter();
                            editor.putString(matchID + "-lastViewedMessage", m.getMessageID());
                            lastViewedMessage = m.getMessageID();
                            editor.apply();
                            if(isDisplayed && endReached)
                                    recyclerView.scrollToPosition(chats.size()-1);
                        }
                    }
                }finally {
                    mutex.unlock();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.orderByKey().addChildEventListener(listener);
    }

    /**
     * This method send the notification, using the JSONObject passed. If fail, show a toast to inform
     * @param notification the JSONObject to send
     */
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "Notification send");
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.showToast(getContext(), getString(R.string.notification_failed));
                        Log.e(TAG, "Error:" + error);
                    }
                }){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(FragmentChat.this.getActivity()).addToRequestQueue(jsonObjectRequest);
    }
}
