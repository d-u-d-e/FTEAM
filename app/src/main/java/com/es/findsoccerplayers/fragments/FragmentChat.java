package com.es.findsoccerplayers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.es.findsoccerplayers.models.Message;
import com.es.findsoccerplayers.R;
import com.es.findsoccerplayers.Utils;
import com.es.findsoccerplayers.adapter.MessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FragmentChat extends Fragment {

    private FirebaseDatabase db;
    private FirebaseUser currentUser;

    private EditText messageText;
    private String relatedMatch;

    private MessageAdapter messageAdapter;
    private List<Message> chats;
    private RecyclerView recyclerView;


    public FragmentChat(String relatedMatch){
        super();
        this.relatedMatch = relatedMatch;
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
                    Utils.showCannotSendMessage(getActivity());
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
        DatabaseReference ref = db.getReference("chats").child(relatedMatch);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", currentUser.getUid());
        map.put("timestamp", System.currentTimeMillis());
        map.put("text", message);
        ref.push().setValue(map);
    }

    private void readMessages(){
        chats = new ArrayList<>();
        DatabaseReference ref = db.getReference("chats").child(relatedMatch);
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
}
