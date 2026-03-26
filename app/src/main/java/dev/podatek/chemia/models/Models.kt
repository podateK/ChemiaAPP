package dev.podatek.chemia.models

import android.os.Parcel
import android.os.Parcelable

data class Question(
    val id: Int,
    val text: String,
    val category: String,
    val level: Int,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

data class QuizResult(
    val quizId: Int,
    val mode: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val skippedAnswers: Int,
    val timeSeconds: Long,
    val percentage: Int,
    val categoryWeakness: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    val pointsEarned: Int
        get() = when (mode) {
            "quick" -> correctAnswers * 10
            "exam" -> (correctAnswers * 10) + (if (timeSeconds < 300) 5 else 0)
            "blitz" -> correctAnswers * 15
            else -> correctAnswers * 10
        }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(quizId)
        parcel.writeString(mode)
        parcel.writeInt(totalQuestions)
        parcel.writeInt(correctAnswers)
        parcel.writeInt(wrongAnswers)
        parcel.writeInt(skippedAnswers)
        parcel.writeLong(timeSeconds)
        parcel.writeInt(percentage)
        parcel.writeString(categoryWeakness)
        parcel.writeLong(timestamp)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<QuizResult> {
        override fun createFromParcel(parcel: Parcel) = QuizResult(parcel)
        override fun newArray(size: Int) = arrayOfNulls<QuizResult>(size)
    }
}

data class UserProfile(
    val username: String = "Użytkownik",
    var totalPoints: Int = 0,
    var completedQuizzes: Int = 0,
    var correctAnswers: Int = 0,
    var totalAnswered: Int = 0
) {
    val successPercentage: Int
        get() = if (totalAnswered == 0) 0 else (correctAnswers * 100) / totalAnswered
}

data class RankingEntry(
    val playerId: String,
    val playerName: String,
    val totalPoints: Long,
    val quizzesPlayed: Int,
    val bestPercentage: Int
)

