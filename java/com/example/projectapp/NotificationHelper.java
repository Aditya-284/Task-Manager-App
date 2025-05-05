package com.example.projectapp;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {
    public static final String CHANNEL_ID = "task_reminders";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for task reminders";
    private static final String TAG = "NotificationHelper";
    private final Context context;
    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void scheduleTaskReminder(int taskId, String taskText, String dueDate, String dueTime) {
        try {
            // Parse date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateTimeString = dueDate + " " + dueTime;
            Date dateTime = dateFormat.parse(dateTimeString);
            if (dateTime != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateTime);
                // Don't schedule if the time has already passed
                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    Log.d(TAG, "Task time has already passed: " + taskText);
                    return;
                }
                // Create intent for the alarm
                Intent intent = new Intent(context, TaskReminderReceiver.class);
                intent.putExtra("task_id", taskId);
                intent.putExtra("task_text", taskText);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        taskId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );
                // Schedule the alarm
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );

                Log.d(TAG, "Scheduled reminder for task: " + taskText + " at " + dateTimeString);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time: " + e.getMessage());
        }
    }

    public void cancelTaskReminder(int taskId) {
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Cancelled reminder for task ID: " + taskId);
    }
} 