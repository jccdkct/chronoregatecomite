package com.example.chronocoursejc2

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.chronocoursejc2.ui.MainScreen
import com.example.chronocoursejc2.ui.MainViewModel
import com.example.chronocoursejc2.ui.RaceState
import com.example.chronocoursejc2.ui.theme.Chronocoursejc2Theme
import kotlinx.serialization.Serializable

@Serializable
object Home : NavKey

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock Portrait Orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Keep Screen On
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Set Brightness to 100%
        val lp = window.attributes
        lp.screenBrightness = 1.0f
        window.attributes = lp

        enableEdgeToEdge()
        
        setContent {
            Chronocoursejc2Theme {
                val backStack = rememberNavBackStack(Home)
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is Home -> NavEntry(key) { MainScreen(viewModel) }
                            else -> error("Unknown key $key")
                        }
                    }
                )
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            val state = viewModel.raceState.value
            if (state == RaceState.IDLE || state == RaceState.READY) {
                viewModel.triggerStartAction()
            } else if (state == RaceState.RUNNING || state == RaceState.COUNTDOWN) {
                viewModel.recordArrival()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
