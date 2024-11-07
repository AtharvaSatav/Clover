package com.example.transactionmanagerapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager; // Import for NotificationManager
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;  // Unique request code for permissions
    private ListView textViewSpokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//        String packageName = getPackageName();
//
//        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setData(Uri.parse("package:" + packageName));
//            startActivity(intent);
//        }

        // Start your SMS detection service
//        Intent serviceIntent = new Intent(this, SmsDetectionService.class);
//        startForegroundService(serviceIntent);

        // Initialize the ListView
        textViewSpokenText = findViewById(R.id.textViewSpokenText);

        // Check and request all necessary permissions
        checkAndRequestPermissions();
        createNotificationChannel();
    }

    // Method to check and request all necessary permissions
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // List of permissions needed for the app
            String[] permissions = {
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.FOREGROUND_SERVICE_LOCATION
            };

            // Check which permissions are not yet granted
            boolean permissionsNeeded = false;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNeeded = true;
                    break;
                }
            }

            // Request permissions if they are not yet granted
            if (permissionsNeeded) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            } else {
                // Permissions already granted, display data
                displayLastSpokenText();
            }
        } else {
            // Permissions are automatically granted for devices below Android 6.0
            displayLastSpokenText();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update the ListView with the latest data when the activity resumes
        displayLastSpokenText();
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            // Check if all permissions were granted
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted, display the data
                displayLastSpokenText();
            } else {
                // Permissions were not granted, show an appropriate message
                TextView textView = findViewById(R.id.permission_denied_text);
                textView.setText("Required permissions not granted. The app cannot function without these permissions.");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Method to display the last spoken text (or transactions) in the ListView
    private void displayLastSpokenText() {
        SharedPreferences prefs = getSharedPreferences("VoiceData", MODE_PRIVATE);
        String transactions = prefs.getString("transactions", "No transactions recorded yet.");
        String[] transactionArray = transactions.split("\n");

        // Create an ArrayAdapter to display the transactions in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, transactionArray);

        ListView listView = findViewById(R.id.textViewSpokenText);
        listView.setAdapter(adapter);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel is a new construct that is not
        // part of the support library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "CHANNEL_ID"; // Use the same ID as used in the SmsReceiver
            CharSequence name = "Transaction Notifications"; // Name of the channel
            String description = "Channel for transaction-related notifications"; // Description
            int importance = NotificationManager.IMPORTANCE_HIGH; // Set importance level

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
