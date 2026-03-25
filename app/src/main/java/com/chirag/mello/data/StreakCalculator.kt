package com.chirag.mello.data

import java.util.Calendar

object StreakCalculator {

    fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val days = timestamps.map { ts ->
            val cal = Calendar.getInstance().apply { timeInMillis = ts }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR), ts)
        }.distinctBy { it.first to it.second }
            .sortedByDescending { it.third }

        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayDay = today.get(Calendar.DAY_OF_YEAR)

        // Most recent entry must be today or yesterday
        val mostRecent = days.firstOrNull() ?: return 0
        val diff = dayDiff(todayYear, todayDay, mostRecent.first, mostRecent.second)
        if (diff > 1) return 0

        var streak = 1
        for (i in 0 until days.size - 1) {
            val curr = days[i]
            val next = days[i + 1]
            val d = dayDiff(curr.first, curr.second, next.first, next.second)
            if (d == 1) streak++ else break
        }
        return streak
    }

    private fun dayDiff(y1: Int, d1: Int, y2: Int, d2: Int): Int {
        val total1 = y1 * 365 + d1
        val total2 = y2 * 365 + d2
        return total1 - total2
    }
}
