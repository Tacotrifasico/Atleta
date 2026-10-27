package com.example.athletic.domain.engine

import com.example.athletic.domain.model.GarminDailyMetrics
import com.example.athletic.domain.model.ReadinessAssessment
import com.example.athletic.domain.model.ReadinessLevel
import com.example.athletic.domain.model.SleepQuality
import com.example.athletic.domain.model.SubjectiveCheckIn

object RecoverySignalEngine {

    fun calculateReadiness(
        checkIn: SubjectiveCheckIn?,
        garmin: GarminDailyMetrics?,
        recentHighLoadYesterday: Boolean = false,
        yesterdayLegsHigh: Boolean = false
    ): ReadinessAssessment {
        val reasons = mutableListOf<String>()
        var penaltyPoints = 0

        // 1. Subjective muscle soreness & DOMS (Rank #5 in hierarchy)
        if (checkIn != null) {
            if (checkIn.legSoreness >= 4) {
                penaltyPoints += 25
                reasons.add("Dolor muscular en piernas reportado alto (${checkIn.legSoreness}/5).")
            } else if (checkIn.legSoreness == 3) {
                penaltyPoints += 10
                reasons.add("Piernas con fatiga moderada (${checkIn.legSoreness}/5).")
            }

            if (checkIn.torsoSoreness >= 4) {
                penaltyPoints += 20
                reasons.add("Dolor muscular en torso reportado alto (${checkIn.torsoSoreness}/5).")
            }

            // Subjective energy (Rank #6)
            if (checkIn.energyScore <= 2) {
                penaltyPoints += 25
                reasons.add("Energía subjetiva baja (${checkIn.energyScore}/5).")
            } else if (checkIn.energyScore >= 4) {
                penaltyPoints -= 10
                reasons.add("Energía subjetiva alta y positiva (${checkIn.energyScore}/5).")
            }

            // Sleep quality (Rank #7)
            if (checkIn.sleptWell == SleepQuality.POOR) {
                penaltyPoints += 25
                reasons.add("Sueño deficiente reportado.")
            } else if (checkIn.sleptWell == SleepQuality.WELL) {
                penaltyPoints -= 5
                reasons.add("Buen descanso nocturno.")
            }

            // Stress
            if (checkIn.stressScore >= 4) {
                penaltyPoints += 15
                reasons.add("Nivel de estrés subjetivo elevado (${checkIn.stressScore}/5).")
            }
        }

        // 2. Garmin Physiological Signals (Relative to personal baselines, NOT universal thresholds)
        if (garmin != null) {
            // HRV vs personal 28-day baseline (Rank #8)
            val hrvDiffPct = if (garmin.hrvBaseline28d > 0) {
                ((garmin.hrvCurrent - garmin.hrvBaseline28d) / garmin.hrvBaseline28d) * 100f
            } else 0f

            if (hrvDiffPct < -12f) {
                penaltyPoints += 20
                reasons.add("HRV %.1f%% por debajo de tu media personal de 28 días.".format(kotlin.math.abs(hrvDiffPct)))
            } else if (hrvDiffPct < -6f) {
                penaltyPoints += 10
                reasons.add("HRV ligeramente bajo (%.1f%% vs baseline 28d).".format(kotlin.math.abs(hrvDiffPct)))
            } else {
                reasons.add("HRV dentro del rango normal personal (%.1f ms).".format(garmin.hrvCurrent))
            }

            // Resting Heart Rate vs personal 28-day baseline (Rank #9)
            val rhrDiff = garmin.rhrCurrent - garmin.rhrBaseline28d
            if (rhrDiff >= 5) {
                penaltyPoints += 18
                reasons.add("Frecuencia cardíaca en reposo +$rhrDiff bpm por encima de tu baseline.")
            } else {
                reasons.add("Frecuencia cardíaca en reposo estable (${garmin.rhrCurrent} bpm).")
            }

            // Body battery & Stress (Rank #10-11)
            if (garmin.bodyBattery < 35) {
                penaltyPoints += 15
                reasons.add("Body Battery bajo (${garmin.bodyBattery}/100).")
            }
        }

        // 3. Activity Yesterday context
        if (recentHighLoadYesterday) {
            penaltyPoints += 15
            reasons.add("Carga física demandante registrada el día anterior.")
        }

        // 4. Garmin Recovery Time (Rank #12 - Secondary reference ONLY)
        val garminHours = garmin?.recoveryHours ?: 0
        if (garminHours > 36) {
            // Informative note, NOT a blocking rule!
            reasons.add("Garmin estima ${garminHours}h de recuperación (usado como referencia secundaria, no como regla rígida).")
        }

        // Calculate score (100 = perfect, 0 = exhausted)
        val score = (100 - penaltyPoints).coerceIn(10, 100)

        val (level, recommendation) = when {
            score >= 70 -> Pair(
                ReadinessLevel.GREEN,
                "Luz verde: sistema preparado para el entrenamiento programado en volumen completo."
            )
            score >= 45 -> Pair(
                ReadinessLevel.YELLOW,
                if (yesterdayLegsHigh) {
                    "Luz amarilla: entrenamiento permitido. Se aconseja adelantar sesión de torso y desplazar pierna/HYROX pesado."
                } else {
                    "Luz amarilla: entrenamiento permitido. Modula descansos o utiliza variante REDUCED si la fatiga persiste."
                }
            )
            else -> Pair(
                ReadinessLevel.RED,
                "Luz roja: fatiga sistémica acumulada. Priorizar recuperación activa, movilidad suave o descanso."
            )
        }

        return ReadinessAssessment(
            level = level,
            score = score,
            bulletReasons = reasons,
            recommendation = recommendation,
            garminRecoveryHours = garminHours
        )
    }
}
