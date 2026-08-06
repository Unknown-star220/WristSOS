package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val steps: Int,
    val stepGoal: Int = 10000,
    val calories: Int,
    val calorieGoal: Int = 500,
    val activeMinutes: Int,
    val activeGoalMinutes: Int = 60,
    val deepSleepMinutes: Int = 210,
    val lightSleepMinutes: Int = 190
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val melodyName: String,
    val repeatDaysCsv: String // e.g. "Mon,Tue,Wed,Thu,Fri"
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val requiredSteps: Int,
    val iconName: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long
)

@Entity(tableName = "app_filters")
data class AppFilterEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val iconCategory: String,
    val isMuted: Boolean
)
