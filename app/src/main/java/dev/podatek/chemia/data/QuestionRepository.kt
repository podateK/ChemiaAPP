package dev.podatek.chemia.data

import android.content.Context
import dev.podatek.chemia.models.Question
import org.json.JSONArray
import org.json.JSONObject

object QuestionRepository {
    private var cachedQuestions: List<Question>? = null

    fun getQuestions(context: Context? = null): List<Question> {

        if (cachedQuestions != null) {
            return cachedQuestions!!
        }

        if (context == null) {
            return emptyList()
        }

        return try {
            val jsonString = context.assets.open("questions.json").bufferedReader().readText()
            val jsonObject = JSONObject(jsonString)
            val questionsArray = jsonObject.getJSONArray("questions")
            
            val questions = mutableListOf<Question>()
            for (i in 0 until questionsArray.length()) {
                val questionObj = questionsArray.getJSONObject(i)
                
                val options = mutableListOf<String>()
                val optionsArray = questionObj.getJSONArray("options")
                for (j in 0 until optionsArray.length()) {
                    options.add(optionsArray.getString(j))
                }
                
                val question = Question(
                    id = questionObj.getInt("id"),
                    text = questionObj.getString("text"),
                    category = questionObj.getString("category"),
                    level = questionObj.getInt("level"),
                    options = options,
                    correctAnswerIndex = questionObj.getInt("correctAnswerIndex"),
                    explanation = questionObj.getString("explanation")
                )
                questions.add(question)
            }
            
            cachedQuestions = questions
            questions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getQuestionsByCategory(category: String, context: Context? = null): List<Question> {
        val questions = if (cachedQuestions != null) cachedQuestions!! else getQuestions(context)
        return questions.filter { it.category == category }
    }

    fun getRandomQuestions(count: Int, context: Context? = null): List<Question> {
        val questions = if (cachedQuestions != null) cachedQuestions!! else getQuestions(context)
        return questions.shuffled().take(count)
    }

    fun getRandomQuestionsExact(count: Int, context: Context? = null): List<Question> {
        val questions = if (cachedQuestions != null) cachedQuestions!! else getQuestions(context)
        
        if (count <= 0) return emptyList()
        if (questions.isEmpty()) return emptyList()
        if (count <= questions.size) return questions.shuffled().take(count)

        val out = mutableListOf<Question>()
        var round = 0

        while (out.size < count) {
            val shuffled = questions.shuffled()
            for ((index, q) in shuffled.withIndex()) {
                if (out.size >= count) break
                val uniqueId = q.id + (round * 1000) + index
                out.add(q.copy(id = uniqueId))
            }
            round++
        }

        return out
    }
}

