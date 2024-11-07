package com.example.transactionmanagerapp;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceRecordingService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "START_RECORDING".equals(intent.getAction())) {
            String amount = intent.getStringExtra("amount");  // Retrieve amount from intent
            startForeground(1, createNotification(amount).build());
            startVoiceRecording(amount);
        }
        return START_STICKY;
    }

    // Create a notification indicating the recording process
    private NotificationCompat.Builder createNotification(String amount) {
        return new NotificationCompat.Builder(this, "CHANNEL_ID")
                .setContentTitle("Recording Transaction")
                .setContentText("Amount: ₹" + amount + ". Please speak the category.")
                .setSmallIcon(R.drawable.ic_mic);
    }

    // Start voice recording to recognize the category
    private void startVoiceRecording(String amount) {
        try {
            SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String spokenCategory = matches.get(0);
                        processVoiceInput(spokenCategory, amount);  // Process the category and amount
                    }
                }

                @Override public void onReadyForSpeech(Bundle params) {}
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) {}
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });

            speechRecognizer.startListening(recognizerIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Process the voice input (category) and the extracted amount from SMS
    private void processVoiceInput(String spokenCategory, String amount) {

        SharedPreferences prefs = getSharedPreferences("VoiceData", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Store the recognized category and amount
        editor.putString("lastCategory", spokenCategory);
        editor.putString("lastAmount", amount);
        editor.apply();

        Log.d("VoiceRecordingService", "Category: " + spokenCategory + ", Amount: ₹" + amount);

        // Save the transaction details in SharedPreferences or a database
        String existingTransactions = prefs.getString("transactions", "");
        String newTransaction = "Category: " + spokenCategory + ", Amount: ₹" + amount;

        editor.putString("transactions", existingTransactions + "\n" + newTransaction);
        editor.apply();

        // You could also send a broadcast or notify your app’s UI of the new transaction
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}











//package com.example.transactionmanagerapp;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.speech.SpeechRecognizer;
//import android.speech.RecognizerIntent;
//import android.speech.RecognitionListener;
//import android.util.Log;
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationCompat;
//import java.util.ArrayList;
//import java.util.Locale;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class VoiceRecordingService extends Service {
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        if (intent != null && "START_RECORDING".equals(intent.getAction())) {
//            startForeground(1, createNotification().build());
//            startVoiceRecording();
//        }
//        return START_STICKY;
//    }
//
//    private NotificationCompat.Builder createNotification() {
//        return new NotificationCompat.Builder(this, "CHANNEL_ID")
//                .setContentTitle("Recording Transaction")
//                .setContentText("Please speak your transaction details.")
//                .setSmallIcon(R.drawable.ic_mic);
//    }
//    private void startVoiceRecording() {
//        try {
//            SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
//            Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//
//            speechRecognizer.setRecognitionListener(new RecognitionListener() {
//                @Override
//                public void onResults(Bundle results) {
//                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                    if (matches != null && !matches.isEmpty()) {
//                        String spokenText = matches.get(0);
//                        processVoiceInput(spokenText);
//                    }
//                }
//
//                @Override public void onReadyForSpeech(Bundle params) {}
//                @Override public void onBeginningOfSpeech() {}
//                @Override public void onRmsChanged(float rmsdB) {}
//                @Override public void onBufferReceived(byte[] buffer) {}
//                @Override public void onEndOfSpeech() {}
//                @Override public void onError(int error) {}
//                @Override public void onPartialResults(Bundle partialResults) {}
//                @Override public void onEvent(int eventType, Bundle params) {}
//            });
//
//            speechRecognizer.startListening(recognizerIntent);
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//    private void processVoiceInput(String spokenText) {
//
//        getSharedPreferences("VoiceData", MODE_PRIVATE)
//                .edit()
//                .putString("lastSpokenText", spokenText)
//                .apply();
//
//
//        Pattern pattern = Pattern.compile("(\\w+)\\s(\\d+)");
//        Matcher matcher = pattern.matcher(spokenText);
//
//        if (matcher.find()) {
//            String category = matcher.group(1);
//            String amount = matcher.group(2);
//
//            getSharedPreferences("VoiceData", MODE_PRIVATE)
//                    .edit()
//                    .putString("lastCategory", category)
//                    .putString("lastAmount", amount)
//                    .apply();
//
//            Log.d("VoiceRecordingService", "Category: " + category + ", Amount: " + amount);
//
//            SharedPreferences prefs = getSharedPreferences("VoiceData", MODE_PRIVATE);
//            String existingTransactions = prefs.getString("transactions", "");
//            String newTransactions = existingTransactions + "\n" + category + ": " + amount;
//
//            prefs.edit()
//                    .putString("transactions", newTransactions)
//                    .apply();
//            // Save the transaction in your app's database or send a broadcast to update the UI or data
//        }
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}