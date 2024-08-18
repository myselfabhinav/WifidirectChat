package com.example.wifidirectchatapp;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView statusTextView;
    private Button discoverPeersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.statusTextView);
        discoverPeersButton = findViewById(R.id.discoverPeersButton);

        checkWifiStatus();

        discoverPeersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DiscoverPeersActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkWifiStatus() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            statusTextView.setText("Wi-Fi is On");
            Toast.makeText(this, "Wi-Fi is On", Toast.LENGTH_SHORT).show();
        } else {
            statusTextView.setText("Wi-Fi is Off");
            Toast.makeText(this, "Wi-Fi is Off", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }
}
