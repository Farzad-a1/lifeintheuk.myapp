package lifeintheuk.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.progressStore by preferencesDataStore(name = "study_progress")

class ProgressRepository(private val context: Context) {
    private val key = stringPreferencesKey("progress_v1")

    val progress: Flow<Progress> = context.progressStore.data.map { prefs -> decode(prefs[key].orEmpty()) }

    suspend fun recordPractice(questionId: String, correct: Boolean) {
        context.progressStore.edit { prefs ->
            val before = decode(prefs[key].orEmpty())
            prefs[key] = encode(
                before.copy(
                    answered = before.answered + 1,
                    correct = before.correct + if (correct) 1 else 0,
                    incorrectIds = if (correct) before.incorrectIds - questionId else before.incorrectIds + questionId,
                )
            )
        }
    }

    suspend fun recordMock(questions: List<Question>, answers: Map<String, Int>, score: Int) {
        context.progressStore.edit { prefs ->
            val before = decode(prefs[key].orEmpty())
            val missed = questions
                .filter { q -> answers[q.id]?.let { it != q.correctAnswer } == true }
                .map { it.id }
                .toSet()
            prefs[key] = encode(
                before.copy(
                    answered = before.answered + questions.size,
                    correct = before.correct + score,
                    incorrectIds = (before.incorrectIds + missed) - questions.filter { answers[it.id] == it.correctAnswer }.map { it.id }.toSet(),
                    mockScores = (before.mockScores + score).takeLast(20),
                ),
            )
        }
    }

    suspend fun reset() { context.progressStore.edit { it.clear() } }

    private fun encode(p: Progress) = JSONObject()
        .put("answered", p.answered).put("correct", p.correct)
        .put("incorrectIds", JSONArray(p.incorrectIds.toList()))
        .put("mockScores", JSONArray(p.mockScores)).toString()

    private fun decode(raw: String): Progress = runCatching {
        if (raw.isBlank()) return Progress()
        val o = JSONObject(raw)
        val ids = o.optJSONArray("incorrectIds") ?: JSONArray()
        val scores = o.optJSONArray("mockScores") ?: JSONArray()
        Progress(
            o.optInt("answered").coerceAtLeast(0),
            o.optInt("correct").coerceAtLeast(0),
            (0 until ids.length()).map { ids.getString(it) }.toSet(),
            (0 until scores.length()).map { scores.getInt(it).coerceIn(0, 24) },
        )
    }.getOrDefault(Progress())
}
