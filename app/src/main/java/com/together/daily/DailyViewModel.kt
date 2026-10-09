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
    var entries by mutableStateOf<List<Entry>>(emptyList()); private set
    var busy by mutableStateOf(false); private set
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var offline by mutableStateOf(false); private set
    private val listeners = mutableListOf<ListenerRegistration>()
    init { if (uid.isNotEmpty()) loadProfile() }
    fun clearError() { error = null }
    private fun work(block: suspend () -> Unit) {
        if (busy) return
        viewModelScope.launch {
            busy = true; error = null
            try { withTimeout(20_000) { block() } } catch (e: Exception) {
                error = if (e is TimeoutCancellationException) "연결 응답이 늦어지고 있어요. 기록 저장을 요청했다면 연결 복구 후 반영될 수 있어요. 표시된 기록을 확인해주세요." else when ((e as? FirebaseAuthException)?.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "이메일 주소를 확인해주세요."
                    "ERROR_WEAK_PASSWORD" -> "비밀번호는 6자 이상이어야 해요."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "이미 가입한 이메일이에요. 로그인해주세요."
                    "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> "이메일 또는 비밀번호를 확인해주세요."
                    else -> e.localizedMessage ?: "연결에 실패했어요. 인터넷 연결을 확인하고 다시 시도해주세요."
                }
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
        listeners += pair.collection("goals").addSnapshotListener { snapshot, e ->
            if (e != null) { error = "목표를 불러오지 못했어요. 연결을 확인해주세요."; return@addSnapshotListener }
            goals = snapshot?.documents?.mapNotNull { doc ->
                runCatching { Goal(doc.id, doc.getString("ownerId")!!, doc.getString("title")!!,
                    LocalDate.parse(doc.getString("start")), doc.getString("end")?.let { LocalDate.parse(it) }) }.getOrNull()
            }?.sortedWith(compareBy<Goal> { it.start }.thenBy { it.title }).orEmpty()
        }
        listeners += pair.collection("entries").addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, e ->
            if (e != null) { error = "기록을 불러오지 못했어요. 연결을 확인해주세요."; return@addSnapshotListener }
            offline = snapshot?.metadata?.isFromCache == true
            entries = snapshot?.documents?.mapNotNull { doc ->
                runCatching { Entry(doc.getString("goalId")!!, LocalDate.parse(doc.getString("date")), doc.getBoolean("done")!!) }.getOrNull()
            }.orEmpty()
        }
    }
    fun addGoal(title: String) = work {
        require(title.trim().length in 1..60) { "목표는 1~60자로 입력해주세요." }
        if (demo) {
            goals = goals + Goal(UUID.randomUUID().toString(), uid, title.trim(), koreaToday())
            return@work
        }
        db!!.collection("pairs").document(pairId).collection("goals").add(mapOf(
            "ownerId" to uid, "title" to title.trim(), "start" to koreaToday().toString(), "end" to null
        )).await()
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
    fun record(goal: Goal, date: LocalDate, done: Boolean?) = work {
        require(goal.ownerId == uid && goal.scheduled(date) && date <= koreaToday())
        if (demo) {
            entries = entries.filterNot { it.goalId == goal.id && it.date == date } +
                (if (done == null) emptyList() else listOf(Entry(goal.id, date, done)))
            return@work
        }
        val ref = db!!.collection("pairs").document(pairId).collection("entries").document(goal.id + "_" + date)
        if (done == null) ref.delete().await()
        else ref.set(mapOf("ownerId" to uid, "goalId" to goal.id, "date" to date.toString(), "done" to done)).await()
    }
    fun logout() {
        if (busy) return
        listeners.forEach { it.remove() }; listeners.clear()
        auth?.signOut(); demo = false; uid = ""; pairId = ""; names = emptyMap(); goals = emptyList(); entries = emptyList()
        ready = false; offline = false; error = null
    }
    fun startDemo() {
        demo = true; uid = "me"; pairId = "demo"; ready = true
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
