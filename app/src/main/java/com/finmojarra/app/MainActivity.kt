package com.finmojarra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finmojarra.app.navigation.FinMojarraNavHost
import com.finmojarra.app.ui.theme.FinMojarraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as FinMojarraApp).container
        setContent {
            FinMojarraTheme {
                FinMojarraNavHost(container = container)
            }
        }
    }
}
