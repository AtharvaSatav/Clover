package com.example.transactionmanagerapp;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsDetectionService extends Service {

    private static final String TAG = "SmsDetectionService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // This method simulates receiving an SMS message.
    public void onSmsReceived(SmsMessage smsMessage) {
        String sender = smsMessage.getDisplayOriginatingAddress();
        String messageBody = smsMessage.getMessageBody();

        Log.d(TAG, "SMS received from: " + sender);
        Log.d(TAG, "Message: " + messageBody);

        // Extract the amount from the message body
        String amount = extractAmount(messageBody);
        if (amount != null) {
            // Send notification
            sendNotification(this, sender, messageBody, amount);
        } else {
            Log.d(TAG, "No amount found in the message.");
        }
    }

    private String extractAmount(String messageBody) {
        String regex = "Rs\\.(\\d+\\.\\d{2})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(messageBody);

        if (matcher.find()) {
            return matcher.group(1); // Get the amount
        }
        return null; // No match found
    }

    private void sendNotification(Context context, String sender, String messageBody, String amount) {
        // Create an intent to start the VoiceRecordingService
        Intent recordIntent = new Intent(context, VoiceRecordingService.class);
        recordIntent.setAction("START_RECORDING");
        recordIntent.putExtra("amount", amount); // Pass the extracted amount to the service

        // Create a PendingIntent for the notification action
        PendingIntent recordPendingIntent = PendingIntent.getService(context, 0, recordIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID")
                .setSmallIcon(R.drawable.ic_mic)
                .setContentTitle("New Transaction Detected")
                .setContentText("Amount: ₹" + amount + ". Record the category by voice.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_mic, "Record Category", recordPendingIntent) // Add action to record category
                .setAutoCancel(true); // Dismiss notification on tap

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        try {
            notificationManager.notify(1, builder.build()); // Show the notification
        } catch (SecurityException e) {
            Log.e(TAG, "Notification permission denied or not available.", e);
        }
    }
}










//package com.example.transactionmanagerapp; // Change this to your package name
//
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//import androidx.core.app.NotificationCompat;
//
//public class SmsDetectionService extends Service {
//    private static final String CHANNEL_ID = "SmsDetectionServiceChannel";
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // Create a notification channel for Android 8.0 and above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel serviceChannel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Clover SMS Detection Service",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(serviceChannel);
//            }
//        }
//
//        // Create a persistent notification to keep the service alive
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Clover App SMS Detection")
//                .setContentText("Detecting SMS in the background")
//                .setSmallIcon(R.drawable.ic_notification) // You need to add this icon to your drawable
//                .build();
//
//        startForeground(1, notification);
//
//        // Your SMS detection logic here
//
//        return START_STICKY; // Ensures the service restarts if it gets killed
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null; // We are not binding this service to any UI component
//    }
//}