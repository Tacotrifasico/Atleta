package com.example.athletic.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class MuscleGroup(val displayName: String) {
    LEGS("Piernas"),
    CHEST("Pecho"),
    BACK("Espalda"),
    SHOULDERS("Hombros"),
    ARMS("Brazos"),
    CORE("Core"),
    CARDIO("Cardiovascular"),
    FULL_BODY("Cuerpo completo")
}

enum class MovementPattern {
    SQUAT,
    HINGE,
    PUSH,
    PULL,
    CARRY,
    ROTATION,
    LUNGE,
    CARDIO_INTERVALS,
    ISOLATION
}

enum class EquipmentType(val displayName: String) {
    BARBELL("Barra"),
    DUMBBELL("Mancuerna"),
    KETTLEBELL("Kettlebell"),
    CABLE("Polea"),
    MACHINE("Máquina"),
    BODYWEIGHT("Calistenia"),
    SLED("Sled"),
    CARDIO_MACHINE("Cardio (Rower/SkiErg/Bici)")
}

enum class GymLocation(val displayName: String) {
    MAIN_GYM("Gym Principal"),
    HYROX_GYM("Gym HYROX"),
    HOME("Casa (KB 35lb & Ligas)"),
    OTHER("Otro Gym")
}

enum class ExerciseRole {
    ANCHOR,     // Dominadas, press militar, jalón, sled, carries
    ROTATION,   // Búlgaras, step-up, landmine, high pull, snatch, wall balls
    ACCESSORY   // Laterales, curl, tríceps, Pallof, face pull
}

enum class ExercisePriority {
    PRIORITY_A, // Movimientos clave no negociables
    PRIORITY_B, // Ejercicios secundarios reubicables
    ACCESSORY   // Volumen auxiliar flexible
}

enum class WeightUnit {
    LB,
    KG,
    BOTH
}

enum class SetStatus {
    PENDING,
    COMPLETED,
    COMPLETED_UNLOGGED,
    SKIPPED,
    MODIFIED
}

enum class DifficultyLevel(val label: String) {
    EASY("Fácil"),
    ON_POINT("Correcto"),
    DEMANDING("Exigente"),
    VERY_DEMANDING("Muy exigente")
}

enum class SessionVariant(val multiplier: Float, val label: String) {
    FULL(1.0f, "FULL (100%)"),
    REDUCED(0.70f, "REDUCED (60–75%)"),
    RECOVERY(0.40f, "RECOVERY (30–50%)")
}

enum class SessionPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class SessionStatus {
    PLANNED,
    COMPLETED,
    MOVED,
    REST
}

enum class ActivityType(val displayName: String) {
    CYCLING("Ciclismo de ruta"),
    CYCLING_MTB_ENDURO("MTB / Enduro"),
    RUNNING("Carrera"),
    SWIMMING("Natación"),
    PILATES("Pilates"),
    WALKING("Caminata / Pasos"),
    HIKING("Senderismo"),
    KAYAK("Kayak"),
    SPORT("Deporte espontáneo"),
    CLASS("Clase guiada"),
    OTHER("Otra actividad")
}

enum class ActivityIntensity(val label: String) {
    Z1_RECOVERY("Z1 Regenerativo"),
    Z2_AEROBIC("Z2 Aeróbico Base"),
    Z3_TEMPO("Z3 Tempo"),
    Z4_THRESHOLD("Z4 Umbral"),
    Z5_MAX("Z5 Intervalos / Máximo")
}

enum class PilatesFocus(val label: String) {
    CORE("Core"),
    SHOULDERS("Hombros / Escápulas"),
    LEGS("Piernas / Glúteos"),
    FULL_BODY("Cuerpo completo"),
    MOBILITY("Movilidad")
}

enum class InterferenceLevel(val label: String, val score: Int) {
    LOW("Baja", 1),
    MODERATE("Moderada", 2),
    HIGH("Alta", 3),
    VERY_HIGH("Muy Alta", 4)
}

enum class ReadinessLevel(val label: String) {
    GREEN("Óptimo (Entrenamiento Normal)"),
    YELLOW("Atención (Permitido, Ajustar Carga)"),
    RED("Recuperación Recomendada")
}

enum class SleepQuality(val label: String) {
    WELL("Dormí bien"),
    AVERAGE("Regular"),
    POOR("Mal")
}

enum class RepeatPreference(val label: String) {
    YES("Sí, excelente"),
    WITH_CHANGES("Con cambios"),
    NO("No repetir")
}

data class MuscleLoads(
    val legs: Int = 0,         // 0 - 5
    val chest: Int = 0,        // 0 - 5
    val back: Int = 0,         // 0 - 5
    val shoulders: Int = 0,    // 0 - 5
    val arms: Int = 0,         // 0 - 5
    val core: Int = 0,         // 0 - 5
    val cardio: Int = 0,       // 0 - 5
    val impact: Int = 0,       // 0 - 5
    val grip: Int = 0,         // 0 - 5
    val globalFatigue: Int = 0 // 0 - 5
)

