package com.example.wrenchempire.util

import com.example.wrenchempire.data.model.JobDefinition
import java.util.Locale

/** Formats large money values with K / M / B / T suffixes using consistent decimals. */
fun formatMoney(value: Double): String {
    return when {
        value >= 1_000_000_000_000.0 -> String.format(Locale.US, "%.1fT", value / 1_000_000_000_000.0)
        value >= 1_000_000_000.0     -> String.format(Locale.US, "%.1fB", value / 1_000_000_000.0)
        value >= 1_000_000.0         -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        value >= 1_000.0             -> String.format(Locale.US, "%.1fK", value / 1_000.0)
        else                         -> String.format(Locale.US, "%.2f", value)
    }
}

/**
 * Returns the display name for a job.
 */
fun jobName(def: JobDefinition): String =
    def.id.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
