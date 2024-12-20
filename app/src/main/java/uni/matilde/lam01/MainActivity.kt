package uni.matilde.lam01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uni.matilde.lam01.ui.map.MainMapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainMapScreen()
        }
    }
}
