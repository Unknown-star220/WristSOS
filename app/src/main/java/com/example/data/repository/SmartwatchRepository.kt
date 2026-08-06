package com.example.data.repository

import com.example.data.local.AlarmEntity
import com.example.data.local.AppFilterEntity
import com.example.data.local.BadgeEntity
import com.example.data.local.DailyStatsEntity
import com.example.data.local.WatchDao
import com.example.data.model.AlarmModel
import com.example.data.model.BadgeModel
import com.example.data.model.DailyStats
import com.example.data.model.NotificationAppFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SmartwatchRepository(private val dao: WatchDao) {

    val allDailyStats: Flow<List<DailyStats>> = dao.getAllDailyStats().map { entities ->
        if (entities.isEmpty()) {
            listOf(
                DailyStats("2026-08-06", steps = 4250, calories = 185, activeMinutes = 38),
                DailyStats("2026-08-05", steps = 11200, calories = 480, activeMinutes = 72),
                DailyStats("2026-08-04", steps = 8900, calories = 390, activeMinutes = 55),
                DailyStats("2026-08-03", steps = 12400, calories = 520, activeMinutes = 80),
                DailyStats("2026-08-02", steps = 6700, calories = 290, activeMinutes = 42),
                DailyStats("2026-08-01", steps = 10100, calories = 440, activeMinutes = 65),
                DailyStats("2026-07-31", steps = 9300, calories = 410, activeMinutes = 58)
            )
        } else {
            entities.map {
                DailyStats(
                    date = it.date,
                    steps = it.steps,
                    stepGoal = it.stepGoal,
                    calories = it.calories,
                    calorieGoal = it.calorieGoal,
                    activeMinutes = it.activeMinutes,
                    activeGoalMinutes = it.activeGoalMinutes,
                    deepSleepMinutes = it.deepSleepMinutes,
                    lightSleepMinutes = it.lightSleepMinutes
                )
            }
        }
    }

    val allAlarms: Flow<List<AlarmModel>> = dao.getAllAlarms().map { entities ->
        entities.map {
            AlarmModel(
                id = it.id,
                hour = it.hour,
                minute = it.minute,
                label = it.label,
                isEnabled = it.isEnabled,
                melodyName = it.melodyName,
                repeatDays = it.repeatDaysCsv.split(",").filter { s -> s.isNotBlank() }
            )
        }
    }

    val allBadges: Flow<List<BadgeModel>> = dao.getAllBadges().map { entities ->
        entities.map {
            BadgeModel(
                id = it.id,
                title = it.title,
                description = it.description,
                requiredSteps = it.requiredSteps,
                iconName = it.iconName,
                isUnlocked = it.isUnlocked,
                unlockedAt = it.unlockedAt
            )
        }
    }

    val allAppFilters: Flow<List<NotificationAppFilter>> = dao.getAllAppFilters().map { entities ->
        entities.map {
            NotificationAppFilter(
                packageName = it.packageName,
                appName = it.appName,
                iconCategory = it.iconCategory,
                isMuted = it.isMuted
            )
        }
    }

    suspend fun seedDefaultsIfEmpty() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (dao.getStatsForDate(today) == null) {
            dao.insertDailyStats(
                DailyStatsEntity(
                    date = today,
                    steps = 4250,
                    calories = 185,
                    activeMinutes = 38
                )
            )
        }

        val defaultBadges = listOf(
            BadgeEntity("badge_10k", "Cyber Nomad", "Reach 10,000 steps in a single day", 10000, "stars", true, System.currentTimeMillis()),
            BadgeEntity("badge_50k", "Neo Runner", "Accumulate 50,000 total steps", 50000, "directions_run", true, System.currentTimeMillis()),
            BadgeEntity("badge_100k", "Quantum Sprinter", "Master 100,000 steps milestone", 100000, "bolt", false, 0L),
            BadgeEntity("badge_night_owl", "Night Hacker", "Track 8+ hours of deep sleep", 0, "bedtime", true, System.currentTimeMillis()),
            BadgeEntity("badge_alarm_master", "8-Bit Maestro", "Compose custom buzzer melody", 0, "music_note", false, 0L)
        )
        dao.insertBadges(defaultBadges)

        val defaultFilters = listOf(
            AppFilterEntity("com.google.android.apps.messaging", "SMS & Messages", "chat", false),
            AppFilterEntity("com.google.android.dialer", "Phone Calls", "call", false),
            AppFilterEntity("com.whatsapp", "WhatsApp", "chat", false),
            AppFilterEntity("com.instagram.android", "Instagram", "camera", true),
            AppFilterEntity("com.twitter.android", "X / Twitter", "public", true),
            AppFilterEntity("com.google.android.gm", "Gmail", "email", false)
        )
        dao.insertAppFilters(defaultFilters)

        val defaultAlarms = listOf(
            AlarmEntity(1, 7, 30, "Cyber Wake Up", true, "8-Bit Chiptune", "Mon,Tue,Wed,Thu,Fri"),
            AlarmEntity(2, 22, 0, "Rest & Recharge", false, "Retro Pulse", "Daily")
        )
        defaultAlarms.forEach { dao.insertAlarm(it) }
    }

    suspend fun addAlarm(alarm: AlarmModel) {
        dao.insertAlarm(
            AlarmEntity(
                id = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                isEnabled = alarm.isEnabled,
                melodyName = alarm.melodyName,
                repeatDaysCsv = alarm.repeatDays.joinToString(",")
            )
        )
    }

    suspend fun deleteAlarm(id: Int) {
        dao.deleteAlarmById(id)
    }

    suspend fun updateAppFilterMute(packageName: String, isMuted: Boolean) {
        dao.updateAppFilterMute(packageName, isMuted)
    }

    suspend fun checkAndUnlockBadges(currentSteps: Int) {
        if (currentSteps >= 10000) {
            dao.unlockBadge("badge_10k", System.currentTimeMillis())
        }
        if (currentSteps >= 50000) {
            dao.unlockBadge("badge_50k", System.currentTimeMillis())
        }
        if (currentSteps >= 100000) {
            dao.unlockBadge("badge_100k", System.currentTimeMillis())
        }
    }
}
