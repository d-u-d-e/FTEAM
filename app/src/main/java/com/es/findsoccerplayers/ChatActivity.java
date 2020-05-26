package com.es.findsoccerplayers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ChatActivity extends AppCompatActivity {

    FirebaseDatabase db;
    FirebaseUser currentUser;

    ImageButton sendButton;
    EditText messageText;
    String relatedMatch;

    MessageAdapter messageAdapter;
    List<Message> chats;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);
        Toolbar toolbar = findViewById(R.id.chat_toolbar);
        toolbar.setTitle(R.string.act_chat_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseDatabase.getInstance();
        sendButton = findViewById(R.id.chat_send_btn);
        messageText = findViewById(R.id.chat_message_text);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString();
                if(!message.equals(""))
                    sendMessage(message);
                else
                    Utils.showCannotSendMessage(ChatActivity.this);
                messageText.setText("");
            }
        });

        Intent info = getIntent();
        relatedMatch = info.getStringExtra("match");
        //TODO assert relatedMatch != null;

        recyclerView = findViewById(R.id.chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        readMessages();
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Message m = snapshot.getValue(Message.class);
                    chats.add(m);
                }

                messageAdapter = new MessageAdapter(ChatActivity.this, chats);
                recyclerView.setAdapter(messageAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
