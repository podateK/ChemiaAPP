package dev.podatek.chemia

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import dev.podatek.chemia.data.QuestionRepository
import dev.podatek.chemia.models.Question
import dev.podatek.chemia.models.QuizResult

class QuizActivity : AppCompatActivity() {

    private lateinit var questions: List<Question>
    private var currentQuestionIndex = 0
    private var selectedAnswerIndex: Int? = null
    private var correctAnswersCount = 0
    private var wrongAnswersCount = 0
    private var skippedAnswersCount = 0
    private var quizMode: String = "quick"
    private var startTime: Long = 0
    private var currentTimer: CountDownTimer? = null

    private lateinit var progressText: TextView
    private lateinit var timerText: TextView
    private lateinit var pointsChip: TextView
    private lateinit var questionText: TextView
    private lateinit var categoryTag: TextView
    private lateinit var levelTag: TextView
    private lateinit var explanationCard: LinearLayout
    private lateinit var explanationText: TextView
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button

    private val optionViews = mutableListOf<LinearLayout>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        quizMode = intent.getStringExtra("quiz_mode") ?: "quick"
        startTime = System.currentTimeMillis()

        initializeViews()
        loadQuestions()
        displayQuestion()
        startTimer()
    }

    private fun initializeViews() {
        progressText = findViewById(R.id.progress_text)
        timerText = findViewById(R.id.timer)
        pointsChip = findViewById(R.id.points_chip)
        questionText = findViewById(R.id.question_text)
        categoryTag = findViewById(R.id.category_tag)
        levelTag = findViewById(R.id.level_tag)
        explanationCard = findViewById(R.id.explanation_card)
        explanationText = findViewById(R.id.explanation_text)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)

        optionViews.add(findViewById(R.id.option_a))
        optionViews.add(findViewById(R.id.option_b))
        optionViews.add(findViewById(R.id.option_c))
        optionViews.add(findViewById(R.id.option_d))

        optionViews.forEachIndexed { index, option ->
            option.setOnClickListener {
                if (selectedAnswerIndex == null) {
                    onAnswerSelected(index)
                }
            }
        }

        btnNext.setOnClickListener {
            goToNextQuestion()
        }

        btnSkip.setOnClickListener {
            skipQuestion()
        }
    }

    private fun loadQuestions() {
        val targetCount = when (quizMode) {
            "quick" -> 10
            "study" -> 8
            "exam" -> 50
            "blitz" -> 15
            else -> 10
        }
        questions = QuestionRepository.getRandomQuestionsExact(targetCount, this)
    }

    private fun displayQuestion() {
        if (currentQuestionIndex >= questions.size) {
            endQuiz()
            return
        }

        val question = questions[currentQuestionIndex]
        selectedAnswerIndex = null

        progressText.text = "${currentQuestionIndex + 1}/${questions.size}"

        questionText.text = question.text
        categoryTag.text = "⚗ ${question.category}"
        levelTag.text = "• Poziom: ${getLevelName(question.level)}"

        optionViews.forEachIndexed { index, option ->
            option.background = getDrawable(R.drawable.option_button_default)
            option.isClickable = true

            val textView = option.getChildAt(1) as TextView
            textView.text = question.options[index]
            textView.setTextColor(getColor(R.color.white))
        }

        explanationCard.visibility = android.view.View.GONE
        btnSkip.visibility = android.view.View.VISIBLE
        btnNext.visibility = android.view.View.GONE
    }

    private fun onAnswerSelected(selectedIndex: Int) {
        selectedAnswerIndex = selectedIndex
        val question = questions[currentQuestionIndex]
        val isCorrect = selectedIndex == question.correctAnswerIndex

        if (isCorrect) {
            correctAnswersCount++
            optionViews[selectedIndex].background = getDrawable(R.drawable.option_button_correct)
            optionViews[selectedIndex].getChildAt(1)?.let { it as? TextView }?.apply {
                setTextColor(getColor(R.color.white))
                text = "${question.options[selectedIndex]} ✓"
            }
            

            val flashAnim = AlphaAnimation(1f, 0.3f)
            flashAnim.duration = 200
            optionViews[selectedIndex].startAnimation(flashAnim)
        } else {
            wrongAnswersCount++
            optionViews[selectedIndex].background = getDrawable(R.drawable.option_button_wrong)
            optionViews[selectedIndex].getChildAt(1)?.let { it as? TextView }?.apply {
                setTextColor(getColor(R.color.white))
                text = "${question.options[selectedIndex]} ✗"
            }

            optionViews[question.correctAnswerIndex].background = getDrawable(R.drawable.option_button_correct)
            optionViews[question.correctAnswerIndex].getChildAt(1)?.let { it as? TextView }?.apply {
                setTextColor(getColor(R.color.white))
                text = "${question.options[question.correctAnswerIndex]} ✓"
            }
        }

        optionViews.forEach { it.isClickable = false }

        explanationCard.visibility = android.view.View.VISIBLE
        explanationText.text = question.explanation

        btnSkip.visibility = android.view.View.GONE
        btnNext.visibility = android.view.View.VISIBLE
    }

    private fun skipQuestion() {
        skippedAnswersCount++
        goToNextQuestion()
    }

    private fun goToNextQuestion() {
        currentQuestionIndex++
        if (currentQuestionIndex >= questions.size) {
            endQuiz()
        } else {
            displayQuestion()
        }
    }

    private fun startTimer() {
        val timeLimit = when (quizMode) {
            "exam" -> 30 * 60 * 1000L
            "blitz" -> questions.size * 15 * 1000L
            else -> 60 * 60 * 1000L
        }

        currentTimer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000

                timerText.text = String.format("⏱ %02d:%02d", minutes, seconds)

                when {
                    millisUntilFinished < 5000 -> timerText.setTextColor(getColor(R.color.error_red))
                    millisUntilFinished < 10000 -> timerText.setTextColor(getColor(R.color.timer_warning))
                    else -> timerText.setTextColor(getColor(R.color.text_light))
                }
            }

            override fun onFinish() {
                endQuiz()
            }
        }.start()
    }

    private fun endQuiz() {
        currentTimer?.cancel()

        val timeSpent = (System.currentTimeMillis() - startTime) / 1000
        val percentage = if (questions.size > 0) (correctAnswersCount * 100) / questions.size else 0

        val result = QuizResult(
            quizId = System.currentTimeMillis().toInt(),
            mode = quizMode,
            totalQuestions = questions.size,
            correctAnswers = correctAnswersCount,
            wrongAnswers = wrongAnswersCount,
            skippedAnswers = skippedAnswersCount,
            timeSeconds = timeSpent,
            percentage = percentage,
            categoryWeakness = "Stechiometria"
        )

        val intent = Intent(this, ResultsActivity::class.java)
        intent.putExtra("quiz_result", result)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun getLevelName(level: Int): String {
        return when (level) {
            1 -> "łatwy"
            2 -> "średni"
            3 -> "trudny"
            else -> "średni"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentTimer?.cancel()
    }
}

