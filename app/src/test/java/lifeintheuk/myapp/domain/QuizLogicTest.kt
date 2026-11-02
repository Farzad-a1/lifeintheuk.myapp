package lifeintheuk.myapp.domain

import lifeintheuk.myapp.data.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizLogicTest {
    private val q1 = Question("a", "Question A", listOf("A", "B", "C", "D"), 1, "Reason A", "Topic")
    private val q2 = Question("b", "Question B", listOf("A", "B", "C", "D"), 0, "Reason B", "Topic")

    @Test fun checksAnswerByOptionIndex() {
        assertTrue(QuizLogic.isCorrect(q1, 1))
        assertFalse(QuizLogic.isCorrect(q1, 0))
    }

    @Test fun calculatesScoreAndUnansweredCount() {
        assertEquals(1, QuizLogic.score(listOf(q1, q2), mapOf("a" to 1, "b" to 2)))
        assertEquals(0, QuizLogic.unanswered(listOf(q1, q2), mapOf("a" to 1, "b" to 2)))
        assertEquals(1, QuizLogic.unanswered(listOf(q1, q2), mapOf("a" to 1)))
    }

    @Test fun incorrectAnswerSetAddsOnceAndCorrectReviewRemoves() {
        val first = QuizLogic.nextIncorrectIds(emptySet(), q1, correct = false)
        assertEquals(setOf("a"), QuizLogic.nextIncorrectIds(first, q1, correct = false))
        assertEquals(emptySet<String>(), QuizLogic.nextIncorrectIds(first, q1, correct = true))
    }

    @Test fun mockResultSeparatesIncorrectAndUnansweredWithoutNegativeMarking() {
        val result = QuizLogic.mockResult(listOf(q1, q2), mapOf("a" to 1, "b" to 3))
        assertEquals(1, result.correct)
        assertEquals(1, result.incorrect)
        assertEquals(0, result.unanswered)
        assertEquals(50, result.percentage)
        val partial = QuizLogic.mockResult(listOf(q1, q2), mapOf("a" to 0))
        assertEquals(1, partial.incorrect)
        assertEquals(1, partial.unanswered)
    }

    @Test fun emptyTestHasNoAnswersOrUnansweredQuestions() {
        assertEquals(0, QuizLogic.score(emptyList(), emptyMap()))
        assertEquals(0, QuizLogic.unanswered(emptyList(), emptyMap()))
    }
}
