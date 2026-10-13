package com.example.wrenchempire.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wrenchempire.ui.theme.*

data class LanguageOption(val code: String, val label: String, val flag: String)

private val languages = listOf(
    LanguageOption("en", "English",  "🇬🇧"),
    LanguageOption("tr", "Türkçe",   "🇹🇷"),
    LanguageOption("de", "Deutsch",  "🇩🇪"),
    LanguageOption("es", "Español",  "🇪🇸"),
    LanguageOption("fr", "Français", "🇫🇷"),
)

@Composable
fun LanguageScreen(onLanguageSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NesNearBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "🔧",
            fontSize = 54.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "WRENCH EMPIRE",
            style = MaterialTheme.typography.displayMedium,
            color = NesYellow,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(4.dp)
                .background(NesYellow)
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = "SELECT LANGUAGE",
            style = MaterialTheme.typography.titleLarge,
            color = NesLightGray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        languages.forEach { lang ->
            LanguageButton(lang, onClick = { onLanguageSelected(lang.code) })
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun LanguageButton(lang: LanguageOption, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(58.dp)
            .border(2.dp, NesYellow)
            .background(NesDarkCard)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = lang.flag, fontSize = 26.sp)
            Spacer(Modifier.width(14.dp))
            Text(
                text = lang.label,
                style = MaterialTheme.typography.titleLarge,
                color = NesWhite
            )
        }
    }
}
