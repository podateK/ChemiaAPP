package dev.podatek.chemia.data

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

object UserPreferencesRepository {

	private const val PREFS_NAME = "chemia_prefs"
	private const val KEY_USERNAME = "username"
	private const val KEY_SOUND_ENABLED = "sound_enabled"
	private const val KEY_HAPTICS_ENABLED = "haptics_enabled"
	private const val KEY_TOTAL_POINTS = "total_points"
	private const val KEY_COMPLETED_QUIZZES = "completed_quizzes"
	private const val KEY_TOTAL_CORRECT = "total_correct"
	private const val KEY_TOTAL_ANSWERED = "total_answered"
	private const val KEY_PLAYER_ID = "player_id"
	private const val KEY_DEVICE_SOURCE_ID = "device_source_id"
	private const val KEY_AUTO_PLAYER_NAME = "auto_player_name"
	private val usernameRegex = Regex("^[A-Za-z0-9]{1,12}$")

	private fun prefs(context: Context) =
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	fun getUsername(context: Context): String {
		val p = prefs(context)
		val saved = p.getString(KEY_USERNAME, null)
		if (!saved.isNullOrBlank() && isValidUsername(saved)) {
			return saved
		}

		val fallback = getOrCreatePlayerName(context)
		p.edit().putString(KEY_USERNAME, fallback).apply()
		return fallback
	}

	fun setUsername(context: Context, username: String) {
		val sanitized = sanitizeUsername(username)
		val finalName = if (isValidUsername(sanitized)) sanitized else getOrCreatePlayerName(context)
		prefs(context).edit().putString(KEY_USERNAME, finalName).apply()
	}

	fun sanitizeUsername(username: String): String = username.trim()

	fun isValidUsername(username: String): Boolean = usernameRegex.matches(username)

	fun getOrCreatePlayerId(context: Context): String {
		val p = prefs(context)
		val saved = p.getString(KEY_PLAYER_ID, null)
		if (!saved.isNullOrBlank()) return saved

		val source = getDeviceSourceId(context)
		val playerId = sha256(source).take(24)

		p.edit().putString(KEY_PLAYER_ID, playerId).apply()
		return playerId
	}

	fun getDeviceFingerprint(context: Context): String {
		val source = getDeviceSourceId(context)
		return sha256(source)
	}

	fun getOrCreatePlayerName(context: Context): String {
		val p = prefs(context)
		val saved = p.getString(KEY_AUTO_PLAYER_NAME, null)
		if (!saved.isNullOrBlank() && isValidUsername(saved) && !isLegacyPlayerName(saved)) return saved

		val playerId = getOrCreatePlayerId(context)
		val name = "Gracz${playerId.takeLast(6).uppercase(Locale.US)}"

		p.edit().putString(KEY_AUTO_PLAYER_NAME, name).apply()
		return name
	}

	private fun isLegacyPlayerName(name: String): Boolean {
		return Regex("^Gracz-\\d{3}$").matches(name)
	}

	fun isSoundEnabled(context: Context): Boolean =
		prefs(context).getBoolean(KEY_SOUND_ENABLED, true)

	fun setSoundEnabled(context: Context, enabled: Boolean) {
		prefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
	}

	fun isHapticsEnabled(context: Context): Boolean =
		prefs(context).getBoolean(KEY_HAPTICS_ENABLED, true)

	fun setHapticsEnabled(context: Context, enabled: Boolean) {
		prefs(context).edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
	}

	fun addQuizResult(context: Context, correctAnswers: Int, totalQuestions: Int, pointsEarned: Int) {
		val p = prefs(context)
		val currentPoints = p.getInt(KEY_TOTAL_POINTS, 0)
		val currentCompleted = p.getInt(KEY_COMPLETED_QUIZZES, 0)
		val currentCorrect = p.getInt(KEY_TOTAL_CORRECT, 0)
		val currentAnswered = p.getInt(KEY_TOTAL_ANSWERED, 0)

		p.edit()
			.putInt(KEY_TOTAL_POINTS, currentPoints + pointsEarned)
			.putInt(KEY_COMPLETED_QUIZZES, currentCompleted + 1)
			.putInt(KEY_TOTAL_CORRECT, currentCorrect + correctAnswers)
			.putInt(KEY_TOTAL_ANSWERED, currentAnswered + totalQuestions)
			.apply()
	}

	fun getTotalPoints(context: Context): Int = prefs(context).getInt(KEY_TOTAL_POINTS, 0)

	fun getCompletedQuizzes(context: Context): Int = prefs(context).getInt(KEY_COMPLETED_QUIZZES, 0)

	fun getSuccessPercentage(context: Context): Int {
		val p = prefs(context)
		val answered = p.getInt(KEY_TOTAL_ANSWERED, 0)
		if (answered == 0) return 0
		val correct = p.getInt(KEY_TOTAL_CORRECT, 0)
		return (correct * 100) / answered
	}

	private fun sha256(input: String): String {
		val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
		return bytes.joinToString("") { "%02x".format(it) }
	}

	private fun getDeviceSourceId(context: Context): String {
		val p = prefs(context)
		val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
		if (!androidId.isNullOrBlank()) return androidId

		val savedFallback = p.getString(KEY_DEVICE_SOURCE_ID, null)
		if (!savedFallback.isNullOrBlank()) return savedFallback

		val generated = UUID.randomUUID().toString()
		p.edit().putString(KEY_DEVICE_SOURCE_ID, generated).apply()
		return generated
	}
}

