package com.example.badbudget

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.CircularProgressIndicator

class AwardsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var textViewStreak: TextView
    private lateinit var textViewBadge: TextView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var starContainer: LinearLayout
    private lateinit var btnPrevMedals: ImageButton
    private lateinit var btnNextMedals: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_awards)

        backButton        = findViewById(R.id.backButton)
        textViewStreak    = findViewById(R.id.textViewStreak)
        textViewBadge     = findViewById(R.id.textViewBadge)
        progressIndicator = findViewById(R.id.progressIndicator)
        starContainer     = findViewById(R.id.starContainer)
        btnPrevMedals     = findViewById(R.id.btnPrevMedals)
        btnNextMedals     = findViewById(R.id.btnNextMedals)

        backButton.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        // Load stats
        StatisticsService.getUserStats(UserSession.id(this)) { stats ->
            runOnUiThread {
                // 1) streak
                textViewStreak.text = "🔥 ${stats.streak}-day streak!"

                // 2) latest badge
                textViewBadge.text = stats.badges.lastOrNull() ?: "No badge yet"

                // 3) progress (out of 500)
                progressIndicator.max = 500
                progressIndicator.progress = stats.points.coerceAtMost(500)

                // 4) stars: one star per 100 points
                val stars = (stats.points / 100).coerceIn(0, 5)
                renderStars(stars)
            }
        }

        // (You can wire up prev/next medal buttons here if desired)
    }

    private fun renderStars(starCount: Int) {
        starContainer.removeAllViews()

        // convert 24dp and 4dp to px
        val sizePx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 24f, resources.displayMetrics
        ).toInt()
        val marginPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics
        ).toInt()

        for (i in 1..5) {
            val iv = ImageView(this).apply {
                setImageResource(
                    if (i <= starCount) R.drawable.ic_star
                    else                 R.drawable.ic_star_outline
                )
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).apply {
                    rightMargin = marginPx
                }
                contentDescription = if (i <= starCount) "Filled star" else "Empty star"
            }
            starContainer.addView(iv)
        }
    }
}
