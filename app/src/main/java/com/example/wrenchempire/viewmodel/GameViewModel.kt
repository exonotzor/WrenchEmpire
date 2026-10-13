package com.example.wrenchempire.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wrenchempire.data.GameRepository
import com.example.wrenchempire.data.OfflineCalculator
import com.example.wrenchempire.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TICK_MS = 100L
private const val PASSIVE_RATE = 0.20   // 20% speed without a worker

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = GameRepository(application)

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val _multiplierMode = MutableStateFlow(MultiplierMode.X1)
    val multiplierMode: StateFlow<MultiplierMode> = _multiplierMode.asStateFlow()

    /** Non-null when the offline earnings dialog should be shown */
    private val _offlineEarned = MutableStateFlow<Double?>(null)
    val offlineEarned: StateFlow<Double?> = _offlineEarned.asStateFlow()

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            // 1. Load saved state
            val saved = repo.gameStateFlow.first()
            val lastClose = repo.lastCloseTimestamp.first()

            // 2. Calculate offline earnings
            val elapsedMs = System.currentTimeMillis() - lastClose
            if (elapsedMs > 5_000 && saved.shop.jobs.any { it.workerLevel > 0 }) {
                val (updatedShop, earned) = OfflineCalculator.calculate(saved.shop, elapsedMs)
                _state.value = saved.copy(shop = updatedShop)
                if (earned > 0.01) _offlineEarned.value = earned
            } else {
                _state.value = saved
            }

            // 3. Start the game loop
            startTick()
        }
    }

    fun setMultiplierMode(mode: MultiplierMode) {
        _multiplierMode.value = mode
    }

    // ─── Game Loop ────────────────────────────────────────────────────────────

    private fun startTick() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(TICK_MS)
                tick(TICK_MS)
            }
        }
    }

    private fun tick(deltaMs: Long) {
        _state.update { gs ->
            var totalReward = 0.0
            val updatedJobs = gs.shop.jobs.map { job ->
                if (!job.isUnlocked) return@map job

                val def = JobDefinitions.byId[job.id] ?: return@map job
                val hasWorker = job.workerLevel > 0
                val shouldRun = hasWorker || job.isManuallyRunning

                if (!shouldRun) return@map job

                val duration = jobDurationMs(def, job.jobLevel)
                val speed = if (hasWorker) speedMultiplier(job.workerLevel) else PASSIVE_RATE
                val progressDelta = (deltaMs.toDouble() / duration * speed).toFloat()
                val newProgress = job.progress + progressDelta

                if (newProgress >= 1f) {
                    val baseReward = jobReward(def, job.jobLevel)
                    val earnMultiplier = earningsMultiplier(if (hasWorker) job.workerLevel else 0)
                    val reward = baseReward * earnMultiplier
                    totalReward += reward
                    // Carry over leftover progress
                    job.copy(
                        progress = (newProgress - 1f).coerceAtLeast(0f),
                        totalEarned = job.totalEarned + reward,
                        completions = job.completions + 1,
                        isManuallyRunning = hasWorker // keep running only if worker present
                    )
                } else {
                    job.copy(progress = newProgress)
                }
            }

            gs.copy(
                shop = gs.shop.copy(
                    jobs = updatedJobs,
                    money = gs.shop.money + totalReward
                )
            )
        }
    }

    // ─── Player Actions ───────────────────────────────────────────────────────

    /** Player taps a job to start manually running it (no worker needed). */
    fun startJob(jobId: String) {
        _state.update { gs ->
            val jobs = gs.shop.jobs.map { job ->
                if (job.id != jobId || !job.isUnlocked) job
                else job.copy(isManuallyRunning = true)
            }
            gs.copy(shop = gs.shop.copy(jobs = jobs))
        }
    }

    /** Upgrade the job station itself with support for x1, x10, x50, and Max multipliers. */
    fun upgradeJob(jobId: String) {
        val def = JobDefinitions.byId[jobId] ?: return
        _state.update { gs ->
            val shop = gs.shop
            val job = shop.jobs.find { it.id == jobId } ?: return@update gs
            if (!job.isUnlocked) return@update gs

            val bulk = calculateJobBulkUpgrade(def, job.jobLevel, _multiplierMode.value, shop.money)
            if (!bulk.canAfford || bulk.count <= 0) return@update gs

            val jobs = shop.jobs.map { j ->
                if (j.id == jobId) j.copy(jobLevel = j.jobLevel + bulk.count) else j
            }
            gs.copy(shop = shop.copy(jobs = jobs, money = shop.money - bulk.totalCost))
        }
    }

    /** Unlock a job by spending money. */
    fun unlockJob(jobId: String) {
        val def = JobDefinitions.byId[jobId] ?: return
        _state.update { gs ->
            val shop = gs.shop
            if (shop.money < def.unlockCost) return@update gs
            val jobs = shop.jobs.map { job ->
                if (job.id == jobId) job.copy(isUnlocked = true) else job
            }
            gs.copy(shop = shop.copy(jobs = jobs, money = shop.money - def.unlockCost))
        }
    }

    /** Hire the first worker for a job (level 0 → 1). */
    fun hireWorker(jobId: String) {
        val def = JobDefinitions.byId[jobId] ?: return
        _state.update { gs ->
            val shop = gs.shop
            val job = shop.jobs.find { it.id == jobId } ?: return@update gs
            if (!job.isUnlocked || job.workerLevel > 0) return@update gs
            if (shop.money < def.workerHireCost) return@update gs
            val jobs = shop.jobs.map { j ->
                if (j.id == jobId) j.copy(workerLevel = 1, isManuallyRunning = false) else j
            }
            gs.copy(shop = shop.copy(jobs = jobs, money = shop.money - def.workerHireCost))
        }
    }

    /** Upgrade an existing worker with support for x1, x10, x50, and Max multipliers. */
    fun upgradeWorker(jobId: String) {
        _state.update { gs ->
            val shop = gs.shop
            val job = shop.jobs.find { it.id == jobId } ?: return@update gs
            if (job.workerLevel == 0 || job.workerLevel >= 100) return@update gs

            val bulk = calculateWorkerBulkUpgrade(job.workerLevel, _multiplierMode.value, shop.money)
            if (!bulk.canAfford || bulk.count <= 0) return@update gs

            val jobs = shop.jobs.map { j ->
                if (j.id == jobId) j.copy(workerLevel = j.workerLevel + bulk.count) else j
            }
            gs.copy(shop = shop.copy(jobs = jobs, money = shop.money - bulk.totalCost))
        }
    }

    /** Upgrade the offline vault cap. */
    fun upgradeOfflineVault() {
        _state.update { gs ->
            val shop = gs.shop
            val cost = OfflineVault.upgradeCost(shop.offlineVaultLevel) ?: return@update gs
            if (shop.money < cost) return@update gs
            gs.copy(
                shop = shop.copy(
                    money = shop.money - cost,
                    offlineVaultLevel = shop.offlineVaultLevel + 1
                )
            )
        }
    }

    /** Dismiss the offline earnings dialog. */
    fun dismissOfflineDialog() { _offlineEarned.value = null }

    /** Called from the language picker screen. */
    fun setLanguage(lang: String) {
        _state.update { it.copy(language = lang, isFirstLaunch = false) }
        saveNow()
    }

    // ─── Persistence ─────────────────────────────────────────────────────────

    fun saveNow() {
        viewModelScope.launch {
            val current = _state.value
            repo.saveGameState(current)
            repo.saveLastCloseTimestamp(System.currentTimeMillis())
        }
    }
}
