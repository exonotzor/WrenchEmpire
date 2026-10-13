package com.example.wrenchempire.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.wrenchempire.ui.theme.*
import com.example.wrenchempire.util.formatMoney

/** Shown when the player reopens the app after being away. */
@Composable
fun OfflineEarningsDialog(earned: Double, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(3.dp, NesYellow)
                .background(NesNearBlack)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💰", fontSize = 48.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "WELCOME BACK!",
                style = MaterialTheme.typography.headlineLarge,
                color = NesYellow,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your crew kept working!",
                style = MaterialTheme.typography.bodyMedium,
                color = NesLightGray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "EARNED:",
                style = MaterialTheme.typography.bodyMedium,
                color = NesWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "+$${formatMoney(earned)}",
                style = MaterialTheme.typography.displayMedium,
                color = NesGreen
            )
            Spacer(Modifier.height(24.dp))
            ChunkyNesButton(
                text = "💰 COLLECT!",
                color = NesGreen,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            )
        }
    }
}
