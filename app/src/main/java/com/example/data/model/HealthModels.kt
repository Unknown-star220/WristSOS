package com.example.data.model

data class DailyStats(
    val date: String, // e.g. "2026-08-06"
    val steps: Int,
    val stepGoal: Int = 10000,
    val calories: Int,
    val calorieGoal: Int = 500,
    val activeMinutes: Int,
    val activeGoalMinutes: Int = 60,
    val deepSleepMinutes: Int = 210,
    val lightSleepMinutes: Int = 190
)

data class BadgeModel(
    val id: String,
    val title: String,
    val description: String,
    val requiredSteps: Int,
    val iconName: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long = 0L
)

enum class SleepQuality {
    EXCELLENT, GOOD, RESTLESS, POOR
}
