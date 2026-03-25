package com.chirag.mello.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.chirag.mello.MainActivity
import com.chirag.mello.R
import java.util.concurrent.TimeUnit
import java.util.Calendar

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        showNotification(applicationContext)
        schedule(applicationContext) // Reschedule for tomorrow
        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "mello_reminder"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "MelloDailyReminder"

        fun showNotification(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Daily Reminder",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "Reminds you to log your day" }
                manager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val largeIcon = BitmapFactory.decodeResource(context.resources, R.drawable.icon)
            
            // Optionally scale icon to system preferred dimensions for large icons
            val width = context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width)
            val height = context.resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height)
            val scaledLargeIcon = android.graphics.Bitmap.createScaledBitmap(largeIcon, width, height, true)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setLargeIcon(scaledLargeIcon)
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(largeIcon)
                    .bigLargeIcon(null as android.graphics.Bitmap?))
                .setContentTitle("Hey, Mello here 🌙")
                .setContentText("Capture your day in one line 🌙")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            manager.notify(NOTIFICATION_ID, notification)
        }

        fun schedule(context: Context) {
            val prefs = context.getSharedPreferences("mello_prefs", Context.MODE_PRIVATE)
            val hour = prefs.getInt("reminder_hour", 21)
            val minute = prefs.getInt("reminder_minute", 0)

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val delay = target.timeInMillis - now.timeInMillis

            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
