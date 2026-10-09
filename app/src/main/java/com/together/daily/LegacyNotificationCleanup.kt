package com.together.daily

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Upgrade cleanup only; this app no longer schedules or sends notifications.
internal object LegacyNotificationCleanup {
    fun cancel(context:Context) {
        context.getSystemService(JobScheduler::class.java).cancel(700)
        val pending=PendingIntent.getBroadcast(context,701,
            Intent().setClassName(context,"com.together.daily.DailyReminder"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
        if(pending!=null) {
            context.getSystemService(AlarmManager::class.java).cancel(pending)
            pending.cancel()
        }
        val manager=context.getSystemService(NotificationManager::class.java)
        listOf(701,702,703).forEach {manager.cancel(it)}
        manager.deleteNotificationChannel("daily")
        val session=context.getSharedPreferences("alert_session",Context.MODE_PRIVATE)
        session.getString("uid",null)?.let {context.getSharedPreferences("alerts_"+it,Context.MODE_PRIVATE).edit().clear().apply()}
        session.edit().clear().apply()
    }
}
class NotificationUpgradeCleanupReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        LegacyNotificationCleanup.cancel(context)
    }
}
