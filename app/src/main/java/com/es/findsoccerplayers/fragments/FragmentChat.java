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
import androidx.annotation.Nullable;
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
    private Lock mutex = new ReentrantLock();

    private boolean seenMsgRetrieved = false;
    private int counter = 0;

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

        endReached = false;
        chats = new ArrayList<>();
        messageAdapter = new MessageAdapter(context, chats);
        recyclerView.setAdapter(messageAdapter);
        if(lastViewedMessage == null)
            seenMsgRetrieved = true;

        sync2();
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
        endReached = true;
    }

    private void sync2(){
        DatabaseReference ref = db.getReference("chats").child(matchID);
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final SharedPreferences.Editor editor = preferences.edit();

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
                            recyclerView.scrollToPosition(chats.size() - 1);
                        }
                    }
                    else{
                        messageAdapter.incrementNewMessagesCounter();
                        messageAdapter.notifyItemInserted(chats.size() - 1);

                        if(m.getSenderID().equals(user.getUid()))
                            onNewMessagesRead();
                        else if(isDisplayed){
                            if(endReached)
                                recyclerView.scrollToPosition(chats.size()-1);
                            editor.putString(matchID + "-lastViewedMessage", m.getMessageID());
                            editor.apply();
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
        ref.addChildEventListener(listener);
    }
}
