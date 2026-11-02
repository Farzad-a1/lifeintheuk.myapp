package lifeintheuk.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import lifeintheuk.myapp.ui.*
import lifeintheuk.myapp.domain.QuizLogic
import androidx.compose.material3.ExperimentalMaterial3Api

private val Page = Color(0xFFEDF3F7)
private val CardSurface = Color(0xFFFFFFFF)
private val Accent = Color(0xFF416A86)
private val PrimaryText = Color(0xFF304B60)
private val SecondaryText = Color(0xFF536B7C)
private val SubtleBorder = Color(0xFFD9E3EA)
private val Success = Color(0xFF236B45)
private val SuccessContainer = Color(0xFFE8F3EC)
private val Error = Color(0xFFA12D32)
private val ErrorContainer = Color(0xFFF9E9E8)
private val SelectedContainer = Color(0xFFE2ECF2)

private val AppColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = SelectedContainer,
    onPrimaryContainer = PrimaryText,
    secondary = SecondaryText,
    onSecondary = Color.White,
    background = Page,
    onBackground = PrimaryText,
    surface = CardSurface,
    onSurface = PrimaryText,
    surfaceVariant = SelectedContainer,
    onSurfaceVariant = SecondaryText,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    error = Error,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Error,
)

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<QuizViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = AppColors) {
            val state by viewModel.state.collectAsStateWithLifecycle()
            LifeInTheUKApp(state, viewModel)
        } }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LifeInTheUKApp(s: UiState, vm: QuizViewModel) {
    var showReset by remember { mutableStateOf(value = false) }
    var showEnd by remember { mutableStateOf(value = false) }
    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Reset local progress?") },
            text = { Text("This will clear your scores, saved incorrect answers and mock test history from this device.") },
            confirmButton = { TextButton(onClick = { vm.resetProgress(); showReset = false }) { Text("Reset") } },
            dismissButton = { TextButton(onClick = { showReset = false }) { Text("Cancel") } },
        )
    }
    if (showEnd) {
        AlertDialog(
            onDismissRequest = { showEnd = false },
            title = { Text("End mock test?") },
            text = { Text("Your answers will be submitted now. You can review them afterwards.") },
            confirmButton = { TextButton(onClick = { showEnd = false; vm.submitMock() }) { Text("End test") } },
            dismissButton = { TextButton(onClick = { showEnd = false }) { Text("Continue test") } },
        )
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        if (s.screen !in listOf(Screen.HOME, Screen.MOCK, Screen.MOCK_RESULTS)) {
            TopAppBar(title = { Text("Life in the UK") }, navigationIcon = { TextButton(onClick = { vm.go(Screen.HOME) }) { Text("Home") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                ))
        }
    }) { padding ->
        when (s.screen) {
            Screen.HOME -> HomeScreen(s, vm, Modifier.padding(padding))
            Screen.PRACTICE, Screen.REVIEW -> PracticeScreen(s, vm, Modifier.padding(padding))
            Screen.PROGRESS -> ProgressScreen(s, Modifier.padding(padding))
            Screen.SETTINGS -> SettingsScreen(Modifier.padding(padding)) { showReset = true }
            Screen.MOCK -> MockScreen(s, vm, Modifier.padding(padding)) { showEnd = true }
            Screen.MOCK_RESULTS -> MockResultsScreen(s, vm, Modifier.padding(padding))
        }
    }
}

@Composable
private fun HomeScreen(s: UiState, vm: QuizViewModel, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 30.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text(stringResource(R.string.app_name), fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(8.dp))
            Text("Build your knowledge with short, independent practice sessions.", fontSize = 17.sp)
            Text("The questions are original illustrative examples, not official test questions.", modifier = Modifier.padding(top = 7.dp), fontSize = 14.sp, color = SecondaryText)
            Spacer(Modifier.height(20.dp))
            s.dataError?.let { Notice("Question data could not be loaded: $it", Error) }
            Text("Study options", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }
        item { HomeCard("Practice Questions", "Learn one question at a time", "Start practice") { vm.startPractice() } }
        item { HomeCard("Mock Test", "24 questions · 45 minutes", "Start mock test", enabled = s.questions.size >= 24) { vm.startMock() }
            if (s.questions.size < 24) Text("A full mock test becomes available when 24 unique questions are loaded.", modifier = Modifier.padding(start = 8.dp, top = 4.dp), color = SecondaryText) }
        item { HomeCard("Review Incorrect Answers", "Practise questions you missed", "Review · ${s.progress.incorrectIds.size}", enabled = s.progress.incorrectIds.isNotEmpty()) { vm.startPractice(review = true) } }
        item { HomeCard("My Progress", "See your study totals", "View progress") { vm.go(Screen.PROGRESS) } }
        item { HomeCard("Settings", "Local data and app information", "Open settings") { vm.go(Screen.SETTINGS) } }
        item { Text("Independent study aid. Not an official UK government app.", color = SecondaryText, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp)) }
    }
}

