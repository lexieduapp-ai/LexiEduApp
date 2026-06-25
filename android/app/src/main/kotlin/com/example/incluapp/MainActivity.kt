package com.example.incluapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.incluapp.navigation.AppNavGraph
import com.example.incluapp.ui.theme.LexiEduTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiEduTheme {
                AppNavGraph(
                    container = (application as LexiEduApplication).container
                )
            }
        }
    }
}
