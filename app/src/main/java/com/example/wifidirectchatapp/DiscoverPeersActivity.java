package com.example.wifidirectchatapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class DiscoverPeersActivity extends AppCompatActivity {

    private static final String TAG = "DiscoverPeersActivity";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int MY_PERMISSIONS_REQUEST_NEARBY_WIFI_DEVICES = 100;

    private WifiP2pManager wifiP2pManager;
    private Channel channel;
    private TextView connectionStatusTextView;
    private ListView peersListView;
    private Button discoverPeersButton;
    private Button chatButton;

    private List<WifiP2pDevice> peerList = new ArrayList<>();
    private ArrayAdapter<String> peersAdapter;
    private String selectedPeerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_peers);

        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        discoverPeersButton = findViewById(R.id.discoverPeersButton);
        peersListView = findViewById(R.id.peersListView);
        chatButton = findViewById(R.id.chatButton);
// In DiscoverPeersActivity.java

        Button btnFileTransfer = findViewById(R.id.btn_file_transfer);
        btnFileTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DiscoverPeersActivity.this, FileTransferActivity.class);
                startActivity(intent);
            }
        });

        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        peersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        peersListView.setAdapter(peersAdapter);

        discoverPeersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndDiscoverPeers();
            }
        });

        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPeerName = peersListView.getItemAtPosition(position).toString();
                connectToSelectedPeer(selectedPeerName);
            }
        });

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPeerName != null) {
                    Intent intent = new Intent(DiscoverPeersActivity.this, ChatActivity.class);
                    intent.putExtra("peerName", selectedPeerName);
                    startActivity(intent);
                }
            }
        });
    }

    private void checkPermissionsAndDiscoverPeers() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.NEARBY_WIFI_DEVICES
            }, MY_PERMISSIONS_REQUEST_LOCATION);

        } else {
            Log.i(TAG, "Permissions granted, starting discovery");
            discoverPeers();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            boolean locationPermissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean wifiPermissionGranted = grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (locationPermissionGranted && wifiPermissionGranted) {
                discoverPeers();
            } else {
                Toast.makeText(this, "Permission denied. Cannot use Wi-Fi Direct.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Permissions not granted. Location: " + locationPermissionGranted + ", Wi-Fi: " + wifiPermissionGranted);
                if (!wifiPermissionGranted) {
                    Log.e(TAG, "Wi-Fi permission denied. This is required to discover peers.");
                }
            }
        }
    }


    private void discoverPeers() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "Permissions are not granted, requesting permissions again");
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.NEARBY_WIFI_DEVICES
            }, MY_PERMISSIONS_REQUEST_LOCATION);

            return; // Exit the method to wait for permission result
        }

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(DiscoverPeersActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Discovery initiated successfully");

                if (ActivityCompat.checkSelfPermission(DiscoverPeersActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(DiscoverPeersActivity.this, android.Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {

                    // Request missing permissions
                    ActivityCompat.requestPermissions(DiscoverPeersActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.NEARBY_WIFI_DEVICES
                    }, MY_PERMISSIONS_REQUEST_LOCATION);

                    return; // Exit the method to wait for the permission result
                }
                wifiP2pManager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        if (peers.getDeviceList().size() > 0) {
                            peerList.clear();
                            peersAdapter.clear();
                            peerList.addAll(peers.getDeviceList());
                            for (WifiP2pDevice device : peers.getDeviceList()) {
                                peersAdapter.add(device.deviceName);
                            }
                            peersAdapter.notifyDataSetChanged();
                            Log.i(TAG, "Peers available: " + peers.getDeviceList().size());
                        } else {
                            Toast.makeText(DiscoverPeersActivity.this, "No Peers Found", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "No peers found");
                        }
                    }
                });
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(DiscoverPeersActivity.this, "Discovery Failed: " + reasonCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Discovery failed with reason code: " + reasonCode);
            }
        });
    }

    private void connectToSelectedPeer(final String peerName) {
        for (WifiP2pDevice device : peerList) {
            if (device.deviceName.equals(peerName)) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.NEARBY_WIFI_DEVICES
                    }, MY_PERMISSIONS_REQUEST_LOCATION);

                    return; // Exit the method to wait for permission result
                }

                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatusTextView.setText("Connected to " + peerName);
                        chatButton.setEnabled(true);
                        Log.i(TAG, "Connected to " + peerName);
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        connectionStatusTextView.setText("Connection Failed");
                        chatButton.setEnabled(false);
                        Toast.makeText(DiscoverPeersActivity.this, "Connection Failed: " + reasonCode, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Connection failed with reason code: " + reasonCode);
                    }
                });
                return;
            }
        }
        Toast.makeText(this, "Peer not found", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Peer not found: " + peerName);
    }
}
