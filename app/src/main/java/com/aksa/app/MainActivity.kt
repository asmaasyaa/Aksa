package com.aksa.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.aksa.app.navigation.AksaNavGraph
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AksaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AksaOrange
                ) {
                    AksaNavGraph()
                }
            }
        }
    }
}
