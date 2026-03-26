package dev.podatek.chemia

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestoreException
import dev.podatek.chemia.data.FirebaseRankingRepository
import dev.podatek.chemia.data.UserPreferencesRepository

class NameSetupActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NameSetup"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_setup)

        val usernameInput = findViewById<EditText>(R.id.input_new_username)
        val errorText = findViewById<TextView>(R.id.name_error)
        val loading = findViewById<ProgressBar>(R.id.name_setup_loading)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_username)

        usernameInput.filters = arrayOf(InputFilter.LengthFilter(12))

        btnConfirm.setOnClickListener {
            val candidate = UserPreferencesRepository.sanitizeUsername(usernameInput.text.toString())
            errorText.text = ""

            if (!UserPreferencesRepository.isValidUsername(candidate)) {
                errorText.text = getString(R.string.name_setup_validation_error)
                return@setOnClickListener
            }

            btnConfirm.isEnabled = false
            loading.visibility = android.view.View.VISIBLE

            FirebaseRankingRepository.createCurrentPlayer(
                context = this,
                username = candidate,
                onComplete = {
                    loading.visibility = android.view.View.GONE
                    startActivity(Intent(this, MenuActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    finish()
                },
                onNameTaken = {
                    loading.visibility = android.view.View.GONE
                    btnConfirm.isEnabled = true
                    errorText.text = getString(R.string.name_setup_name_taken)
                },
                onError = { err ->
                    val firestoreCode = (err as? FirebaseFirestoreException)?.code?.name ?: "N/A"
                    Log.e(
                        TAG,
                        "[createCurrentPlayer] type=${err::class.java.simpleName} code=$firestoreCode message=${err.message}",
                        err
                    )
                    loading.visibility = android.view.View.GONE
                    btnConfirm.isEnabled = true
                    val message = if (BuildConfig.DEBUG) {
                        "${getString(R.string.name_setup_save_error)}\n${err.message ?: "Brak szczegolow"}"
                    } else {
                        getString(R.string.name_setup_save_error)
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}

