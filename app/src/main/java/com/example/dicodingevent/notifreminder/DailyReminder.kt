package com.example.dicodingevent.notifreminder


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.dicodingevent.R
import com.example.dicodingevent.data.local.database.AppDatabaseRoomEvent
import com.example.dicodingevent.data.repository.EventRepository
import com.example.dicodingevent.data.response.ListEventsItem
import com.example.dicodingevent.data.retrofit.ApiConfig
import java.util.concurrent.TimeUnit

class DailyReminder(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    init {
        createNotificationChannel(context)
    }

    override suspend fun doWork(): Result {
        val apiServices = ApiConfig.getApiServices()
        val favoriteEventDao = AppDatabaseRoomEvent.getDatabaseInstance(applicationContext).favoriteEventDao()
        val eventRepository = EventRepository(apiServices, favoriteEventDao)

        // get event
        val nearestEvent = eventRepository.fetchNearestActiveEvent()

        // last notification send
        nearestEvent?.let {
            if (!hasNotificationBeenSent(it)) {
                showNotification(applicationContext, it)
                markNotificationAsSent(it) // first
            }
        }

        return Result.success()
    }

    private fun hasNotificationBeenSent(event: ListEventsItem): Boolean {
        //check SharedPreferences if notification send
        val sharedPreferences = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("notification_sent_${event.id}", false)
    }

    private fun markNotificationAsSent(event: ListEventsItem) {

        val sharedPreferences = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("notification_sent_${event.id}", true).apply()
    }

    private fun showNotification(context: Context, event: ListEventsItem) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "event_channel",
                "Event Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "event_channel")
            .setSmallIcon(R.mipmap.dicoding_event_image_background)
            .setContentTitle(event.name)
            .setContentText("Dimulai pada: ${event.beginTime}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(event.id.hashCode(), builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "event_channel"
            val channelName = "Event Notifications"
            val channelDescription = "Notifikasi untuk mengingatkan event"

            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = channelDescription

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val DAILY_REMINDER_TAG = "daily_reminder_tag"

        fun setupDailyReminder(context: Context) {
            val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val isReminderEnabled = sharedPreferences.getBoolean("pref_daily_reminder", false)

            if (isReminderEnabled) {
                // Set periodic reminder
                val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyReminder>(1, TimeUnit.DAYS)
                    .addTag(DAILY_REMINDER_TAG)
                    .setInitialDelay(12, TimeUnit.HOURS)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "daily_reminder",
                    ExistingPeriodicWorkPolicy.UPDATE,
                    periodicWorkRequest
                )
                val oneTimeWorkRequest = OneTimeWorkRequestBuilder<DailyReminder>()
                    .addTag(DAILY_REMINDER_TAG)
                    .setInitialDelay(0, TimeUnit.SECONDS)
                    .build()

                WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
                resetNotificationStatus(sharedPreferences)
            }
        }

        private fun resetNotificationStatus(sharedPreferences: SharedPreferences) {
            // Reset status pengingat
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.putBoolean("pref_daily_reminder", true)
            editor.apply()
        }

        fun cancelDailyReminder(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(DAILY_REMINDER_TAG)
            WorkManager.getInstance(context).cancelUniqueWork("daily_reminder")
        }
    }
}


