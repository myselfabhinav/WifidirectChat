package com.example.wifidirectchatapp;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatManager {
    private static final String TAG = "ChatManager";
    private Socket socket;
    private ServerSocket serverSocket;
    private Handler handler;
    private BufferedReader input;
    private PrintWriter output;

    public ChatManager(Socket socket, Handler handler) {
        this.socket = socket;
        this.handler = handler;
        initializeStreams();
    }

    private void initializeStreams() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing streams", e);
        }
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    public void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    handler.obtainMessage(1, message).sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading message", e);
            }
        }).start();
    }

    public static ChatManager createServer(int port, Handler handler) {
        ChatManager chatManager = null;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            chatManager = new ChatManager(socket, handler);
        } catch (IOException e) {
            Log.e(TAG, "Error creating server", e);
        }
        return chatManager;
    }

    public static ChatManager connectToServer(String host, int port, Handler handler) {
        ChatManager chatManager = null;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            chatManager = new ChatManager(socket, handler);
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to server", e);
        }
        return chatManager;
    }
}
