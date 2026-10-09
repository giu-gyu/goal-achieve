package com.together.daily

import android.Manifest
import android.app.*
import android.app.job.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.time.*

internal object AppAlerts {
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
        val p=prefs(c,uid)
        val jobs=c.getSystemService(JobScheduler::class.java)
        jobs.cancel(700)
        if(p.getBoolean("partner",false)&&uid!="me") {
            val extras=PersistableBundle().apply {putString("uid",uid);putString("pair",pair)}
            jobs.schedule(JobInfo.Builder(700,ComponentName(c,PartnerAlertJob::class.java)).setExtras(extras).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(15*60*1000L).setPersisted(true).build())
        }
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
    fun partnerCompleted(c:Context,uid:String,partner:String,name:String,complete:Boolean) {
        val p=prefs(c,uid)
        val key="sent_"+partner+"_"+koreaToday()
        if(p.getBoolean("partner",false)&&complete&&!p.getBoolean(key,false)&&notify(c,702,"${name}님이 오늘 목표를 모두 완료했어요! 🎉"))p.edit().putBoolean(key,true).apply()
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
class PartnerAlertJob:JobService() {
    private var task:Job?=null
    override fun onStartJob(params:JobParameters):Boolean {
        task=CoroutineScope(Dispatchers.Main+SupervisorJob()).launch {
            var retry=false
            try {
                withTimeout(20000) {
                    FirebaseApp.initializeApp(this@PartnerAlertJob)
                    val uid=params.extras.getString("uid")?:return@withTimeout
                    if(FirebaseAuth.getInstance().currentUser?.uid!=uid)return@withTimeout
                    val pair=FirebaseFirestore.getInstance().collection("pairs").document(params.extras.getString("pair")!!)
                    val names=pair.get(Source.SERVER).await().get("names") as? Map<*,*> ?:return@withTimeout
                    val partner=names.keys.filterIsInstance<String>().firstOrNull {it!=uid}?:return@withTimeout
                    val goals=pair.collection("goals").get(Source.SERVER).await().documents.filter {it.getString("ownerId")==partner&&LocalDate.parse(it.getString("start"))<=koreaToday()&&(it.getString("end")==null||LocalDate.parse(it.getString("end"))>=koreaToday())}
                    val entries=pair.collection("entries").whereEqualTo("date",koreaToday().toString()).get(Source.SERVER).await().documents
                    AppAlerts.partnerCompleted(this@PartnerAlertJob,uid,partner,names[partner].toString(),goals.isNotEmpty()&&goals.all {g->entries.any {it.getString("goalId")==g.id&&it.getBoolean("done")==true}})
                }
            }catch(e:CancellationException){throw e}catch(e:Exception){retry=true}
            jobFinished(params,retry)
        }
        return true
    }
    override fun onStopJob(params:JobParameters):Boolean {task?.cancel();return true}
}