data class Exercise(
    val id: String,
    val name: String,
    val targetMuscles: List<MuscleGroup>,
    val pattern: MovementPattern,
    val equipment: EquipmentType,
    val gymLocation: GymLocation = GymLocation.MAIN_GYM,
    val isUnilateral: Boolean = false,
    val role: ExerciseRole = ExerciseRole.ROTATION,
    val priority: ExercisePriority = ExercisePriority.PRIORITY_B,
    val recommendedOrder: Int = 1,
    val availableWeights: List<Float> = listOf(15f, 20f, 25f, 30f, 35f, 40f, 45f, 50f),
    val isPlateLoaded: Boolean = false,
    val isPerHand: Boolean = false,
    val substitutions: List<String> = emptyList(),
    val notes: String = ""
)

data class LoggedSet(
    val setNumber: Int,
    val targetReps: Int,
    var actualReps: Int? = null,
    val targetWeight: Float,
    var actualWeight: Float? = null,
    val unit: WeightUnit = WeightUnit.LB,
    val isPerSide: Boolean = false,
    val isPerHand: Boolean = false,
    val isPlateLoaded: Boolean = false,
    var status: SetStatus = SetStatus.PENDING,
    var difficulty: DifficultyLevel? = null,
    var notes: String = ""
)

data class WorkoutExercise(
    val exercise: Exercise,
    val sets: List<LoggedSet>,
    val targetRestSeconds: Int = 90,
    var order: Int = 1,
    var isSkipped: Boolean = false,
    var notes: String = ""
)

data class SessionFeedback(
    val feltTrainedScore: Int = 4,   // 1-5
    val funScore: Int = 4,           // 1-5
    val energyRemainingPct: Int = 50,// 0-100%
    val repeatPreference: RepeatPreference = RepeatPreference.YES,
    val notes: String = ""
)

data class WorkoutSession(
    val sessionId: String,
    val name: String,
    var dayOfWeek: DayOfWeek,
    var date: LocalDate,
    val priority: SessionPriority = SessionPriority.MEDIUM,
    val isMovable: Boolean = true,
    val isAnchorSession: Boolean = false,
    val plannedDurationMinutes: Int = 60,
    var actualDurationMinutes: Int? = null,
    var variant: SessionVariant = SessionVariant.FULL,
    val exercises: MutableList<WorkoutExercise>,
    val loads: MuscleLoads,
    var status: SessionStatus = SessionStatus.PLANNED,
    var feedback: SessionFeedback? = null,
    var notes: String = ""
)

data class SpontaneousActivity(
    val id: String,
    val type: ActivityType,
    val date: LocalDate,
    val durationMinutes: Int,
    val distanceKm: Float? = null,
    val elevationGainM: Int? = null,
    val intensity: ActivityIntensity = ActivityIntensity.Z2_AEROBIC,
    val pilatesFocus: PilatesFocus? = null,
    val stepCount: Int? = null,
    val calculatedLoads: MuscleLoads,
    val notes: String = ""
)

data class GarminDailyMetrics(
    val date: LocalDate,
    val hrvCurrent: Float,
    val hrvBaseline28d: Float,
    val rhrCurrent: Int,
    val rhrBaseline28d: Int,
    val sleepDurationHours: Float,
    val sleepScore: Int = 80,
    val sleepQuality: SleepQuality = SleepQuality.WELL,
    val bodyBattery: Int = 75,
    val stressScore: Int = 25,
    val steps: Int = 8500,
    val recoveryHours: Int = 24 // Secondary signal only!
)

data class SubjectiveCheckIn(
    val date: LocalDate,
    val energyScore: Int = 4,      // 1-5
    val legSoreness: Int = 1,      // 1-5
    val torsoSoreness: Int = 1,    // 1-5
    val musclePain: Int = 1,       // 1-5
    val stressScore: Int = 2,      // 1-5
    val motivationScore: Int = 4,  // 1-5
    val sleptWell: SleepQuality = SleepQuality.WELL,
    val notes: String = ""
)

data class ReadinessAssessment(
    val level: ReadinessLevel,
    val score: Int,
    val bulletReasons: List<String>,
    val recommendation: String,
    val garminRecoveryHours: Int
)

data class ChangeLogEntry(
    val id: String,
    val date: LocalDate,
    val beforeState: String,
    val afterState: String,
    val reason: String,
    val triggerData: String,
    val origin: String = "LOCAL_RULE_ENGINE" // LOCAL_RULE_ENGINE, USER, CHATGPT
)

data class ReorganizationProposal(
    val explanation: String,
    val triggerRule: String,
    val changes: List<ChangeLogEntry>,
    val updatedSessions: List<WorkoutSession>,
    val interferenceSummary: String
)

data class WhatIfSimulation(
    val activity: SpontaneousActivity,
    val estimatedLoads: MuscleLoads,
    val interferenceLevel: InterferenceLevel,
    val proposal: ReorganizationProposal
)
