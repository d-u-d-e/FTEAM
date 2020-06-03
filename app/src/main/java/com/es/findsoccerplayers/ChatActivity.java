package com.es.findsoccerplayers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    DatabaseReference db;
    FirebaseUser currentUser;

    ImageButton sendButton;
    EditText messageText;
    String relatedMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
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
        db = FirebaseDatabase.getInstance().getReference();
        sendButton = findViewById(R.id.chat_send_btn);
        messageText = findViewById(R.id.chat_message_text);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageText.getText().toString();
                if(!message.equals(""))
                    sendMessage(currentUser.getUid(), message);
                else
                    Utils.showCannotSendMessage(ChatActivity.this);
                messageText.setText("");
            }
        });

        Intent info = getIntent();
        relatedMatch = info.getStringExtra("match");
        //TODO assert relatedMatch != null;
    }

    private void sendMessage(String sender, String message){
        DatabaseReference ref = db.child(relatedMatch);
        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", sender);
        map.put("timestamp", System.currentTimeMillis());
        map.put("text", message);
        ref.push().setValue(map);
    }
}
