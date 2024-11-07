package com.example.transactionmanagerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Start the service again after the device is rebooted
//            Intent serviceIntent = new Intent(context, SmsDetectionService.class);
//            context.startForegroundService(serviceIntent);
            Intent serviceIntent1 = new Intent(context, SmsReceiver.class);
            context.startForegroundService(serviceIntent1);
        }
        else{
        Intent serviceIntent = new Intent(context, SmsReceiver.class);
        context.startForegroundService(serviceIntent);}
    }
}
