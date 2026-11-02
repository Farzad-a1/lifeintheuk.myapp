package lifeintheuk.myapp.data

data class Question(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val topic: String,
)

data class Progress(
    val answered: Int = 0,
    val correct: Int = 0,
    val incorrectIds: Set<String> = emptySet(),
    val mockScores: List<Int> = emptyList(),
) {
    val incorrect: Int get() = answered - correct
    val bestMockScore: Int get() = mockScores.maxOrNull() ?: 0
}
