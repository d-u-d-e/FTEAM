package com.es.findsoccerplayers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import java.util.Map;
import com.es.findsoccerplayers.MySingleton;

public class FragmentChat extends Fragment {

    private FirebaseDatabase db;
    private FirebaseUser currentUser;

    private EditText messageText;
    private String matchID;

    private MessageAdapter messageAdapter;
    private List<Message> chats;
    private RecyclerView recyclerView;
    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAA-4ONOMg:APA91bHdgJGWL2WfWG0Ql5FsRmXVzq_O4mC5i5q5x7BMImnCRkDlMbhIg6VL72Z5LOA861KF5_ZR7CJdzLV4mPOHw8t3DQ9rh8k-4ADyIeBsmzATx9mL_YLVHaBRNcVPv9xcS_2EmhL3";
    final private String contentType = "application/json";


    public FragmentChat(String matchID){
        super();
        this.matchID = matchID;
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

        readMessages();

        return view;
    }

    private void sendMessage(String message){
        DatabaseReference ref = db.getReference("chats").child(matchID);
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        Message m = new Message(currentUser.getUid(), username, message, System.currentTimeMillis());
        ref.push().setValue(m);

        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        JSONObject notificationNoti = new JSONObject();
        try {
            notificationBody.put("title", Utils.getPreviewDescription(username));
            notificationBody.put("body", Utils.getPreviewDescription(message));
            notificationBody.put("sender", currentUser.getUid());
            notificationBody.put("match", matchID);
            notificationNoti.put("tag", matchID);
            notificationNoti.put("title", Utils.getPreviewDescription(username));
            notificationNoti.put("body", Utils.getPreviewDescription(message));
            notificationNoti.put("icon", "ic_message");

            notification.put("to", "/topics/" + matchID);
            notification.put("notification", notificationNoti );
            notification.put("data", notificationBody);


        } catch (JSONException e) {

        }
        sendNotification(notification);
    }

    private void readMessages(){
        chats = new ArrayList<>();
        DatabaseReference ref = db.getReference("chats").child(matchID);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Message m = snapshot.getValue(Message.class);
                    chats.add(m);
                }

                messageAdapter = new MessageAdapter(getActivity(), chats);
                recyclerView.setAdapter(messageAdapter);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(getContext(), "on Response", Toast.LENGTH_LONG).show();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Request error", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(FragmentChat.this.getActivity()).addToRequestQueue(jsonObjectRequest);
    }
}
