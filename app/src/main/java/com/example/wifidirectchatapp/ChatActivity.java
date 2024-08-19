package com.example.wifidirectchatapp;

import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private ChatManager chatManager;
    private TextView chatTextView;
    private EditText messageEditText;
    private Button sendButton;
    private boolean isServer; // Flag to determine if this device is the server

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            chatTextView.append("Friend: " + msg.obj.toString() + "\n");
            chatTextView.scrollTo(0, chatTextView.getBottom()); // Scroll to the bottom
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

        // Determine if this device should act as a server or client
        isServer = getIntent().getBooleanExtra("isServer", false);

        if (isServer) {
            // Start as a server
            new Thread(() -> {
                chatManager = ChatManager.createServer(8888, handler);
                chatManager.startListening();
            }).start();
        } else {
            // Start as a client, first get the server IP address
            new Thread(() -> {
                String serverIpAddress = getServerIpAddress();
                chatManager = ChatManager.createClient(serverIpAddress, 8888, handler);
                chatManager.startListening();
            }).start();
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditText.getText().toString();
                if (!message.isEmpty()) {
                    chatManager.sendMessage(message);
                    chatTextView.append("You: " + message + "\n");
                    chatTextView.scrollTo(0, chatTextView.getBottom()); // Scroll to the bottom
                    messageEditText.setText("");
                }
            }
        });
    }

    // Method to get the server's IP address when this device is a client
    private String getServerIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        int gatewayIp = dhcpInfo.gateway;
        return String.format(
                "%d.%d.%d.%d",
                (gatewayIp & 0xff),
                (gatewayIp >> 8 & 0xff),
                (gatewayIp >> 16 & 0xff),
                (gatewayIp >> 24 & 0xff)
        );
    }

    private static class ChatManager {

        private static final int PORT = 8888;
        private ServerSocket serverSocket;
        private Socket clientSocket;
        private BufferedReader input;
        private BufferedWriter output;
        private Handler handler;
        private String serverIp;

        // For Server
        public static ChatManager createServer(int port, Handler handler) {
            return new ChatManager(port, handler);
        }

        // For Client
        public static ChatManager createClient(String serverIp, int port, Handler handler) {
            return new ChatManager(serverIp, port, handler);
        }

        private ChatManager(int port, Handler handler) {
            this.handler = handler;
            new Thread(() -> {
                try {
                    serverSocket = new ServerSocket(port);
                    clientSocket = serverSocket.accept();
                    setupStreams();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private ChatManager(String serverIp, int port, Handler handler) {
            this.serverIp = serverIp;
            this.handler = handler;
            new Thread(() -> {
                try {
                    clientSocket = new Socket(serverIp, port);
                    setupStreams();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private void setupStreams() throws Exception {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }

        public void startListening() {
            new Thread(() -> {
                String message;
                try {
                    while ((message = input.readLine()) != null) {
                        Message msg = handler.obtainMessage();
                        msg.obj = message;
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void sendMessage(String message) {
            new Thread(() -> {
                try {
                    output.write(message + "\n");
                    output.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
