package com.example.wrenchempire.data

import com.example.wrenchempire.data.model.JobDefinitions
import com.example.wrenchempire.data.model.JobState
import com.example.wrenchempire.data.model.OfflineVault
import com.example.wrenchempire.data.model.ShopState
import com.example.wrenchempire.data.model.earningsMultiplier
import com.example.wrenchempire.data.model.jobDurationMs
import com.example.wrenchempire.data.model.jobReward
import com.example.wrenchempire.data.model.speedMultiplier

object OfflineCalculator {

    /**
     * Given the shop state and elapsed real-world milliseconds since the app closed,
     * returns a new ShopState with money added (capped by offlineVaultLevel) and
     * the raw dollar amount earned (for the popup).
     */
    fun calculate(shop: ShopState, elapsedMs: Long): Pair<ShopState, Double> {
        val capMs = OfflineVault.capHours(shop.offlineVaultLevel) * 3_600_000L
        val effectiveMs = elapsedMs.coerceAtMost(capMs)

        var totalEarned = 0.0
        val updatedJobs = shop.jobs.map { job ->
            if (!job.isUnlocked || job.workerLevel == 0) return@map job

            val def = JobDefinitions.byId[job.id] ?: return@map job
            val duration = jobDurationMs(def, job.jobLevel)
            val speed  = speedMultiplier(job.workerLevel)
            val earn   = earningsMultiplier(job.workerLevel)

            // How many completions fit in effectiveMs?
            val effectiveDuration = (duration / speed).coerceAtLeast(200.0).toLong()
            val completions = (effectiveMs / effectiveDuration).toInt()
            val baseReward = jobReward(def, job.jobLevel)
            val earned = completions * baseReward * earn

            totalEarned += earned
            job.copy(
                completions = job.completions + completions,
                totalEarned = job.totalEarned + earned,
                progress = 0f      // reset progress on resume
            )
        }

        return Pair(
            shop.copy(jobs = updatedJobs, money = shop.money + totalEarned),
            totalEarned
        )
    }
}
