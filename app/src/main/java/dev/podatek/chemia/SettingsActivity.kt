package dev.podatek.chemia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.text.InputFilter
import androidx.appcompat.app.AppCompatActivity
import dev.podatek.chemia.data.FirebaseRankingRepository
import androidx.appcompat.widget.SwitchCompat
import dev.podatek.chemia.data.UserPreferencesRepository

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val usernameInput = findViewById<EditText>(R.id.input_username)
        val soundSwitch = findViewById<SwitchCompat>(R.id.switch_sound)
        val hapticsSwitch = findViewById<SwitchCompat>(R.id.switch_haptics)
        val btnSave = findViewById<Button>(R.id.btn_save_settings)
        val btnBack = findViewById<Button>(R.id.btn_back_menu)

        usernameInput.setText(UserPreferencesRepository.getUsername(this))
        usernameInput.filters = arrayOf(InputFilter.LengthFilter(12))
        soundSwitch.isChecked = UserPreferencesRepository.isSoundEnabled(this)
        hapticsSwitch.isChecked = UserPreferencesRepository.isHapticsEnabled(this)

        btnSave.setOnClickListener {
            val candidate = UserPreferencesRepository.sanitizeUsername(usernameInput.text.toString())
            if (!UserPreferencesRepository.isValidUsername(candidate)) {
                Toast.makeText(this, R.string.name_setup_validation_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            UserPreferencesRepository.setSoundEnabled(this, soundSwitch.isChecked)
            UserPreferencesRepository.setHapticsEnabled(this, hapticsSwitch.isChecked)

            val currentUsername = UserPreferencesRepository.getUsername(this)
            if (candidate == currentUsername) {
                Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            FirebaseRankingRepository.updateCurrentPlayerName(
                context = this,
                username = candidate,
                onComplete = {
                    btnSave.isEnabled = true
                    Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
                    finish()
                },
                onNameTaken = {
                    btnSave.isEnabled = true
                    Toast.makeText(this, R.string.name_setup_name_taken, Toast.LENGTH_SHORT).show()
                },
                onError = {
                    btnSave.isEnabled = true
                    Toast.makeText(this, R.string.settings_name_sync_error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}

