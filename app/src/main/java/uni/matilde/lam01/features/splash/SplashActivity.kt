package uni.matilde.lam01.features.splash

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import uni.matilde.lam01.core.app.MainActivity
import uni.matilde.lam01.R

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creazione del layout programmaticamente
        val layout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
        }

        val logo1 = ImageView(this).apply {
            setImageResource(R.drawable.soundmap_logo_black)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                height = 200 // Altezza personalizzata per ridimensionare
            }
        }

        val logo2 = ImageView(this).apply {
            setImageResource(R.drawable.soundmap_title)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                height = 150 // Altezza personalizzata per ridimensionare
            }
        }

        layout.addView(logo1)
        layout.addView(logo2)

        setContentView(layout)

        // Avvia la MainActivity dopo la splash screen
        layout.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) // Durata della splash screen (2 secondi)
    }
}