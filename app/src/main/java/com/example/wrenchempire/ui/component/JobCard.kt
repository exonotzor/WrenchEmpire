package com.example.wrenchempire.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wrenchempire.data.model.*
import com.example.wrenchempire.ui.theme.*
import com.example.wrenchempire.util.formatMoney
import com.example.wrenchempire.util.jobName

@Composable
fun JobCard(
    def: JobDefinition,
    job: JobState,
    shopMoney: Double,
    multiplierMode: MultiplierMode,
    onUnlock: () -> Unit,
    onStart: () -> Unit,
    onUpgradeJob: () -> Unit,
    onHireWorker: () -> Unit,
    onUpgradeWorker: () -> Unit
) {
    val tier = workerTierForLevel(job.workerLevel)
    val borderColor = when {
        !job.isUnlocked        -> NesCardBorder
        tier == WorkerTier.NONE-> NesYellow
        tier == WorkerTier.APPRENTICE -> TierApprenticeColor
        tier == WorkerTier.JOURNEYMAN -> TierJourneymanColor
        tier == WorkerTier.MASTER     -> TierMasterColor
        else                          -> TierLegendColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor)
            .background(NesDarkCard)
            .padding(12.dp)
    ) {
        if (!job.isUnlocked) {
            LockedJobContent(def, shopMoney, onUnlock)
        } else {
            UnlockedJobContent(
                def = def,
                job = job,
                shopMoney = shopMoney,
                multiplierMode = multiplierMode,
                onStart = onStart,
                onUpgradeJob = onUpgradeJob,
                onHireWorker = onHireWorker,
                onUpgradeWorker = onUpgradeWorker
            )
        }
    }
}

// ─── Locked Job ──────────────────────────────────────────────────────────────

@Composable
private fun LockedJobContent(
    def: JobDefinition,
    shopMoney: Double,
    onUnlock: () -> Unit
) {
    val canAfford = shopMoney >= def.unlockCost
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "🔒", fontSize = 28.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = jobName(def),
                style = MaterialTheme.typography.titleLarge,
                color = NesLightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "COST: $${formatMoney(def.unlockCost)}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (canAfford) NesGreen else NesRed
            )
        }
        Spacer(Modifier.width(8.dp))
        ChunkyNesButton(
            text = "UNLOCK",
            subText = "$${formatMoney(def.unlockCost)}",
            enabled = canAfford,
            color = if (canAfford) NesGreen else NesMidGray,
            onClick = onUnlock,
            modifier = Modifier.width(120.dp).height(48.dp)
        )
    }
}

// ─── Unlocked Job ────────────────────────────────────────────────────────────

