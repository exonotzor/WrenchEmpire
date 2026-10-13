package com.example.wrenchempire.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wrenchempire.data.model.JobDefinitions
import com.example.wrenchempire.data.model.MultiplierMode
import com.example.wrenchempire.data.model.OfflineVault
import com.example.wrenchempire.ui.component.AnimatedWorkshopView
import com.example.wrenchempire.ui.component.ChunkyNesButton
import com.example.wrenchempire.ui.component.JobCard
import com.example.wrenchempire.ui.component.OfflineEarningsDialog
import com.example.wrenchempire.ui.theme.*
import com.example.wrenchempire.util.formatMoney
import com.example.wrenchempire.viewmodel.GameViewModel

@Composable
fun GameScreen(vm: GameViewModel) {
    val state by vm.state.collectAsState()
    val multiplierMode by vm.multiplierMode.collectAsState()
    val offlineEarned by vm.offlineEarned.collectAsState()
    val shop = state.shop

    // Count of jobs where a worker has been purchased
    val hiredWorkersCount = shop.jobs.count { it.isUnlocked && it.workerLevel > 0 }

    // Offline earnings dialog
    offlineEarned?.let { earned ->
        OfflineEarningsDialog(earned = earned, onDismiss = { vm.dismissOfflineDialog() })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NesNearBlack)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // ── 1. Top Bar: App Name & Money & Offline Vault ─────────────────────
        item(key = "header_top_bar") {
            TopBar(
                money = shop.money,
                offlineVaultLevel = shop.offlineVaultLevel,
                canUpgradeVault = OfflineVault.upgradeCost(shop.offlineVaultLevel)?.let { shop.money >= it } ?: false,
                vaultUpgradeCost = OfflineVault.upgradeCost(shop.offlineVaultLevel),
                onUpgradeVault = { vm.upgradeOfflineVault() }
            )
        }

        // ── 2. Multiplier Selector: x1, x10, x50, Max ────────────────────────
        item(key = "header_multiplier") {
            MultiplierSelectorRow(
                currentMode = multiplierMode,
                onModeSelected = { vm.setMultiplierMode(it) }
            )
        }

        // ── 3. Animated Unified Multi-Car Garage Scene ──────────────────────
        item(key = "header_workshop") {
            AnimatedWorkshopView(
                hiredWorkersCount = hiredWorkersCount,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // ── 4. Section Title ─────────────────────────────────────────────────
        item(key = "header_job_title") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 WORKSHOP STATIONS (22)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NesLightGray
                )
                Text(
                    text = "BUY: ${multiplierMode.label.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NesCyan
                )
            }
        }

        // ── 5. All 22 Job Control Cards ──────────────────────────────────────
        items(JobDefinitions.all, key = { it.id }) { def ->
            val jobState = shop.jobs.find { it.id == def.id } ?: return@items
            Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                JobCard(
                    def = def,
                    job = jobState,
                    shopMoney = shop.money,
                    multiplierMode = multiplierMode,
                    onUnlock        = { vm.unlockJob(def.id) },
                    onStart         = { vm.startJob(def.id) },
                    onUpgradeJob    = { vm.upgradeJob(def.id) },
                    onHireWorker    = { vm.hireWorker(def.id) },
                    onUpgradeWorker = { vm.upgradeWorker(def.id) }
                )
            }
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(
    money: Double,
    offlineVaultLevel: Int,
    canUpgradeVault: Boolean,
    vaultUpgradeCost: Double?,
    onUpgradeVault: () -> Unit
) {
    val capHours = OfflineVault.capHours(offlineVaultLevel)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NesDarkCard)
            .border(width = 2.dp, color = NesCardBorder)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            // Row 1: App Title (Left) and Large Money Counter (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⭐ WRENCH EMPIRE ⭐",
                        style = MaterialTheme.typography.titleLarge,
                        color = NesYellow,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "🔧 AUTO REPAIR GARAGE",
                        style = MaterialTheme.typography.bodySmall,
                        color = NesLightGray
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Big Money Counter Box (Never squished)
                Box(
                    modifier = Modifier
                        .background(NesNearBlack)
                        .border(2.dp, NesGreen)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$${formatMoney(money)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = NesGreen,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Row 2: Offline Vault Status & Upgrade Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰ OFFLINE: ${capHours}h",
                    style = MaterialTheme.typography.bodySmall,
                    color = NesWhite,
                    modifier = Modifier.weight(1f)
                )

                if (vaultUpgradeCost != null) {
                    val nextHours = OfflineVault.caps.getOrElse(offlineVaultLevel + 1) { 24 }
                    ChunkyNesButton(
                        text = "VAULT +${nextHours - capHours}h",
                        subText = "$${formatMoney(vaultUpgradeCost)}",
                        enabled = canUpgradeVault,
                        color = if (canUpgradeVault) NesCyan else NesMidGray,
                        onClick = onUpgradeVault,
                        modifier = Modifier.height(42.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .border(1.dp, NesGold)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "MAX 24h",
                            style = MaterialTheme.typography.labelSmall,
                            color = NesGold
                        )
                    }
                }
            }
        }
    }
}

// ─── Multiplier Selector Row: [x1] [x10] [x50] [MAX] ─────────────────────────

@Composable
private fun MultiplierSelectorRow(
    currentMode: MultiplierMode,
    onModeSelected: (MultiplierMode) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(NesDarkCard)
            .border(2.dp, NesCardBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "BUY MULTIPLIER:",
                style = MaterialTheme.typography.labelSmall,
                color = NesLightGray
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MultiplierMode.values().forEach { mode ->
                    val isSelected = (mode == currentMode)
                    Box(
                        modifier = Modifier
                            .border(2.dp, if (isSelected) NesYellow else NesMidGray)
                            .background(if (isSelected) NesYellow.copy(alpha = 0.25f) else Color.Transparent)
                            .clickable { onModeSelected(mode) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NesYellow else NesLightGray
                        )
                    }
                }
            }
        }
    }
}