@Composable
private fun HomeCard(title: String, description: String, action: String, enabled: Boolean = true, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, SubtleBorder), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = MaterialTheme.colorScheme.primary)
            Text(description, modifier = Modifier.padding(top = 4.dp), fontSize = 15.sp)
            Text(action, modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold, color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PracticeScreen(s: UiState, vm: QuizViewModel, modifier: Modifier = Modifier) {
    val q = vm.question(s.practiceIds.getOrNull(s.practiceIndex))
    if (q == null) {
        Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text(if (s.screen == Screen.REVIEW) "No incorrect answers to review" else "No questions are available", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text("Load valid question records in app/src/main/assets/questions.json.", Modifier.padding(top = 12.dp))
            Button(onClick = { vm.go(Screen.HOME) }, modifier = Modifier.padding(top = 20.dp)) { Text("Back to Home") }
        }
        return
    }
    val correct = s.practiceCorrect
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(top = 14.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(if (s.screen == Screen.REVIEW) "Review Incorrect Answers" else "Practice Questions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Question ${s.practiceIndex + 1} of ${s.practiceIds.size}", modifier = Modifier.padding(top = 8.dp))
            Text("Illustrative question · not an official test question", fontSize = 13.sp, color = SecondaryText)
            LinearProgressIndicator(progress = { (s.practiceIndex + 1f) / s.practiceIds.size }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
        item { Text(q.question, fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)) }
        itemsIndexed(q.options) { index, option ->
            val status = when {
                s.practiceAnswer == null -> null
                index == q.correctAnswer -> Success
                index == s.practiceAnswer -> Error
                else -> null
            }
            OptionCard(option, status, selected = s.practiceAnswer == index, enabled = s.practiceAnswer == null) { vm.choosePractice(index) }
        }
        if (correct != null) item {
            val message = if (correct) "Correct answer" else "Incorrect answer. The correct answer is ${q.options[q.correctAnswer]}."
            Notice("$message\nExplanation: ${q.explanation}", if (correct) Success else Error)
        }
        if (correct != null) item {
            Button(onClick = {
                if ((s.practiceIndex + 1) >= s.practiceIds.size) vm.go(Screen.HOME) else vm.nextPractice()
            }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text(if ((s.practiceIndex + 1) >= s.practiceIds.size) "Finish" else "Next Question") }
        }
    }
}

@Composable
private fun OptionCard(text: String, status: Color?, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val label = when (status) { Success -> "Correct answer"; Error -> "Selected incorrect answer"; else -> if (selected) "Selected" else "Not selected" }
    Card(shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, status ?: SubtleBorder), colors = CardDefaults.cardColors(containerColor = when (status) { Success -> SuccessContainer; Error -> ErrorContainer; else -> MaterialTheme.colorScheme.surface }),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(enabled = enabled, onClick = onClick).semantics { contentDescription = "$text. $label" }) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick, enabled = enabled)
            Text(text, modifier = Modifier.weight(1f).padding(start = 8.dp), fontSize = 17.sp)
            status?.let { Text(if (it == Success) "✓ Correct" else "✕ Incorrect", color = it, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun Notice(message: String, color: Color) {
    Card(border = BorderStroke(1.dp, color.copy(alpha = 0.35f)), colors = CardDefaults.cardColors(containerColor = if (color == Success) SuccessContainer else if (color == Error) ErrorContainer else MaterialTheme.colorScheme.surface)) {
        Text(message, Modifier.fillMaxWidth().padding(16.dp), color = color, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp)
    }
}

@Composable
private fun ProgressScreen(s: UiState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("My Progress", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        StatCard("Questions answered", s.progress.answered.toString())
        StatCard("Correct answers", s.progress.correct.toString())
        StatCard("Incorrect answers", s.progress.incorrect.toString())
        StatCard("Saved for review", s.progress.incorrectIds.size.toString())
        StatCard("Best mock score", if (s.progress.mockScores.isEmpty()) "—" else "${s.progress.bestMockScore} / 24")
        StatCard("Mock tests completed", s.progress.mockScores.size.toString())
        Text("Recent mock scores", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
        if (s.progress.mockScores.isEmpty()) Text("No mock tests completed yet.", color = SecondaryText)
        s.progress.mockScores.asReversed().take(5).forEachIndexed { index, score ->
            StatCard("Mock test ${s.progress.mockScores.size - index}", "$score / 24")
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(border = BorderStroke(1.dp, SubtleBorder), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(17.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 16.sp); Text(value, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier, onReset: () -> Unit) {
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("App version 1.1.0", fontSize = 17.sp)
        Text("Disclaimer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("This app is an independent study aid. It is not affiliated with, endorsed by, or an official app of the UK government. Sample questions are illustrative and do not guarantee readiness for or success in the real test.", fontSize = 16.sp, lineHeight = 24.sp)
        OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Reset local progress") }
        Text("Your progress is stored on this device. This app does not request network access.", color = SecondaryText)
    }
}

@Composable
private fun MockScreen(s: UiState, vm: QuizViewModel, modifier: Modifier = Modifier, onEnd: () -> Unit) {
    val qs = vm.mockQuestions()
    val q = qs.getOrNull(s.mockIndex) ?: return
    Column(modifier.fillMaxSize().padding(18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mock Test", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("%02d:%02d".format(s.mockRemainingSeconds / 60, s.mockRemainingSeconds % 60), fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text("Original illustrative questions · not official test questions", fontSize = 13.sp, color = SecondaryText)
        Text("Question ${s.mockIndex + 1} of ${qs.size}", Modifier.padding(top = 8.dp))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text(q.question, fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold) }
            itemsIndexed(q.options) { index, option ->
                Card(border = BorderStroke(1.dp, if (s.mockAnswers[q.id] == index) Accent else SubtleBorder), colors = CardDefaults.cardColors(containerColor = if (s.mockAnswers[q.id] == index) SelectedContainer else MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().clickable { vm.selectMock(index) }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(s.mockAnswers[q.id] == index, { vm.selectMock(index) }); Text(option, Modifier.padding(start = 8.dp), fontSize = 16.sp) }
                }
            }
            item { Text("Question navigation", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp)) }
            item { Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                qs.indices.chunked(6).forEach { row -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { row.forEach { n ->
                    OutlinedButton(onClick = { vm.navigateMock(n) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (n == s.mockIndex) SelectedContainer else MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface)) { Text((n + 1).toString()) }
                } } }
            } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.navigateMock(s.mockIndex - 1) }, enabled = s.mockIndex > 0, modifier = Modifier.weight(1f)) { Text("Previous") }
            OutlinedButton(onClick = { vm.navigateMock(s.mockIndex + 1) }, enabled = s.mockIndex < qs.lastIndex, modifier = Modifier.weight(1f)) { Text("Next") }
        }
        TextButton(onClick = onEnd, modifier = Modifier.align(Alignment.End)) { Text("End test") }
    }
}

@Composable
private fun MockResultsScreen(s: UiState, vm: QuizViewModel, modifier: Modifier = Modifier) {
    val qs = vm.mockQuestions()
    val (correct, incorrect, unanswered, percentage) = QuizLogic.mockResult(qs, s.mockAnswers)
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 18.dp), contentPadding = PaddingValues(vertical = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Mock Test Results", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("$correct / ${qs.size} correct · $percentage%", Modifier.padding(top = 10.dp), fontSize = 21.sp, fontWeight = FontWeight.SemiBold)
            Text("Correct: $correct   Incorrect: $incorrect   Unanswered: $unanswered", Modifier.padding(top = 4.dp))
            Text("No negative marking is applied.", Modifier.padding(top = 4.dp), color = SecondaryText)
            Button(onClick = { vm.go(Screen.HOME) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Home") }
            Text("Review answers", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        }
        itemsIndexed(qs) { index, q ->
            val selected = s.mockAnswers[q.id]
            Card(border = BorderStroke(1.dp, SubtleBorder), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${index + 1}. ${q.question}", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text("Your answer: ${selected?.let(q.options::get) ?: "Unanswered"}", color = if (selected == q.correctAnswer) Success else Error)
                    Text("Correct answer: ${q.options[q.correctAnswer]}", color = Success, fontWeight = FontWeight.SemiBold)
                    Text("Explanation: ${q.explanation}")
                }
            }
        }
    }
}
