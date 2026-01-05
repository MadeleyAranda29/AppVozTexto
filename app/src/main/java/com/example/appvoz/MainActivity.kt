package com.example.appvoz

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.appvoz.ui.startApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startApp(this)
    }
}