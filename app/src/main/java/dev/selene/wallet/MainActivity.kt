package dev.selene.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.selene.wallet.ui.SeleneApp
import dev.selene.wallet.ui.SeleneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeleneTheme {
                SeleneApp()
            }
        }
    }
}