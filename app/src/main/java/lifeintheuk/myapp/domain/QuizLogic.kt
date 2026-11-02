package lifeintheuk.myapp.domain

import lifeintheuk.myapp.data.Question

data class MockResult(val correct: Int, val incorrect: Int, val unanswered: Int, val percentage: Int)

object QuizLogic {
    fun isCorrect(question: Question, selectedIndex: Int): Boolean = selectedIndex == question.correctAnswer
    fun score(questions: List<Question>, answers: Map<String, Int>): Int =
        questions.count { (id, _, _, correctAnswer) -> answers[id] == correctAnswer }
    fun unanswered(questions: List<Question>, answers: Map<String, Int>): Int = questions.count { it.id !in answers }
    fun mockResult(questions: List<Question>, answers: Map<String, Int>): MockResult {
        val correct = score(questions, answers)
        val unanswered = unanswered(questions, answers)
        return MockResult(
            correct,
            questions.size - correct - unanswered,
            unanswered,
            if (questions.isEmpty()) 0 else (correct * 100) / questions.size,
        )
    }
    fun nextIncorrectIds(existing: Set<String>, question: Question, correct: Boolean): Set<String> =
        if (correct) existing - question.id else existing + question.id
}
