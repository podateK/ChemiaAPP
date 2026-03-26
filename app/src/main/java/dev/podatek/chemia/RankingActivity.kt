package dev.podatek.chemia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import dev.podatek.chemia.data.FirebaseRankingRepository
import dev.podatek.chemia.data.UserPreferencesRepository
import dev.podatek.chemia.models.RankingEntry

class RankingActivity : AppCompatActivity() {

    private enum class RankingTab {
        POINTS, EFFICIENCY, QUIZZES
    }

    private lateinit var myPlayerNameText: TextView
    private lateinit var loading: ProgressBar
    private lateinit var emptyMessage: TextView
    private lateinit var rankingContainer: LinearLayout
    private lateinit var btnTabPoints: Button
    private lateinit var btnTabEfficiency: Button
    private lateinit var btnTabQuizzes: Button

    private var selectedTab: RankingTab = RankingTab.POINTS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        myPlayerNameText = findViewById(R.id.my_player_name)
        loading = findViewById(R.id.ranking_loading)
        emptyMessage = findViewById(R.id.ranking_empty)
        rankingContainer = findViewById(R.id.ranking_list_container)
        btnTabPoints = findViewById(R.id.btn_tab_points)
        btnTabEfficiency = findViewById(R.id.btn_tab_efficiency)
        btnTabQuizzes = findViewById(R.id.btn_tab_quizzes)

        val btnRefresh = findViewById<Button>(R.id.btn_refresh_ranking)
        val btnBack = findViewById<Button>(R.id.btn_back_from_ranking)

        myPlayerNameText.text = UserPreferencesRepository.getOrCreatePlayerName(this)

        btnRefresh.setOnClickListener {
            loadRanking()
        }

        btnTabPoints.setOnClickListener {
            selectedTab = RankingTab.POINTS
            updateTabSelection()
            loadRanking()
        }

        btnTabEfficiency.setOnClickListener {
            selectedTab = RankingTab.EFFICIENCY
            updateTabSelection()
            loadRanking()
        }

        btnTabQuizzes.setOnClickListener {
            selectedTab = RankingTab.QUIZZES
            updateTabSelection()
            loadRanking()
        }

        btnBack.setOnClickListener {
            finish()
        }

        updateTabSelection()
        loadRanking()
    }

    private fun loadRanking() {
        loading.visibility = View.VISIBLE
        emptyMessage.visibility = View.GONE
        rankingContainer.removeAllViews()

        val (orderField, metricType) = when (selectedTab) {
            RankingTab.POINTS -> "totalPoints" to MetricType.POINTS
            RankingTab.EFFICIENCY -> "bestPercentage" to MetricType.EFFICIENCY
            RankingTab.QUIZZES -> "quizzesPlayed" to MetricType.QUIZZES
        }

        FirebaseRankingRepository.fetchTopRanking(
            context = this,
            limit = 100,
            orderField = orderField,
            onSuccess = { ranking ->
                loading.visibility = View.GONE
                if (ranking.isEmpty()) {
                    emptyMessage.visibility = View.VISIBLE
                    return@fetchTopRanking
                }
                renderRanking(ranking, metricType)
            },
            onError = {
                loading.visibility = View.GONE
                emptyMessage.visibility = View.VISIBLE
            }
        )
    }

    private fun updateTabSelection() {
        fun styleButton(button: Button, selected: Boolean) {
            if (selected) {
                button.setBackgroundResource(R.drawable.button_primary)
                button.setTextColor(getColor(R.color.white))
            } else {
                button.setBackgroundResource(R.drawable.button_outline)
                button.setTextColor(getColor(R.color.accent_blue))
            }
        }

        styleButton(btnTabPoints, selectedTab == RankingTab.POINTS)
        styleButton(btnTabEfficiency, selectedTab == RankingTab.EFFICIENCY)
        styleButton(btnTabQuizzes, selectedTab == RankingTab.QUIZZES)
    }

    private enum class MetricType {
        POINTS, EFFICIENCY, QUIZZES
    }

    private fun renderRanking(entries: List<RankingEntry>, metricType: MetricType) {
        val currentPlayerId = UserPreferencesRepository.getOrCreatePlayerId(this)

        val header = TextView(this).apply {
            text = when (metricType) {
                MetricType.POINTS -> getString(R.string.ranking_top_points)
                MetricType.EFFICIENCY -> getString(R.string.ranking_top_efficiency)
                MetricType.QUIZZES -> getString(R.string.ranking_top_quizzes)
            }
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@RankingActivity, R.color.white))
            setPadding(0, 10, 0, 10)
        }
        rankingContainer.addView(header)

        entries.forEachIndexed { index, entry ->
            val row = layoutInflater.inflate(R.layout.item_ranking_entry, rankingContainer, false)

            val rankText = row.findViewById<TextView>(R.id.rank_number)
            val nameText = row.findViewById<TextView>(R.id.rank_player_name)
            val pointsText = row.findViewById<TextView>(R.id.rank_points)
            val detailsText = row.findViewById<TextView>(R.id.rank_details)

            rankText.text = "#${index + 1}"
            nameText.text = entry.playerName
            pointsText.text = when (metricType) {
                MetricType.POINTS -> "${entry.totalPoints} pkt"
                MetricType.EFFICIENCY -> "${entry.bestPercentage}%"
                MetricType.QUIZZES -> "${entry.quizzesPlayed} quizów"
            }
            pointsText.setTextColor(
                when (metricType) {
                    MetricType.POINTS -> getColor(R.color.ranking_points)
                    MetricType.EFFICIENCY -> getColor(R.color.ranking_efficiency)
                    MetricType.QUIZZES -> getColor(R.color.ranking_quizzes)
                }
            )
            detailsText.text = when (metricType) {
                MetricType.POINTS -> getString(
                    R.string.ranking_row_points_details,
                    entry.bestPercentage,
                    entry.quizzesPlayed
                )
                MetricType.EFFICIENCY -> getString(
                    R.string.ranking_row_efficiency_details,
                    entry.totalPoints,
                    entry.quizzesPlayed
                )
                MetricType.QUIZZES -> getString(
                    R.string.ranking_row_quizzes_details,
                    entry.totalPoints,
                    entry.bestPercentage
                )
            }

            if (entry.playerId == currentPlayerId) {
                row.setBackgroundResource(R.drawable.option_button_correct)
            }

            rankingContainer.addView(row)
        }
    }
}

