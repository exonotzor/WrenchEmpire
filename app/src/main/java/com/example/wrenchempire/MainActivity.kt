package com.example.wrenchempire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.wrenchempire.ui.screen.GameScreen
import com.example.wrenchempire.ui.screen.LanguageScreen
import com.example.wrenchempire.ui.theme.WrenchEmpireTheme
import com.example.wrenchempire.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WrenchEmpireTheme {
                val state by vm.state.collectAsState()

                if (state.isFirstLaunch || state.language.isEmpty()) {
                    LanguageScreen(onLanguageSelected = { lang -> vm.setLanguage(lang) })
                } else {
                    GameScreen(vm = vm)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.saveNow()
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.saveNow()
    }
}
