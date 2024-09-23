// The app is reciving the text from bluetooth and also text to speech and display the text . now it just need to update the UI .
package com.example.banglabluetoothtts;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private TextToSpeech tts;
    private TextView receivedTextView;
    private SeekBar pitchSeekBar, speedSeekBar;

    // Replace with your HC-05 UUID and address
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String HC05_ADDRESS = "58:56:00:00:B3:B7"; // "XX:XX:XX:XX:XX:XX"; Replace with your HC-05 MAC address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        receivedTextView = findViewById(R.id.receivedTextView);
        pitchSeekBar = findViewById(R.id.pitchSeekBar);
        speedSeekBar = findViewById(R.id.speedSeekBar);

        // Initialize TTS with Bangla Locale
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.forLanguageTag("bn-BD"));  // Set to Bangla
            }
        });

        // Set up SeekBars for pitch and speed control
        pitchSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float pitch = progress / 50.0f;  // Convert progress to pitch scale
                tts.setPitch(pitch);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float speed = progress / 50.0f;  // Convert progress to speed scale
                tts.setSpeechRate(speed);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Connect to Bluetooth
        connectToBluetooth();
    }

    private void connectToBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice hc05 = bluetoothAdapter.getRemoteDevice(HC05_ADDRESS);

        try {
            bluetoothSocket = hc05.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();

            // Start listening for incoming text
            InputStream inputStream = bluetoothSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            new Thread(() -> {
                try {
                    while (true) {
                        String receivedText = reader.readLine();
                        runOnUiThread(() -> {
                            receivedTextView.setText(receivedText);
                            tts.speak(receivedText, TextToSpeech.QUEUE_FLUSH, null, null);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
