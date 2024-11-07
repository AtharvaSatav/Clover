package com.example.transactionmanagerapp;

import android.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.app.Service;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    String messageBody = smsMessage.getMessageBody();

                    // Log the SMS details for debugging
                    Log.d("SmsReceiver", "SMS received from: " + sender);
                    Log.d("SmsReceiver", "Message: " + messageBody);

                    // Extract the amount from the message body
                    String amount = extractAmount(messageBody);
                    if (amount == null) {
                        Log.d("SmsReceiver", "No amount found in the message.");
                        return; // Exit if no amount is found
                    }

                    // Check if the app has the necessary notification permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                != PackageManager.PERMISSION_GRANTED) {
                            Log.w("SmsReceiver", "POST_NOTIFICATIONS permission not granted. Cannot send notification.");
                            return;
                        }
                    }

                    // Send the notification for the user to categorize the transaction
                    sendNotification(context, sender, messageBody, amount);
                }
            }
        }
    }

    // Method to extract the amount from the message body
    private String extractAmount(String messageBody) {
        // Define a pattern to match currency amounts (e.g., Rs. 1000 or INR 500)
        //String regex = "Rs\\.(\\d+\\.\\d{2})";
        String regex = "(?i)(rs\\.?|inr\\.?|₹)?\\s*(\\d+(?:,\\d{3})*(\\.\\d{1,2}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(messageBody);

        // Check if the pattern matches and return the found amount
        if (matcher.find()) {
            return matcher.group(2); // Get the amount (the first capturing group)
        }
        return null;
    }

    private void sendNotification(Context context, String sender, String messageBody, String amount) {
        // Create an intent to start the VoiceRecordingService
        Intent recordIntent = new Intent(context, VoiceRecordingService.class);
        recordIntent.setAction("START_RECORDING");
        recordIntent.putExtra("amount", amount); // Pass the extracted amount to the service

        // Create a PendingIntent for the notification action
        PendingIntent recordPendingIntent = PendingIntent.getService(context, 0, recordIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

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
            Log.e("SmsReceiver", "Notification permission denied or not available.", e);
        }
    }
}











//package com.example.transactionmanagerapp;
//
//import android.Manifest;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Bundle;
//import android.telephony.SmsMessage;
//import android.util.Log;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//import androidx.core.content.ContextCompat;
//
//public class SmsReceiver extends BroadcastReceiver {
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        Bundle bundle = intent.getExtras();
//        if (bundle != null) {
//            Object[] pdus = (Object[]) bundle.get("pdus");
//            if (pdus != null) {
//                for (Object pdu : pdus) {
//                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
//                    String sender = smsMessage.getDisplayOriginatingAddress();
//                    String messageBody = smsMessage.getMessageBody();
//
//                    // Log the SMS details for debugging
//                    Log.d("SmsReceiver", "SMS received from: " + sender);
//                    Log.d("SmsReceiver", "Message: " + messageBody);
//
//                    // Check if the app has the necessary notification permission before attempting to send a notification
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        // Android 13 and above require POST_NOTIFICATIONS permission
//                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
//                                != PackageManager.PERMISSION_GRANTED) {
//                            // Permission is not granted, handle appropriately
//                            Log.w("SmsReceiver", "POST_NOTIFICATIONS permission not granted. Cannot send notification.");
//                            return; // Exit without sending a notification
//                        }
//                    }
//
//                    // If permission is granted, or if below Android 13, proceed with sending the notification
//                    sendNotification(context, sender, messageBody);
//                }
//            }
//        }
//    }
//
//    private void sendNotification(Context context, String sender, String messageBody) {
//        // Create an intent to start the VoiceRecordingService
//        Intent recordIntent = new Intent(context, VoiceRecordingService.class);
//        recordIntent.setAction("START_RECORDING");
//
//        // Create a PendingIntent for the notification action
//        PendingIntent recordPendingIntent = PendingIntent.getService(context, 0, recordIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Build the notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID")
//                .setSmallIcon(R.drawable.ic_mic) // Make sure this drawable exists
//                .setContentTitle("New Transaction Detected")
//                .setContentText("Record your transaction by voice")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .addAction(R.drawable.ic_mic, "Record", recordPendingIntent) // Add action to start recording
//                .setAutoCancel(true); // Dismiss notification on tap
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//
//        try {
//            notificationManager.notify(1, builder.build()); // Show the notification
//        } catch (SecurityException e) {
//            Log.e("SmsReceiver", "Notification permission denied or not available.", e);
//        }
//    }
//}