@Composable
private fun UnlockedJobContent(
    def: JobDefinition,
    job: JobState,
    shopMoney: Double,
    multiplierMode: MultiplierMode,
    onStart: () -> Unit,
    onUpgradeJob: () -> Unit,
    onHireWorker: () -> Unit,
    onUpgradeWorker: () -> Unit
) {
    val hasWorker = job.workerLevel > 0
    val tier = workerTierForLevel(job.workerLevel)
    val currentReward = jobReward(def, job.jobLevel) * earningsMultiplier(job.workerLevel)
    val durationSeconds = (jobDurationMs(def, job.jobLevel) / 1000.0)

    // Dynamic Bulk Upgrade for Station
    val jobBulk = calculateJobBulkUpgrade(def, job.jobLevel, multiplierMode, shopMoney)

    // Worker Hire / Bulk Upgrade
    val workerHireCost = def.workerHireCost
    val canHireWorker = shopMoney >= workerHireCost
    val workerBulk = calculateWorkerBulkUpgrade(job.workerLevel, multiplierMode, shopMoney)

    Column {
        // ── 1. Header: Name & Reward ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = def.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = jobName(def),
                style = MaterialTheme.typography.titleMedium,
                color = NesWhite,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Reward badge
            Box(
                modifier = Modifier
                    .background(NesNearBlack)
                    .border(1.dp, NesGreen)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+$${formatMoney(currentReward)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = NesGreen,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── 2. Subheader: Station Level Badge + Duration + Auto Status ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(NesYellow.copy(alpha = 0.2f))
                        .border(1.dp, NesYellow)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LV.${job.jobLevel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NesYellow
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "⏱️ ${String.format("%.1f", durationSeconds)}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = NesLightGray
                )
            }

            Text(
                text = if (hasWorker) "${tier.emoji} CREW Lv.${job.workerLevel}" else "MANUAL",
                style = MaterialTheme.typography.bodySmall,
                color = if (hasWorker) workerColor(tier) else NesLightGray
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── 3. Chunky Progress Bar ──
        ChunkyProgressBar(
            progress = job.progress,
            color = if (hasWorker) workerColor(tier) else NesCyan,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        // ── 4. Main Actions: Work + Station Upgrade (Row 1) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Button A: Work manually (or auto indicator)
            if (!hasWorker) {
                ChunkyNesButton(
                    text = if (job.isManuallyRunning) "WORKING…" else "🔨 WORK",
                    subText = if (job.isManuallyRunning) "IN PROGRESS" else "TAP TO START",
                    enabled = !job.isManuallyRunning,
                    color = if (job.isManuallyRunning) NesMidGray else NesCyan,
                    onClick = onStart,
                    modifier = Modifier.weight(0.42f).height(50.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .height(50.dp)
                        .border(2.dp, workerColor(tier))
                        .background(workerColor(tier).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🟢 AUTO CREW",
                            style = MaterialTheme.typography.labelSmall,
                            color = workerColor(tier)
                        )
                        Text(
                            text = "WORKING",
                            style = MaterialTheme.typography.bodySmall,
                            color = NesWhite
                        )
                    }
                }
            }

            // Button B: Upgrade Job Station Level (Dynamic Bulk Buy)
            val stationUpText = "⬆️ UP (+${jobBulk.count})"
            ChunkyNesButton(
                text = stationUpText,
                subText = "$${formatMoney(jobBulk.totalCost)}",
                enabled = jobBulk.canAfford,
                color = if (jobBulk.canAfford) NesYellow else NesMidGray,
                onClick = onUpgradeJob,
                modifier = Modifier.weight(0.58f).height(50.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── 5. Worker Automation Action (Row 2 - Full Width) ──
        if (!hasWorker) {
            ChunkyNesButton(
                text = "👦 HIRE APPRENTICE",
                subText = "AUTOMATE FOR $${formatMoney(workerHireCost)}",
                enabled = canHireWorker,
                color = if (canHireWorker) NesOrange else NesMidGray,
                onClick = onHireWorker,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )
        } else if (job.workerLevel < 100) {
            val crewUpText = "⚡ UP CREW (+${workerBulk.count})"
            ChunkyNesButton(
                text = crewUpText,
                subText = "$${formatMoney(workerBulk.totalCost)} (Lv.${job.workerLevel + workerBulk.count})",
                enabled = workerBulk.canAfford,
                color = if (workerBulk.canAfford) NesEmerald else NesMidGray,
                onClick = onUpgradeWorker,
                modifier = Modifier.fillMaxWidth().height(46.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .border(2.dp, NesGold)
                    .background(NesGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⭐ CREW MAX LEVEL (100)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NesGold
                )
            }
        }
    }
}

// ─── Reusable Chunky NES Primitives ──────────────────────────────────────────

@Composable
fun ChunkyNesButton(
    text: String,
    subText: String = "",
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color = NesYellow,
    modifier: Modifier = Modifier
) {
    val effectiveColor = if (enabled) color else NesMidGray
    Box(
        modifier = modifier
            .border(2.dp, effectiveColor)
            .background(if (enabled) effectiveColor.copy(alpha = 0.22f) else NesDarkCard)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) NesWhite else NesMidGray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subText.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodySmall,
                    color = effectiveColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChunkyProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(14.dp)
            .background(NesNearBlack)
            .border(2.dp, NesCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(color)
        )
    }
}

private fun workerColor(tier: WorkerTier) = when (tier) {
    WorkerTier.APPRENTICE -> TierApprenticeColor
    WorkerTier.JOURNEYMAN -> TierJourneymanColor
    WorkerTier.MASTER     -> TierMasterColor
    WorkerTier.LEGEND     -> TierLegendColor
    WorkerTier.NONE       -> NesCyan
}
