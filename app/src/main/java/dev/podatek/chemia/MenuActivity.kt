package dev.podatek.chemia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import dev.podatek.chemia.data.UserPreferencesRepository

class MenuActivity : AppCompatActivity() {

    private lateinit var usernameText: TextView
    private lateinit var totalPointsText: TextView
    private lateinit var totalQuizzesText: TextView
    private lateinit var successPercentageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        usernameText = findViewById(R.id.username)
        totalPointsText = findViewById(R.id.total_points)
        totalQuizzesText = findViewById(R.id.total_quizzes)
        successPercentageText = findViewById(R.id.success_percentage)

        try {

            val modeQuickQuiz = findViewById<LinearLayout>(R.id.mode_quick_quiz)
            modeQuickQuiz.setOnClickListener {
                startQuiz("quick")
            }

            val modeStudy = findViewById<LinearLayout>(R.id.mode_study)
            modeStudy.setOnClickListener {
                startQuiz("study")
            }

            val modeExam = findViewById<LinearLayout>(R.id.mode_exam)
            modeExam.setOnClickListener {
                startQuiz("exam")
            }

            val modeBlitz = findViewById<LinearLayout>(R.id.mode_blitz)
            modeBlitz.setOnClickListener {
                startQuiz("blitz")
            }

            val navStats = findViewById<FrameLayout>(R.id.nav_stats)
            navStats.setOnClickListener {
                Toast.makeText(this, "Statystyki będą dostępne wkrótce", Toast.LENGTH_SHORT).show()
            }

            val navRanking = findViewById<FrameLayout>(R.id.nav_ranking)
            navRanking.setOnClickListener {
                startActivity(Intent(this, RankingActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

            val navSettings = findViewById<FrameLayout>(R.id.nav_settings)
            navSettings.setOnClickListener {
                startActivity(Intent(this, SettingsActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val username = UserPreferencesRepository.getUsername(this)
        val totalPoints = UserPreferencesRepository.getTotalPoints(this)
        val completedQuizzes = UserPreferencesRepository.getCompletedQuizzes(this)
        val success = UserPreferencesRepository.getSuccessPercentage(this)

        usernameText.text = username
        totalPointsText.text = totalPoints.toString()
        totalQuizzesText.text = completedQuizzes.toString()
        successPercentageText.text = "$success%"
    }

    private fun startQuiz(mode: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("quiz_mode", mode)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}

