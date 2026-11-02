package lifeintheuk.myapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class PracticeFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun homeAndPracticeQuestionAreReachable() {
        compose.onNodeWithText("Practice Questions").assertIsDisplayed().performClick()
        compose.onNodeWithText("Which city is the capital of Scotland?").assertIsDisplayed()
        compose.onNodeWithText("Edinburgh").performClick()
        compose.onNodeWithText("Correct answer\nExplanation: Edinburgh is the capital city of Scotland.").assertIsDisplayed()
    }
}
