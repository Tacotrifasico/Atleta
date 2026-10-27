package com.example.athletic.domain.engine

import com.example.athletic.domain.model.InterferenceLevel
import com.example.athletic.domain.model.MuscleLoads

data class InterferenceAnalysis(
    val level: InterferenceLevel,
    val reasons: List<String>,
    val recommendation: String
)

object InterferenceEngine {

    fun calculateInterference(
        loadA: MuscleLoads,
        loadB: MuscleLoads,
        daysDifference: Int = 1,
        activityAName: String = "Actividad previa",
        activityBName: String = "Siguiente sesión"
    ): InterferenceAnalysis {
        val reasons = mutableListOf<String>()

        // 1. Leg overlap (Highest risk factor after cycling, running, high steps)
        val legOverlap = loadA.legs >= 4 && loadB.legs >= 4
        val legSevere = loadA.legs == 5 && loadB.legs >= 4

        // 2. Shoulder / Back overlap (e.g., swimming intense + overhead press)
        val shoulderOverlap = loadA.shoulders >= 4 && loadB.shoulders >= 4
        val backOverlap = loadA.back >= 4 && loadB.back >= 4

        // 3. Impact overlap (running intervals + HYROX sled/runs)
        val impactOverlap = (loadA.impact >= 3 && loadB.impact >= 3) || (loadA.cardio >= 4 && loadB.cardio >= 4)

        // 4. Grip / Arms overlap
        val gripOverlap = loadA.grip >= 4 && loadB.grip >= 3

        // 5. Global fatigue
        val highFatigueBoth = loadA.globalFatigue >= 4 && loadB.globalFatigue >= 4

        val temporalMultiplier = when (daysDifference) {
            0 -> 1.5f
            1 -> 1.0f
            2 -> 0.6f
            else -> 0.3f
        }

        var conflictScore = 0

        if (legSevere) {
            conflictScore += 5
            reasons.add("Tren inferior con fatiga máxima ($activityAName) interfiere con demanda de piernas ($activityBName).")
        } else if (legOverlap) {
            conflictScore += 3
            reasons.add("Superposición significativa en musculatura de piernas.")
        }

        if (impactOverlap) {
            conflictScore += 3
            reasons.add("Alto estrés cardiovascular y de impacto articular concurrente.")
        }

        if (shoulderOverlap) {
            conflictScore += 3
            reasons.add("Sobrecarga acumulada en articulación del hombro y deltoides.")
        }

        if (backOverlap) {
            conflictScore += 2
            reasons.add("Fatiga muscular en dorsal/cadena posterior.")
        }

        if (gripOverlap) {
            conflictScore += 2
            reasons.add("Fatiga acumulada en antebrazo y agarre.")
        }

        if (highFatigueBoth) {
            conflictScore += 2
            reasons.add("Demanda sistémica global elevada consecutiva.")
        }

        // Adjust for temporal proximity
        val adjustedScore = (conflictScore * temporalMultiplier).toInt()

        val level = when {
            adjustedScore >= 4 -> InterferenceLevel.VERY_HIGH
            adjustedScore == 3 -> InterferenceLevel.HIGH
            adjustedScore == 2 -> InterferenceLevel.MODERATE
            else -> InterferenceLevel.LOW
        }

        val recommendation = when (level) {
            InterferenceLevel.VERY_HIGH -> "Reorganizar: separar 48h, adelantar sesión de torso o insertar día regenerativo."
            InterferenceLevel.HIGH -> "Revisar sesión: reducir volumen a variante REDUCED o intercambiar por sesión de diferente grupo muscular."
            InterferenceLevel.MODERATE -> "Compatible con atención: verificar recuperación subjetiva y omitir accesorios duplicados."
            InterferenceLevel.LOW -> "Totalmente compatible: sin interferencia biomecánica ni energética relevante."
        }

        return InterferenceAnalysis(
            level = level,
            reasons = if (reasons.isEmpty()) listOf("No se detectan interferencias musculares ni sistémicas significativas.") else reasons,
            recommendation = recommendation
        )
    }
}
