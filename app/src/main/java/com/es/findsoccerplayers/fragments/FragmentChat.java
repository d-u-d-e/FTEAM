package com.es.findsoccerplayers.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.models.Message;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FragmentChat extends Fragment {

    private FirebaseDatabase db;
    private FirebaseUser currentUser;

    private EditText messageText;
    private String matchID;

    private MessageAdapter messageAdapter;
    private List<Message> chats;
    private RecyclerView recyclerView;
    private SharedPreferences preferences;
    private String lastViewedMessage;

    private Context context;

    private ChildEventListener listener;

    public static boolean isDisplayed = false;
    public static boolean endReached;
    public static boolean toRead;
    private Lock mutex = new ReentrantLock();

    public FragmentChat(String matchID, Context context){
        super();
        this.matchID = matchID;
        this.context = context;
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
        endReached = true;
        toRead = false;

        readMessages();
        return view;
    }

    private void sendMessage(String message){
        DatabaseReference ref = db.getReference("chats").child(matchID);
        String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        SharedPreferences.Editor editor = preferences.edit();

        DatabaseReference r = ref.push();
        Message m = new Message(r.getKey(), currentUser.getUid(), username, message, System.currentTimeMillis());
        r.setValue(m);
        editor.putString(matchID + "-lastViewedMessage", m.getMessageID()); //this information is cleared when any match is deleted from every user
        editor.apply();
    }

    private void readMessages(){
        chats = new ArrayList<>();
        DatabaseReference ref = db.getReference("chats").child(matchID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chats.clear();
                int count = 0;
                boolean lastSeenReceived = false;

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Message m = snapshot.getValue(Message.class);
                    assert m != null;

                    if(!lastSeenReceived){
                        count++;
                        if(m.getMessageID().equals(lastViewedMessage))
                            lastSeenReceived = true;
                    }
                    chats.add(m);
                }

                messageAdapter = new MessageAdapter(getActivity(), chats, count);
                recyclerView.setAdapter(messageAdapter);

                if(count < chats.size()){
                    toRead = true;
                    recyclerView.scrollToPosition(count);
                    SharedPreferences.Editor editor = preferences.edit();
                    lastViewedMessage = chats.get(chats.size()-1).getMessageID();
                    editor.putString(matchID, lastViewedMessage);
                    editor.apply();
                }
                sync();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onNewMessagesRead(){
        if(messageAdapter != null)
            messageAdapter.OnNewMessagesRead();
        toRead = false;
        if(chats.size() > 0)
            recyclerView.scrollToPosition(chats.size()-1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DatabaseReference ref = db.getReference("chats").child(matchID);
        ref.removeEventListener(listener);
        isDisplayed = false;
        endReached = true;
    }

    private void sync(){
        DatabaseReference ref = db.getReference("chats").child(matchID);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        listener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                Message m = snapshot.getValue(Message.class);
                assert m != null;
                SharedPreferences.Editor editor = preferences.edit();
                //this is because startAt is unfortunately inclusive
                if(!m.getMessageID().equals(lastViewedMessage)){
                    mutex.lock();
                    try{
                        chats.add(m);
                        messageAdapter.notifyItemInserted(chats.size()-1);
                        if(m.getSenderID().equals(user.getUid())){
                            onNewMessagesRead();
                            editor.putString(matchID + "-lastViewedMessage", m.getMessageID());
                            editor.apply();
                        }
                        else{
                            if(isDisplayed){
                                if(endReached)
                                    recyclerView.scrollToPosition(chats.size()-1);
                                messageAdapter.incrementNewMessagesCounter();
                                toRead = false;
                                editor.putString(matchID + "-lastViewedMessage", m.getMessageID());
                                editor.apply();
                            }
                            else{
                                toRead = true;
                                messageAdapter.incrementNewMessagesCounter();
                            }
                        }
                    }
                    finally {
                        mutex.unlock();
                    }
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

        if(lastViewedMessage != null)
            ref.orderByKey().startAt(lastViewedMessage).addChildEventListener(listener);
        else
            ref.addChildEventListener(listener);
    }
}
