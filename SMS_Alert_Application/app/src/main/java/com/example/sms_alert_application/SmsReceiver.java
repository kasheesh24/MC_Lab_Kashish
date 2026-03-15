package com.example.sms_alert_application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import androidx.core.app.NotificationCompat;

public class SmsReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "SMS_ALERT_CHANNEL";
    public static final String ACTION_SIMULATE = "com.example.sms_alert_application.SIMULATE_SMS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null) {
                for (SmsMessage sms : messages) {
                    String sender = sms.getOriginatingAddress();
                    String messageBody = sms.getMessageBody();
                    showNotification(context, sender, messageBody);
                }
            }
        } else if (ACTION_SIMULATE.equals(intent.getAction())) {
            showNotification(context, "SIMULATOR", "This is a simulated SMS message for testing.");
        }
    }

    private void showNotification(Context context, String sender, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "SMS Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("New SMS from: " + sender)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}