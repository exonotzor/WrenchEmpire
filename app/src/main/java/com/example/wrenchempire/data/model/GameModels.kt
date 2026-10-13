package com.example.wrenchempire.data.model

import kotlinx.serialization.Serializable

// ─── Multiplier Mode (x1, x10, x50, Max) ────────────────────────────────────
enum class MultiplierMode(val label: String) {
    X1("x1"),
    X10("x10"),
    X50("x50"),
    MAX("MAX")
}

data class BulkUpgradeInfo(
    val count: Int,
    val totalCost: Double,
    val canAfford: Boolean
)

// ─── Worker Tier (visual label only) ─────────────────────────────────────────
enum class WorkerTier(val label: String, val emoji: String) {
    NONE("", ""),
    APPRENTICE("Apprentice", "👦"),
    JOURNEYMAN("Journeyman", "🔧"),
    MASTER("Master", "⚙️"),
    LEGEND("Legend", "⭐")
}

fun workerTierForLevel(level: Int): WorkerTier = when {
    level == 0  -> WorkerTier.NONE
    level <= 25 -> WorkerTier.APPRENTICE
    level <= 50 -> WorkerTier.JOURNEYMAN
    level <= 75 -> WorkerTier.MASTER
    else        -> WorkerTier.LEGEND
}

// Speed: 1 + level*0.05  →  level 100 = 6×
fun speedMultiplier(level: Int): Double = 1.0 + level * 0.05

// Earnings: 1 + level*0.03  →  level 100 = 4×
fun earningsMultiplier(level: Int): Double = 1.0 + level * 0.03

// Base cost for single worker upgrade
fun workerUpgradeCost(currentLevel: Int): Double =
    if (currentLevel == 0) 0.0
    else 50.0 * Math.pow(1.18, currentLevel.toDouble())

// Bulk Worker Upgrade Calculation
fun calculateWorkerBulkUpgrade(
    currentLevel: Int,
    mode: MultiplierMode,
    availableMoney: Double
): BulkUpgradeInfo {
    if (currentLevel >= 100) return BulkUpgradeInfo(0, 0.0, false)
    val r = 1.18
    val maxRemaining = 100 - currentLevel
    val singleCost = workerUpgradeCost(currentLevel)

    val count = when (mode) {
        MultiplierMode.X1 -> 1
        MultiplierMode.X10 -> 10.coerceAtMost(maxRemaining)
        MultiplierMode.X50 -> 50.coerceAtMost(maxRemaining)
        MultiplierMode.MAX -> {
            if (availableMoney < singleCost) 1
            else {
                // S = c0 * (r^k - 1) / (r - 1) <= M
                val k = Math.floor(Math.log(1.0 + (availableMoney * (r - 1.0) / singleCost)) / Math.log(r)).toInt()
                k.coerceIn(1, maxRemaining)
            }
        }
    }

    val totalCost = singleCost * (Math.pow(r, count.toDouble()) - 1.0) / (r - 1.0)
    return BulkUpgradeInfo(count, totalCost, availableMoney >= totalCost)
}

// ─── Job Station Level & Upgrades ───────────────────────────────────────────
// Job upgrade cost: starts at $4.00 for Tire Change (2 clicks @ $2)
fun jobUpgradeCost(def: JobDefinition, currentJobLevel: Int): Double {
    val baseCost = if (def.unlockCost <= 0.0) {
        def.baseReward * 2.0 // e.g. Tire Change: $2.0 * 2 = $4.00!
    } else {
        (def.unlockCost * 0.30).coerceAtLeast(def.baseReward * 2.0)
    }
    return baseCost * Math.pow(1.16, (currentJobLevel - 1).toDouble())
}

// Bulk Job Station Upgrade Calculation
fun calculateJobBulkUpgrade(
    def: JobDefinition,
    currentJobLevel: Int,
    mode: MultiplierMode,
    availableMoney: Double
): BulkUpgradeInfo {
    val r = 1.16
    val singleCost = jobUpgradeCost(def, currentJobLevel)

    val count = when (mode) {
        MultiplierMode.X1 -> 1
        MultiplierMode.X10 -> 10
        MultiplierMode.X50 -> 50
        MultiplierMode.MAX -> {
            if (availableMoney < singleCost) 1
            else {
                val k = Math.floor(Math.log(1.0 + (availableMoney * (r - 1.0) / singleCost)) / Math.log(r)).toInt()
                k.coerceAtLeast(1)
            }
        }
    }

    val totalCost = singleCost * (Math.pow(r, count.toDouble()) - 1.0) / (r - 1.0)
    return BulkUpgradeInfo(count, totalCost, availableMoney >= totalCost)
}

// Reward scales with job level (+45% per level)
fun jobReward(def: JobDefinition, jobLevel: Int): Double =
    def.baseReward * (1.0 + (jobLevel - 1) * 0.45)

// Duration gets shorter (faster) with job level
fun jobDurationMs(def: JobDefinition, jobLevel: Int): Long {
    val speedUp = 1.0 + (jobLevel - 1) * 0.08
    return (def.baseDurationMs / speedUp).toLong().coerceAtLeast(350L)
}

// ─── Job State ────────────────────────────────────────────────────────────────
@Serializable
data class JobState(
    val id: String,
    val isUnlocked: Boolean = false,
    val jobLevel: Int = 1,               // Station level (1, 2, 3...)
    val workerLevel: Int = 0,            // 0 = no worker; 1..100 = automated worker
    val progress: Float = 0f,            // 0.0 – 1.0
    val isManuallyRunning: Boolean = false,
    val totalEarned: Double = 0.0,       // lifetime earnings for this job
    val completions: Int = 0
)

// ─── Offline Vault Level ──────────────────────────────────────────────────────
object OfflineVault {
    val caps = listOf(2, 4, 8, 16, 24)   // hours per level
    val costs = listOf(10_000.0, 1_000_000.0, 1_000_000_000.0, 1_000_000_000_000.0)

    fun capHours(level: Int): Int = caps.getOrElse(level) { 24 }
    fun upgradeCost(currentLevel: Int): Double? = costs.getOrNull(currentLevel)
}

// ─── Shop State ───────────────────────────────────────────────────────────────
@Serializable
data class ShopState(
    val jobs: List<JobState> = JobDefinitions.all.map {
        JobState(id = it.id, isUnlocked = it.unlockCost == 0.0, jobLevel = 1)
    },
    val money: Double = 0.0,
    val offlineVaultLevel: Int = 0,
    val lastSaveTimestamp: Long = 0L
)

// ─── Root Game State ──────────────────────────────────────────────────────────
@Serializable
data class GameState(
    val shop: ShopState = ShopState(),
    val language: String = "",           // empty = not yet selected
    val isFirstLaunch: Boolean = true
)
