package com.together.daily

import android.Manifest
import android.app.*
import android.app.job.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import java.time.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

internal object AppAlerts {
    var foreground=false
    fun prefs(c:Context,uid:String)=c.getSharedPreferences("alerts_"+uid,Context.MODE_PRIVATE)
    fun notify(c:Context,id:Int,text:String):Boolean {
        if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return false
        val manager=c.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("daily","목표 알림",NotificationManager.IMPORTANCE_DEFAULT))
        if(!manager.areNotificationsEnabled())return false
        val open=PendingIntent.getActivity(c,0,Intent(c,MainActivity::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        manager.notify(id,Notification.Builder(c,"daily").setSmallIcon(com.together.daily.R.drawable.ic_heart).setContentTitle("쏘규 Daily 챌린지").setContentText(text).setContentIntent(open).setAutoCancel(true).build())
        return true
    }
    fun configure(c:Context,uid:String,pair:String) {
        c.getSharedPreferences("alert_session",Context.MODE_PRIVATE).edit().putString("uid",uid).putString("pair",pair).apply()
        c.getSystemService(JobScheduler::class.java).cancel(700)
        scheduleReminder(c,uid)
    }
    fun scheduleReminder(c:Context,uid:String) {
        val p=prefs(c,uid)
        val alarm=c.getSystemService(AlarmManager::class.java)
        val intent=PendingIntent.getBroadcast(c,701,Intent(c,DailyReminder::class.java).putExtra("uid",uid),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        alarm.cancel(intent)
        if(!p.getBoolean("reminder",false))return
        val zone=ZoneId.of("Asia/Seoul")
        val now=ZonedDateTime.now(zone)
        var next=now.withHour(p.getInt("hour",21)).withMinute(p.getInt("minute",0)).withSecond(0).withNano(0)
        if(!next.isAfter(now))next=next.plusDays(1)
        alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),intent)
    }
    fun partnerRecorded(c:Context,uid:String,partner:String,name:String,complete:Boolean) {
        val p=prefs(c,uid)
        val key="records_sent_"+partner+"_"+koreaToday()
        if(foreground&&p.getBoolean("partnerRecorded",true)&&complete&&!p.getBoolean(key,false)&&notify(c,702,"${name}님이 오늘 목표의 완료·미완료를 모두 기록했어요."))p.edit().putBoolean(key,true).apply()
    }
    fun stop(c:Context) {
        c.getSystemService(JobScheduler::class.java).cancel(700)
        val alarm=PendingIntent.getBroadcast(c,701,Intent(c,DailyReminder::class.java),PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
        if(alarm!=null)c.getSystemService(AlarmManager::class.java).cancel(alarm)
        c.getSharedPreferences("alert_session",Context.MODE_PRIVATE).edit().clear().apply()
    }
}

class DailyReminder:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        val uid=intent.getStringExtra("uid")?:return
        if(context.getSharedPreferences("alert_session",Context.MODE_PRIVATE).getString("uid",null)!=uid)return
        if(AppAlerts.prefs(context,uid).getBoolean("reminder",false))AppAlerts.notify(context,701,"오늘 목표를 확인하고 완료 여부를 기록해주세요.")
        AppAlerts.scheduleReminder(context,uid)
    }
}
class AlertBootReceiver:BroadcastReceiver() {
    override fun onReceive(context:Context,intent:Intent) {
        val p=context.getSharedPreferences("alert_session",Context.MODE_PRIVATE)
        val uid=p.getString("uid",null)?:return
        AppAlerts.configure(context,uid,p.getString("pair","").orEmpty())
    }
}

internal suspend fun fetchDailyRecords(context:Context,uid:String,pairId:String):Triple<Map<String,String>,List<Goal>,List<Entry>> {
    FirebaseApp.initializeApp(context)
    require(FirebaseAuth.getInstance().currentUser?.uid==uid)
    val pair=FirebaseFirestore.getInstance().collection("pairs").document(pairId)
    val names=(pair.get(Source.SERVER).await().get("names") as? Map<*,*>)?.entries?.associate {it.key.toString() to it.value.toString()}.orEmpty()
    val goals=pair.collection("goals").get(Source.SERVER).await().documents.mapNotNull {doc ->
        runCatching {Goal(doc.id,doc.getString("ownerId")!!,doc.getString("title")!!,LocalDate.parse(doc.getString("start")),doc.getString("end")?.let {LocalDate.parse(it)})}.getOrNull()
    }
    val entries=pair.collection("entries").whereEqualTo("date",koreaToday().toString()).get(Source.SERVER).await().documents.mapNotNull {doc ->
        runCatching {Entry(doc.getString("goalId")!!,LocalDate.parse(doc.getString("date")),doc.getBoolean("done")!!)}.getOrNull()
    }
    return Triple(names,goals,entries)
}
