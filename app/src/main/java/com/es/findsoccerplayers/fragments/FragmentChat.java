package com.es.findsoccerplayers.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.NotificationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.ActivityLogin;
import com.es.findsoccerplayers.ActivitySelectMatch;
import com.es.findsoccerplayers.models.Message;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    private FirebaseDatabase db;
    private EditText messageText;
    private MessageAdapter messageAdapter;
    private List<Message> chats;
    private RecyclerView recyclerView;

    private SharedPreferences preferences;
    private String lastViewedMessage;
    private Context context;

    private static final String FCM_API = "https://fcm.googleapis.com/fcm/send";

    private static final String SERVER_KEY = "key=" +
            "AAAA-4ONOMg:APA91bHdgJGWL2WfWG0Ql5FsRmXVzq_O4mC5i5q5x7BMImnCRkDlMbhI" +
            "g6VL72Z5LOA861KF5_ZR7CJdzLV4mPOHw8t3DQ9rh8k-4ADyIeBsmzATx9mL_YLVHaBRNcVPv9xcS_2EmhL3";

    private static final String CONTENT_TYPE = "application/json";
    static final String LAST_VIEWED_MESSAGE = "lastViewedMessage";

    private ChildEventListener listener;

    //true if current user is looking at the chat, otherwise false
    public static boolean isDisplayed = false;
    //true if the end of the message list has been reached, otherwise false.
    //we included this variable to make our reception of messages more intelligent: if the user reached
    //the end, then the receipt of new messages will automatically scroll the list. Otherwise nothing will happen.
    public static boolean endReached;
    private Lock mutex = new ReentrantLock();

    //true if all already seen messages have been retrieved
    private boolean seenMsgRetrieved = false;

    //counter for already seen messages
    private int counter = 0;

    public FragmentChat(Context context){
        super();
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat, container, false);

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
        //here endReached is adjusted when user scrolls the list
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
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
        //preferences are set for each user used to log in with
        lastViewedMessage = preferences.getString(ActivityLogin.currentUserID + "." +
                ActivitySelectMatch.matchID + "." + LAST_VIEWED_MESSAGE, null);

        //will be updated after
        endReached = true;
        chats = new ArrayList<>();
        messageAdapter = new MessageAdapter(context, chats);
        recyclerView.setAdapter(messageAdapter);
        //clearly if no lastViewedMessage exists, then 0 seen msg have also been retrieved
        if(lastViewedMessage == null)
            seenMsgRetrieved = true;

        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ActivitySelectMatch.matchID, 1);

        sync();
        return view;
    }

    /**
     * Sends a message. The param message is the message to deliver. This method writes the message to the database and sends a notification
     * to the right topic, to notify other interested users that a new message has arrived
     * @param message string to send
     */
    private void sendMessage(String message){
        DatabaseReference ref = db.getReference("chats").child(ActivitySelectMatch.matchID);
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        SharedPreferences.Editor editor = preferences.edit();

        DatabaseReference r = ref.push();
        Message m = new Message(r.getKey(), ActivityLogin.currentUserID, username, message, System.currentTimeMillis());
        r.setValue(m);
        //this information is cleared when any booked match is deleted from any user
        editor.putString(ActivityLogin.currentUserID + "." +
                ActivitySelectMatch.matchID + "." + LAST_VIEWED_MESSAGE, m.getMessageID());
        lastViewedMessage = m.getMessageID();
        editor.apply();

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject notificationNoti = new JSONObject();
        try {
            //Notification Data used by our Service
            notificationBody.put("title", Utils.getPreviewDescription(username));
            notificationBody.put("body", Utils.getPreviewDescription(message));
            notificationBody.put("sender", ActivityLogin.currentUserID);
            notificationBody.put("match", ActivitySelectMatch.matchID);

            //Notification data used by the System
            notificationNoti.put("tag", ActivitySelectMatch.matchID);
            notificationNoti.put("title", Utils.getPreviewDescription(username));
            notificationNoti.put("body", Utils.getPreviewDescription(message));
            notificationNoti.put("icon", "ic_message");

            //Construct of the JSONObject fields
            notification.put("to", "/topics/" + ActivitySelectMatch.matchID);
            notification.put("notification", notificationNoti );
            notification.put("data", notificationBody);


        } catch (JSONException e) {
            Log.e(TAG, "Error: " + e);
        }
        sendNotification(notification);
    }

    /**
     * When this method is called, all new messages are cleared
     */
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
        DatabaseReference ref = db.getReference("chats").child(ActivitySelectMatch.matchID);
        ref.removeEventListener(listener);
        isDisplayed = false;
        endReached = true;
    }

    private void sync(){
        DatabaseReference ref = db.getReference("chats").child(ActivitySelectMatch.matchID);
        final SharedPreferences.Editor editor = preferences.edit();
        //Cancel the notification if we open the related chat

        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message m = snapshot.getValue(Message.class);
                assert m != null;
                String messageID = m.getMessageID();
                mutex.lock();
                try {
                    chats.add(m);
                    if(!seenMsgRetrieved){ //while not every already seen message have been retrieved, do this
                        counter++;
                        //at the end counter indicates the point where new messages start
                        if(messageID.equals(lastViewedMessage)){
                            seenMsgRetrieved = true;
                            messageAdapter.setNewMsgStartingPosition(counter);
                            messageAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(counter);
                        }
                    }
                    else{
                        messageAdapter.notifyItemInserted(chats.size() - 1);

                        //sending a message will "clear" all new (unread) messages
                        if(m.getSenderID().equals(ActivityLogin.currentUserID))
                            onNewMessagesRead();
                        else{
                            messageAdapter.incrementNewMessagesCounter();
                            editor.putString(ActivityLogin.currentUserID + "." +
                                    ActivitySelectMatch.matchID + "." + LAST_VIEWED_MESSAGE, m.getMessageID());
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
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        };
        ref.orderByKey().addChildEventListener(listener);
    }

    /**
     * This method sends the notification, using the JSONObject passed.
     * @param notification the JSONObject to send
     */
    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                FCM_API,
                notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                })

        {
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", SERVER_KEY);
                params.put("Content-Type", CONTENT_TYPE);
                return params;
            }
        };
        MySingleton.getInstance(FragmentChat.this.getActivity()).addToRequestQueue(jsonObjectRequest);
    }
}
