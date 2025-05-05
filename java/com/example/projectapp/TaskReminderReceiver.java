package com.example.projectapp;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class TaskReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "TaskReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("task_id", -1);
        String taskText = intent.getStringExtra("task_text");

        if (taskId != -1 && taskText != null) {
            Log.d(TAG, "Showing notification for task: " + taskText);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Task Reminder")
                    .setContentText(taskText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{100, 200, 300, 400, 500});

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(taskId, builder.build());
        }
    }
} 