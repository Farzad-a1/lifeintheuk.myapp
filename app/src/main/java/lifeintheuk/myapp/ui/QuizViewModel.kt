package lifeintheuk.myapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import lifeintheuk.myapp.data.Progress
import lifeintheuk.myapp.data.ProgressRepository
import lifeintheuk.myapp.data.Question
import lifeintheuk.myapp.data.QuestionLoader
import lifeintheuk.myapp.domain.QuizLogic
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

enum class Screen { HOME, PRACTICE, REVIEW, PROGRESS, SETTINGS, MOCK, MOCK_RESULTS }
data class UiState(
    val screen: Screen = Screen.HOME,
    val questions: List<Question> = emptyList(),
    val dataError: String? = null,
    val progress: Progress = Progress(),
    val practiceIds: List<String> = emptyList(),
    val practiceIndex: Int = 0,
    val practiceAnswer: Int? = null,
    val practiceCorrect: Boolean? = null,
    val mockAnswers: Map<String, Int> = emptyMap(),
    val mockIndex: Int = 0,
    val mockRemainingSeconds: Int = 2700,
    val mockSubmitted: Boolean = false,
)

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProgressRepository(application)
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        val result = QuestionLoader.load(application)
        _state.update { it.copy(questions = result.getOrDefault(emptyList()), dataError = result.exceptionOrNull()?.message) }
        viewModelScope.launch { repository.progress.collectLatest { p -> _state.update { it.copy(progress = p) } } }
    }

    fun go(screen: Screen) { _state.update { it.copy(screen = screen) } }
    fun startPractice(review: Boolean = false) {
        val (_, questions, _, progress) = _state.value
        val ids = if (review) {
            questions.asSequence().map { it.id }.filter { it in progress.incorrectIds }.toList()
        } else {
            questions.map { it.id }
        }
        _state.update { it.copy(screen = if (review) Screen.REVIEW else Screen.PRACTICE, practiceIds = ids, practiceIndex = 0, practiceAnswer = null, practiceCorrect = null) }
    }
    fun choosePractice(index: Int) {
        val s = _state.value
        if (s.practiceAnswer != null) return
        val q = s.questions.firstOrNull { it.id == s.practiceIds.getOrNull(s.practiceIndex) } ?: return
        val correct = QuizLogic.isCorrect(q, index)
        _state.update { it.copy(practiceAnswer = index, practiceCorrect = correct) }
        viewModelScope.launch { repository.recordPractice(q.id, correct) }
    }
    fun nextPractice() {
        val s = _state.value
        _state.update { it.copy(practiceIndex = (s.practiceIndex + 1).coerceAtMost((s.practiceIds.size - 1).coerceAtLeast(0)), practiceAnswer = null, practiceCorrect = null) }
    }
    fun startMock() {
        val qs = _state.value.questions.shuffled().take(24)
        if (qs.size < 24) return
        _state.update { it.copy(screen = Screen.MOCK, mockAnswers = emptyMap(), mockIndex = 0, mockRemainingSeconds = 2700, mockSubmitted = false, practiceIds = qs.map { q -> q.id }) }
        viewModelScope.launch {
            while ((_state.value.screen == Screen.MOCK) && !_state.value.mockSubmitted && (_state.value.mockRemainingSeconds > 0)) {
                delay(1.seconds)
                _state.update { it.copy(mockRemainingSeconds = (it.mockRemainingSeconds - 1).coerceAtLeast(0)) }
            }
            if (_state.value.screen == Screen.MOCK && _state.value.mockRemainingSeconds == 0) submitMock()
        }
    }
    fun selectMock(index: Int) {
        val s = _state.value; val id = s.practiceIds.getOrNull(s.mockIndex) ?: return
        if (!s.mockSubmitted) _state.update { it.copy(mockAnswers = s.mockAnswers + (id to index)) }
    }
    fun navigateMock(index: Int) { _state.update { it.copy(mockIndex = index.coerceIn(0, 23)) } }
    fun submitMock() {
        val s = _state.value
        if (s.mockSubmitted) return
        val qs = s.practiceIds.mapNotNull { id -> s.questions.find { it.id == id } }
        val score = QuizLogic.score(qs, s.mockAnswers)
        _state.update { it.copy(screen = Screen.MOCK_RESULTS, mockSubmitted = true) }
        viewModelScope.launch { repository.recordMock(qs, s.mockAnswers, score) }
    }
    fun resetProgress() { viewModelScope.launch { repository.reset() } }
    fun question(id: String?): Question? = _state.value.questions.firstOrNull { it.id == id }
    fun mockQuestions(): List<Question> = _state.value.practiceIds.mapNotNull { id -> _state.value.questions.find { it.id == id } }
}
