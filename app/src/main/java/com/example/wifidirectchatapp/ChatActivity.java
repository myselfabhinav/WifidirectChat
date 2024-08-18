package com.example.wifidirectchatapp;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatActivity extends AppCompatActivity {

    private ChatManager chatManager;
    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendButton;

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            chatTextView.append("Friend: " + msg.obj.toString() + "\n");
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTextView = findViewById(R.id.chat);
        messageEditText = findViewById(R.id.message);
        sendButton = findViewById(R.id.send);

        // Example connection as a server, replace with your own connection logic
        new Thread(() -> {
            chatManager = ChatManager.createServer(8888, handler);
            chatManager.startListening();
        }).start();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString();
                chatManager.sendMessage(message);
                chatTextView.append("You: " + message + "\n");
                messageEditText.setText("");
            }
        });
    }
}
