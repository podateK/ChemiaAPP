package dev.podatek.chemia

import android.animation.ValueAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import dev.podatek.chemia.data.FirebaseRankingRepository

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        try {
            val progressBar = findViewById<ProgressBar>(R.id.loading_progress)
            

            animateProgressBar(progressBar)

            Handler(Looper.getMainLooper()).postDelayed({
                navigateByAccountState()
            }, 2500)
        } catch (e: Exception) {
            e.printStackTrace()

            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun navigateByAccountState() {
        FirebaseRankingRepository.isCurrentPlayerRegistered(
            context = this,
            onResult = { exists ->
                val target = if (exists) MenuActivity::class.java else NameSetupActivity::class.java
                val intent = Intent(this, target)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            },
            onError = {
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        )
    }

    private fun animateProgressBar(progressBar: ProgressBar) {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 2000
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }

        animator.start()
    }
}

