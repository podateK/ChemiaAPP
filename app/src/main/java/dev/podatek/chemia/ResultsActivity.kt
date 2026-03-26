package dev.podatek.chemia

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestoreException
import dev.podatek.chemia.data.FirebaseRankingRepository
import dev.podatek.chemia.data.UserPreferencesRepository
import dev.podatek.chemia.models.QuizResult

class ResultsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ResultsRankingSync"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val result = intent.getParcelableExtra<QuizResult>("quiz_result")

        if (result != null) {
            UserPreferencesRepository.addQuizResult(
                context = this,
                correctAnswers = result.correctAnswers,
                totalQuestions = result.totalQuestions,
                pointsEarned = result.pointsEarned
            )
            syncResultWithRanking(result)
            displayResults(result)
        }

        val btnPlayAgain = findViewById<Button>(R.id.btn_play_again)
        btnPlayAgain.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        val btnMenu = findViewById<Button>(R.id.btn_menu)
        btnMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun displayResults(result: QuizResult) {
        val scorePercentage = findViewById<TextView>(R.id.score_percentage)
        val feedbackMessage = findViewById<TextView>(R.id.feedback_message)
        val correctCount = findViewById<TextView>(R.id.correct_count)
        val wrongCount = findViewById<TextView>(R.id.wrong_count)
        val skippedCount = findViewById<TextView>(R.id.skipped_count)
        val timeSpent = findViewById<TextView>(R.id.time_spent)
        val weaknessCategory = findViewById<TextView>(R.id.weakness_category)
        val pointsEarned = findViewById<TextView>(R.id.points_earned)

        scorePercentage.text = "${result.percentage}%"

        val animator = ObjectAnimator.ofInt(0, result.percentage)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            scorePercentage.text = "${animation.animatedValue}%"
        }
        animator.start()

        feedbackMessage.text = when {
            result.percentage < 50 -> "Spróbuj jeszcze raz! 💪"
            result.percentage < 80 -> "Dobra robota! 🎉"
            else -> "Wyśmienicie! 🏆"
        }

        feedbackMessage.setTextColor(
            when {
                result.percentage < 50 -> getColor(R.color.error_red)
                result.percentage < 80 -> getColor(R.color.timer_warning)
                else -> getColor(R.color.success_green)
            }
        )

        correctCount.text = "${result.correctAnswers}"
        wrongCount.text = "${result.wrongAnswers}"
        skippedCount.text = "${result.skippedAnswers}"

        val minutes = result.timeSeconds / 60
        val seconds = result.timeSeconds % 60
        timeSpent.text = "$minutes:${seconds.toString().padStart(2, '0')}"

        weaknessCategory.text = result.categoryWeakness

        pointsEarned.text = "+${result.pointsEarned} pkt"
    }

    private fun syncResultWithRanking(result: QuizResult) {
        FirebaseRankingRepository.isCurrentPlayerRegistered(
            context = this,
            onResult = { exists ->
                if (exists) {
                    submitResult(result)
                } else {
                    Toast.makeText(this, R.string.name_setup_required, Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, NameSetupActivity::class.java))
                }
            },
            onError = { err ->
                logRankingError("isCurrentPlayerRegistered", err)
                showSyncError(err)
            }
        )
    }

    private fun submitResult(result: QuizResult) {
        FirebaseRankingRepository.submitQuizResult(
            context = this,
            result = result,
            onError = { err ->
                logRankingError("submitQuizResult", err)
                showSyncError(err)
            }
        )
    }

    private fun showSyncError(error: Exception) {
        val message = if (BuildConfig.DEBUG) {
            "${getString(R.string.ranking_sync_pending)}\n${error.message ?: "Brak szczegolow"}"
        } else {
            getString(R.string.ranking_sync_pending)
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun logRankingError(operation: String, error: Exception) {
        val firestoreCode = (error as? FirebaseFirestoreException)?.code?.name ?: "N/A"
        Log.e(
            TAG,
            "[$operation] Sync error | type=${error::class.java.simpleName} | code=$firestoreCode | message=${error.message}",
            error
        )
    }
}

