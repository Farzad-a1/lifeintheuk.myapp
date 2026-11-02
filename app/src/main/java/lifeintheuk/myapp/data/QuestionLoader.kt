package lifeintheuk.myapp.data

import android.content.Context
import org.json.JSONArray

object QuestionLoader {
    fun load(context: Context): Result<List<Question>> = runCatching {
        val raw = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        parse(raw)
    }

    fun parse(raw: String): List<Question> {
        val array = JSONArray(raw)
        require(array.length() > 0) { "The question file is empty." }
        val questions = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val optionsJson = item.getJSONArray("options")
            val options = (0 until optionsJson.length()).map { optionsJson.getString(it).trim() }
            val answer = item.getInt("correctAnswer")
            require(item.getString("id").isNotBlank()) { "Question ${index + 1} has no ID." }
            require(item.getString("question").isNotBlank()) { "Question ${index + 1} has no text." }
            require((options.size == 4) && options.all(String::isNotBlank)) { "Question ${index + 1} must have four non-empty options." }
            require(answer in 0..3) { "Question ${index + 1} has an invalid correctAnswer index." }
            require(item.getString("explanation").isNotBlank() && item.getString("topic").isNotBlank()) {
                "Question ${index + 1} needs an explanation and topic."
            }
            Question(
                item.getString("id"),
                item.getString("question"),
                options,
                answer,
                item.getString("explanation"),
                item.getString("topic"),
            )
        }
        require(questions.distinctBy { it.id }.size == questions.size) { "Question IDs must be unique." }
        return questions
    }
}
