package com.example.wrenchempire.data.model

// Static, immutable definition of each job
data class JobDefinition(
    val id: String,
    val nameKey: String,           // maps to strings resource key
    val emoji: String,
    val baseDurationMs: Long,      // ms to complete at speed 1×
    val baseReward: Double,        // $ per completion at earnings 1×
    val unlockCost: Double,        // money needed to unlock (0 = free)
    val workerHireCost: Double     // money needed to hire first worker
)

object JobDefinitions {
    val all: List<JobDefinition> = listOf(
        JobDefinition("tire_change",         "job_tire_change",          "🛞", 5_000,    2.0,           0.0,        20.0),
        JobDefinition("wheel_balance",       "job_wheel_balance",        "⚖️", 10_000,   5.0,           50.0,       80.0),
        JobDefinition("oil_change",          "job_oil_change",           "🛢️", 15_000,   10.0,          200.0,      300.0),
        JobDefinition("battery_replace",     "job_battery_replace",      "🔋", 20_000,   18.0,          500.0,      700.0),
        JobDefinition("brake_pads",          "job_brake_pads",           "🛑", 25_000,   28.0,          1_000.0,    1_500.0),
        JobDefinition("headlight_adj",       "job_headlight_adj",        "💡", 20_000,   22.0,          2_000.0,    3_000.0),
        JobDefinition("filter_replace",      "job_filter_replace",       "🌬️", 30_000,   35.0,          5_000.0,    7_500.0),
        JobDefinition("exhaust_repair",      "job_exhaust_repair",       "💨", 45_000,   60.0,          12_000.0,   18_000.0),
        JobDefinition("ac_recharge",         "job_ac_recharge",          "❄️", 50_000,   80.0,          25_000.0,   37_500.0),
        JobDefinition("shock_absorber",      "job_shock_absorber",       "🔩", 60_000,   110.0,         50_000.0,   75_000.0),
        JobDefinition("windshield",          "job_windshield",           "🪟", 70_000,   150.0,         100_000.0,  150_000.0),
        JobDefinition("rim_repair",          "job_rim_repair",           "🎡", 80_000,   180.0,         200_000.0,  300_000.0),
        JobDefinition("carburetor",          "job_carburetor",           "🔬", 90_000,   230.0,         400_000.0,  600_000.0),
        JobDefinition("differential",        "job_differential",         "⚙️", 120_000,  350.0,         800_000.0,  1_200_000.0),
        JobDefinition("electrical_system",   "job_electrical_system",    "⚡", 150_000,  450.0,         1_500_000.0,2_250_000.0),
        JobDefinition("gearbox",             "job_gearbox",              "🔧", 180_000,  650.0,         3_000_000.0,4_500_000.0),
        JobDefinition("brake_overhaul",      "job_brake_overhaul",       "🚗", 210_000,  850.0,         6_000_000.0,9_000_000.0),
        JobDefinition("bodywork",            "job_bodywork",             "🔨", 240_000,  1_100.0,       12_000_000.0,18_000_000.0),
        JobDefinition("turbo",               "job_turbo",                "🌀", 300_000,  1_600.0,       25_000_000.0,37_500_000.0),
        JobDefinition("engine_overhaul",     "job_engine_overhaul",      "🏗️", 480_000,  3_000.0,       50_000_000.0,75_000_000.0),
        JobDefinition("engine_replace",      "job_engine_replace",       "🚀", 600_000,  5_000.0,       100_000_000.0,150_000_000.0),
        JobDefinition("chassis_repair",      "job_chassis_repair",       "🏭", 900_000,  10_000.0,      250_000_000.0,375_000_000.0)
    )

    val byId: Map<String, JobDefinition> = all.associateBy { it.id }
}
