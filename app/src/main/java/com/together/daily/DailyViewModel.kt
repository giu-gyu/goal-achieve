package com.together.daily

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

fun koreaToday(): LocalDate = LocalDate.now(ZoneId.of("Asia/Seoul"))

class DailyViewModel(application: Application) : AndroidViewModel(application) {
    val configured = FirebaseApp.initializeApp(application) != null
    private val auth: FirebaseAuth? = if (configured) FirebaseAuth.getInstance() else null
    private val db: FirebaseFirestore? = if (configured) FirebaseFirestore.getInstance() else null
    var demo by mutableStateOf(false); private set
    var uid by mutableStateOf(auth?.currentUser?.uid.orEmpty()); private set
    var pairId by mutableStateOf(""); private set
    var names by mutableStateOf<Map<String, String>>(emptyMap()); private set
    var goals by mutableStateOf<List<Goal>>(emptyList()); private set
    var dateSchedules by mutableStateOf<Map<LocalDate,GoalSchedule>>(emptyMap()); private set
    fun scheduleFor(date:LocalDate)=scheduleForDate(date,dateSchedules)
    fun memberLabel(id:String)="${names[id].orEmpty()} ${memberEmoji(profiles[id]?:MemberProfile())}"
    var entries by mutableStateOf<List<Entry>>(emptyList()); private set
    var memos by mutableStateOf<List<DailyMemo>>(emptyList()); private set
    var profiles by mutableStateOf<Map<String,MemberProfile>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var memoStatus by mutableStateOf<String?>(null); private set
    var offline by mutableStateOf(false); private set
    private val listeners = mutableListOf<ListenerRegistration>()
    init { if (uid.isNotEmpty()) loadProfile() }
    fun clearError() { error = null }
    private fun work(onFailure: (String) -> Unit = {}, block: suspend () -> Unit) {
        if (busy) return
        busy = true; error = null
        viewModelScope.launch {
            try { withTimeout(20_000) { block() } } catch (e: Exception) {
                error = if (e is TimeoutCancellationException) "연결 응답이 늦어지고 있어요. 기록 저장을 요청했다면 연결 복구 후 반영될 수 있어요. 표시된 기록을 확인해주세요." else when ((e as? FirebaseAuthException)?.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "이메일 주소를 확인해주세요."
                    "ERROR_WEAK_PASSWORD" -> "비밀번호는 6자 이상이어야 해요."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "이미 가입한 이메일이에요. 로그인해주세요."
                    "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> "이메일 또는 비밀번호를 확인해주세요."
                    else -> e.localizedMessage ?: "연결에 실패했어요. 인터넷 연결을 확인하고 다시 시도해주세요."
                }
                onFailure(error ?: "저장하지 못했어요.")
            } finally { busy = false }
        }
    }
    fun login(email: String, password: String, signup: Boolean) = work {
        val result = if (signup) auth!!.createUserWithEmailAndPassword(email.trim(), password).await()
            else auth!!.signInWithEmailAndPassword(email.trim(), password).await()
        uid = result.user!!.uid
        loadProfileData()
    }
    fun resetPassword(email: String) = work {
        require(email.isNotBlank()) { "이메일을 먼저 입력해주세요." }
        auth!!.sendPasswordResetEmail(email.trim()).await()
        error = "비밀번호 재설정 메일을 보냈어요. 스팸함도 확인해주세요."
    }
    fun retryProfile() { loadProfile() }
    private fun loadProfile() = work { loadProfileData() }
    private suspend fun loadProfileData() {
        ready = false
        val profile = db!!.collection("users").document(uid).get().await()
        val id = profile.getString("pairId")
        if (id != null) observe(id)
        ready = true
    }
    fun connect(name: String, code: String?) = work {
        require(name.trim().length in 1..20) { "이름은 1~20자로 입력해주세요." }
        val id = code?.trim()?.lowercase() ?: UUID.randomUUID().toString().replace("-", "")
        require(id.matches(Regex("[0-9a-f]{32}"))) { "초대 코드 32자리를 확인해주세요." }
        val pair = db!!.collection("pairs").document(id)
        val profile = db.collection("users").document(uid)
        db.runTransaction { tx ->
            val existingProfile = tx.get(profile)
            require(!existingProfile.exists()) { "이미 연결된 공간이 있어요. 다시 로그인해주세요." }
            val snapshot = tx.get(pair)
            val members = (snapshot.get("members") as? List<*>)?.filterIsInstance<String>().orEmpty()
            if (code == null) {
                require(!snapshot.exists()) { "코드 생성에 실패했어요. 다시 시도해주세요." }
                tx.set(pair, mapOf("members" to listOf(uid), "names" to mapOf(uid to name.trim())))
            } else {
                require(snapshot.exists()) { "초대 코드를 찾을 수 없어요." }
                require(uid in members || members.size < 2) { "이미 두 명이 참여한 공간이에요." }
                if (uid !in members) {
                    val oldNames = (snapshot.get("names") as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() }.orEmpty()
                    tx.update(pair, mapOf("members" to members + uid, "names" to oldNames + (uid to name.trim())))
                }
            }
            tx.set(profile, mapOf("pairId" to id))
        }.await()
        observe(id)
        ready = true
    }
    private fun observe(id: String) {
        listeners.forEach { it.remove() }; listeners.clear()
        pairId = id
        val pair = db!!.collection("pairs").document(id)
        listeners += pair.addSnapshotListener { snapshot, e ->
            if (e != null) { error = "커플 정보를 불러오지 못했어요."; return@addSnapshotListener }
            names = (snapshot?.get("names") as? Map<*, *>)?.entries?.associate { it.key.toString() to it.value.toString() }.orEmpty()
        }
        listeners += pair.collection("memos").addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot,e ->
            if(e!=null) {error="메모를 불러오지 못했습니다. Firebase 규칙을 확인하세요.";return@addSnapshotListener}
            if(snapshot?.metadata?.hasPendingWrites()==true) return@addSnapshotListener
            memos=snapshot?.documents?.mapNotNull {doc -> runCatching {DailyMemo(doc.getString("ownerId")!!,LocalDate.parse(doc.getString("date")),doc.getString("text")!!)}.getOrNull()}.orEmpty()
        }
        listeners += pair.collection("profiles").addSnapshotListener {snapshot,e ->
            if(e!=null) {error="프로필을 불러오지 못했습니다. Firebase 규칙을 확인하세요.";return@addSnapshotListener}
            profiles=snapshot?.documents?.associate {it.id to MemberProfile(it.getString("bio").orEmpty(),it.getString("resolve").orEmpty(),it.getString("gender")?.let {value->runCatching {ProfileGender.valueOf(value)}.getOrNull()},it.getLong("emojiStyle")?.toInt()?.takeIf {index->index in MemberEmojiStyles.indices}?:0)}.orEmpty()
        }
        listeners += pair.collection("daySchedules").addSnapshotListener {snapshot,e ->
            if(e!=null) {error="날짜 분류를 불러오지 못했어요.";return@addSnapshotListener}
            dateSchedules=snapshot?.documents?.mapNotNull {doc -> runCatching {
                LocalDate.parse(doc.id) to GoalSchedule.valueOf(doc.getString("schedule")!!)
            }.getOrNull() }?.toMap().orEmpty()
            goals=goals.map {it.copy(dateSchedules=dateSchedules)}
        }
        listeners += pair.collection("goals").addSnapshotListener { snapshot, e ->
            if (e != null) { error = "목표를 불러오지 못했어요. 연결을 확인해주세요."; return@addSnapshotListener }
            goals = snapshot?.documents?.mapNotNull { doc ->
                runCatching { Goal(doc.id, doc.getString("ownerId")!!, doc.getString("title")!!,
                    LocalDate.parse(doc.getString("start")), doc.getString("end")?.let { LocalDate.parse(it) },doc.getLong("order")?.toInt()?:Int.MAX_VALUE,
                    doc.getString("schedule")?.let { runCatching { GoalSchedule.valueOf(it) }.getOrNull() } ?: GoalSchedule.WEEKDAY,
                    doc.getLong("weeklyTarget")?.toInt()?.takeIf { it in 1..7 },dateSchedules=dateSchedules,colorIndex=doc.getLong("colorIndex")?.toInt()?.takeIf {it in GoalPalettes.indices}) }.getOrNull()
            }?.sortedWith(compareBy<Goal> { it.order }.thenBy { it.start }.thenBy { it.title }).orEmpty()

        }
        listeners += pair.collection("entries").addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) { error = "기록을 불러오지 못했어요. 연결을 확인해주세요."; return@addSnapshotListener }
            offline = snapshot?.metadata?.isFromCache == true
            entries = snapshot?.documents?.mapNotNull { doc ->
                runCatching { Entry(doc.getString("goalId")!!, LocalDate.parse(doc.getString("date")), doc.getBoolean("done")!!) }.getOrNull()
            }.orEmpty()

        }
    }
    private val memoDraftStore = application.getSharedPreferences("memo_drafts", android.content.Context.MODE_PRIVATE)
    fun memoDraft(date:LocalDate):String? = memoDraftStore.getString(uid+"_"+date,null)
    fun editMemoDraft(date:LocalDate,text:String) {
        memoDraftStore.edit().putString(uid+"_"+date,text).apply()
        if(!busy) memoStatus=null
    }
    fun saveMemo(date:LocalDate,text:String,onSaved:()->Unit = {}) = work(onFailure={ message ->
        memoStatus = if (message.contains("PERMISSION_DENIED", ignoreCase=true) || message.contains("permission", ignoreCase=true))
            "저장 권한이 거부됐어요. Firebase에 최신 규칙이 게시됐는지 확인해주세요. 입력한 메모는 유지됩니다."
        else "서버 저장을 확인하지 못했어요. 입력한 메모는 보관했습니다. 연결 복구 후 기록을 확인해주세요."
    }) {
        require(text.length<=2000 && date<=koreaToday()) { "메모는 오늘까지, 2000자 이하로 입력해주세요." }
        memoStatus = "메모 저장 중…"
        val clean = text.trim()
        if (!demo) {
            require(uid.isNotBlank() && pairId.isNotBlank()) { "로그인과 커플 연결을 확인해주세요." }
            val ref=db!!.collection("pairs").document(pairId).collection("memos").document(uid+"_"+date)
            if(clean.isBlank()) ref.delete().await()
            else ref.set(mapOf("ownerId" to uid,"date" to date.toString(),"text" to clean)).await()
        }
        memos=memos.filterNot {it.ownerId==uid&&it.date==date} +
            if(clean.isBlank()) emptyList() else listOf(DailyMemo(uid,date,clean))
        memoStatus = if(clean.isBlank()) "메모를 삭제했어요." else "메모를 저장했어요."
        if(memoDraft(date)==text) memoDraftStore.edit().remove(uid+"_"+date).apply()
        onSaved()
    }
    fun setDaySchedule(date:LocalDate,schedule:GoalSchedule)=work {
        require(date==koreaToday()) {"오늘의 분류만 변경할 수 있어요."}
        if(!demo) db!!.collection("pairs").document(pairId).collection("daySchedules").document(date.toString())
            .set(mapOf("schedule" to schedule.name,"ownerId" to uid)).await()
        dateSchedules=dateSchedules+(date to schedule)
        goals=goals.map {it.copy(dateSchedules=dateSchedules)}
    }
    fun saveProfile(name:String,bio:String,resolve:String,gender:ProfileGender?=profiles[uid]?.gender,emojiStyle:Int=profiles[uid]?.emojiStyle?:0)=work {
        require(name.trim().length in 1..20&&bio.length<=200&&resolve.length<=200&&emojiStyle in MemberEmojiStyles.indices)
        if(demo) {names=names+(uid to name.trim());profiles=profiles+(uid to MemberProfile(bio.trim(),resolve.trim(),gender,emojiStyle));return@work}
        val pair=db!!.collection("pairs").document(pairId)
        val batch=db.batch()
        batch.update(pair,com.google.firebase.firestore.FieldPath.of("names",uid),name.trim())
        batch.set(pair.collection("profiles").document(uid),mapOf("bio" to bio.trim(),"resolve" to resolve.trim(),"gender" to gender?.name,"emojiStyle" to emojiStyle))
        batch.commit().await()
    }
    fun addGoal(title:String,position:Int?=null,schedule:GoalSchedule=if(KoreanHolidays.isRestDay(koreaToday())) GoalSchedule.WEEKEND else GoalSchedule.WEEKDAY,weeklyTarget:Int?=null,colorIndex:Int?=null,onSaved:()->Unit={})=work {
        require(colorIndex==null||colorIndex in GoalPalettes.indices)
        require(title.trim().length in 1..60) {"목표는 1~60자로 입력해주세요."}
        require(weeklyTarget==null || weeklyTarget in 1..7) { "주당 횟수는 1~7회로 설정해주세요." }
        val own=goals.filter {it.ownerId==uid&&it.schedule==schedule}.toMutableList()
        require(goals.count {it.ownerId==uid}<450) {"목표는 최대 450개까지 등록할 수 있습니다."}
        val index=(position?:own.size).coerceIn(0,own.size)
        val ref=if(demo)null else db!!.collection("pairs").document(pairId).collection("goals").document()
        val new=Goal(ref?.id?:UUID.randomUUID().toString(),uid,title.trim(),koreaToday(),schedule=schedule,weeklyTarget=weeklyTarget,dateSchedules=dateSchedules,colorIndex=colorIndex?:own.size%GoalPalettes.size)
        own.add(index,new)
        val ordered=own.mapIndexed {i,g->g.copy(order=i)}
        if(demo) {goals=goals.filterNot {it.ownerId==uid&&it.schedule==schedule}+ordered;onSaved();return@work}
        val batch=db!!.batch()
        ordered.forEach {g ->
            if(g.id==new.id)batch.set(ref!!,mapOf("ownerId" to uid,"title" to g.title,"start" to g.start.toString(),"end" to null,"order" to g.order,"schedule" to g.schedule.name,"weeklyTarget" to g.weeklyTarget,"colorIndex" to g.colorIndex))
            else batch.update(ref!!.parent.document(g.id),"order",g.order)
        }
        batch.commit().await()
        onSaved()
    }
    fun moveGoal(goal:Goal,direction:Int)=work {
        require(goal.ownerId==uid&&direction in listOf(-1,1))
        val schedule=goal.schedule
        val own=goals.filter {it.ownerId==uid&&it.schedule==schedule}.toMutableList()
        require(own.size<=450)
        val index=own.indexOfFirst {it.id==goal.id}
        val target=index+direction
        if(index<0||target !in own.indices)return@work
        own.removeAt(index);own.add(target,goal)
        val ordered=own.mapIndexed {i,g->g.copy(order=i)}
        if(demo) {goals=goals.filterNot {it.ownerId==uid&&it.schedule==schedule}+ordered;return@work}
        val refs=db!!.collection("pairs").document(pairId).collection("goals")
        val batch=db.batch()
        ordered.forEach {batch.update(refs.document(it.id),"order",it.order)}
        batch.commit().await()
    }
    fun reorderGoals(schedule:GoalSchedule,ids:List<String>,onSaved:()->Unit) = work {
        val own=goals.filter {it.ownerId==uid&&it.schedule==schedule}
        require(ids.size==own.size&&ids.toSet()==own.map {it.id}.toSet()) {"목표 목록이 변경됐어요. 다시 순서를 설정해주세요."}
        require(ids.size<=450)
        val byId=own.associateBy {it.id}
        val ordered=ids.mapIndexed {index,id->byId.getValue(id).copy(order=index)}
        if(demo) {goals=goals.filterNot {it.ownerId==uid&&it.schedule==schedule}+ordered;onSaved();return@work}
        val refs=db!!.collection("pairs").document(pairId).collection("goals")
        val batch=db.batch()
        ordered.forEach {batch.update(refs.document(it.id),"order",it.order)}
        batch.commit().await()
        goals=goals.map {goal->ordered.firstOrNull {it.id==goal.id}?:goal}.sortedBy {it.order}
        onSaved()
    }
    fun renameGoal(goal: Goal, title: String) = work {
        require(goal.ownerId == uid)
        require(title.trim().length in 1..60) { "목표는 1~60자로 입력해주세요." }
        if (demo) {
            goals = goals.map { if (it.id == goal.id) it.copy(title = title.trim()) else it }
            return@work
        }
        db!!.collection("pairs").document(pairId).collection("goals").document(goal.id)
            .update("title", title.trim()).await()
    }
    fun updateGoal(goal:Goal,title:String,schedule:GoalSchedule,weeklyTarget:Int?,colorIndex:Int)=work {
        require(goal.ownerId==uid&&title.trim().length in 1..60)
        require(weeklyTarget==null||weeklyTarget in 1..7)
        require(colorIndex in GoalPalettes.indices)
        if(!demo) db!!.collection("pairs").document(pairId).collection("goals").document(goal.id)
            .update(mapOf("title" to title.trim(),"schedule" to schedule.name,"weeklyTarget" to weeklyTarget,"colorIndex" to colorIndex)).await()
        goals=goals.map {if(it.id==goal.id)it.copy(title=title.trim(),schedule=schedule,weeklyTarget=weeklyTarget,colorIndex=colorIndex) else it}
    }
    fun configureGoal(goal:Goal,schedule:GoalSchedule,weeklyTarget:Int?) = work {
        require(goal.ownerId==uid && (weeklyTarget==null || weeklyTarget in 1..7))
        if(demo) {
            goals=goals.map {if(it.id==goal.id) it.copy(schedule=schedule,weeklyTarget=weeklyTarget) else it}
        } else {
            db!!.collection("pairs").document(pairId).collection("goals").document(goal.id)
                .update(mapOf("schedule" to schedule.name,"weeklyTarget" to weeklyTarget)).await()
        }
    }
    fun deleteGoal(goal: Goal) = work {
        require(goal.ownerId == uid)
        if (demo) {
            goals = goals.filterNot { it.id == goal.id }
            entries = entries.filterNot { it.goalId == goal.id }
            return@work
        }
        val pair = db!!.collection("pairs").document(pairId)
        val records = pair.collection("entries").whereEqualTo("goalId", goal.id).get().await()
        records.documents.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        pair.collection("goals").document(goal.id).delete().await()
    }
    fun archive(goal: Goal) = work {
        require(goal.ownerId == uid)
        if (demo) {
            goals = goals.map { if (it.id == goal.id) it.copy(end = koreaToday()) else it }
            return@work
        }
        db!!.collection("pairs").document(pairId).collection("goals").document(goal.id)
            .update("end", koreaToday().toString()).await()
    }
    fun record(goal: Goal, date: LocalDate, done: Boolean?,onSaved:()->Unit={}) = work {
        require(goal.ownerId == uid && goal.scheduled(date) && date <= koreaToday())
        if (!demo) {
            val ref = db!!.collection("pairs").document(pairId).collection("entries").document(goal.id + "_" + date)
            if (done == null) ref.delete().await()
            else ref.set(mapOf("ownerId" to uid, "goalId" to goal.id, "date" to date.toString(), "done" to done)).await()
        }
        entries = entries.filterNot { it.goalId == goal.id && it.date == date } +
            (if (done == null) emptyList() else listOf(Entry(goal.id, date, done)))
        onSaved()
    }
    fun logout() {
        if (busy) return
        listeners.forEach { it.remove() }; listeners.clear()
        auth?.signOut(); demo = false; uid = ""; pairId = ""; names = emptyMap(); goals = emptyList(); entries = emptyList()
        dateSchedules=emptyMap(); ready = false; offline = false; error = null; memoStatus = null; memos=emptyList();profiles=emptyMap()
    }
    fun startDemo() {
        dateSchedules=emptyMap(); demo = true; uid = "me"; pairId = "demo"; ready = true
        names = mapOf("me" to "나", "partner" to "짝꿍")
        val start = koreaToday().minusDays(20)
        goals = listOf(Goal("walk", uid, "30분 걷기", start),
            Goal("read", uid, "책 10쪽 읽기", start.plusDays(3)),
            Goal("water", "partner", "물 6잔 마시기", start))
        entries = goals.flatMap { goal ->
            (0L..20L).mapNotNull { offset ->
                val date = start.plusDays(offset)
                if (!goal.scheduled(date) || offset % 5L == 0L) null
                else Entry(goal.id, date, offset % 3L != 0L)
            }
        }
    }
    override fun onCleared() { listeners.forEach { it.remove() }; super.onCleared() }
}
