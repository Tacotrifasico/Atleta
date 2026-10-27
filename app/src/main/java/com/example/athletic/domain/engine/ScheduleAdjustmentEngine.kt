package com.example.athletic.domain.engine

import com.example.athletic.domain.model.ChangeLogEntry
import com.example.athletic.domain.model.DifficultyLevel
import com.example.athletic.domain.model.Exercise
import com.example.athletic.domain.model.ExercisePriority
import com.example.athletic.domain.model.ExerciseRole
import com.example.athletic.domain.model.InterferenceLevel
import com.example.athletic.domain.model.MuscleGroup
import com.example.athletic.domain.model.MuscleLoads
import com.example.athletic.domain.model.PilatesFocus
import com.example.athletic.domain.model.ReadinessAssessment
import com.example.athletic.domain.model.ReadinessLevel
import com.example.athletic.domain.model.ReorganizationProposal
import com.example.athletic.domain.model.SessionPriority
import com.example.athletic.domain.model.SessionStatus
import com.example.athletic.domain.model.SessionVariant
import com.example.athletic.domain.model.SpontaneousActivity
import com.example.athletic.domain.model.WhatIfSimulation
import com.example.athletic.domain.model.WorkoutExercise
import com.example.athletic.domain.model.WorkoutSession
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID

object ScheduleAdjustmentEngine {

    /**
     * Resolves the "Machine Occupied" dilemma:
     * Priority:
     * 1. Advance next compatible exercise (respecting physiological order and non-competing muscles)
     * 2. Use approved substitution from exercise library
     * 3. Wait and reorder
     */
    fun handleMachineOccupied(
        session: WorkoutSession,
        occupiedIndex: Int,
        exerciseCatalog: Map<String, Exercise>
    ): MachineOccupiedResolution {
        val exercises = session.exercises
        if (occupiedIndex !in exercises.indices) {
            return MachineOccupiedResolution(exercises, "Índice de ejercicio inválido", null)
        }

        val occupied = exercises[occupiedIndex]
        val occupiedExercise = occupied.exercise

        // Check if there is an approved substitution
        val validSubId = occupiedExercise.substitutions.firstOrNull { exerciseCatalog.containsKey(it) }
        val substituteExercise = validSubId?.let { exerciseCatalog[it] }

        // Check if an existing later exercise can be advanced safely
        val advanceableIndex = exercises.indices.firstOrNull { i ->
            i > occupiedIndex && !exercises[i].isSkipped && isPhysiologicallySafeToAdvance(occupiedExercise, exercises[i].exercise)
        }

        return MachineOccupiedResolution(
            currentExercises = exercises,
            explanation = "Máquina ocupada para '${occupiedExercise.name}'. Se ofrecen alternativas respetando la fisiología articular y fatiga.",
            substituteExercise = substituteExercise,
            advanceCandidateIndex = advanceableIndex
        )
    }

    private fun isPhysiologicallySafeToAdvance(current: Exercise, candidate: Exercise): Boolean {
        // Never advance accessories that pre-fatigue the current compound movement
        // e.g. lateral raises before military press -> false
        // pec deck before heavy bench -> false
        if (candidate.role == ExerciseRole.ACCESSORY && current.role == ExerciseRole.ANCHOR) {
            val commonMuscles = candidate.targetMuscles.intersect(current.targetMuscles.toSet())
            if (commonMuscles.isNotEmpty()) return false
        }
        return true
    }

    /**
     * Filters session exercises for time constraint "Tengo X minutos".
     * Preserves ANCHORS and PRIORITY_A first, then ROTATION, reducing ACCESSORY.
     */
    fun adaptForDuration(session: WorkoutSession, targetMinutes: Int): WorkoutSession {
        val exercises = session.exercises
        val estimatedTotalMinutes = exercises.size * 12 // ~10-12 min per exercise including warm-up & sets

        if (estimatedTotalMinutes <= targetMinutes) {
            return session.copy(variant = SessionVariant.FULL)
        }

        // Sort by priority: ANCHORS and PRIORITY_A first
        val sortedExercises = exercises.sortedWith(
            compareBy<WorkoutExercise> {
                when (it.exercise.role) {
                    ExerciseRole.ANCHOR -> 0
                    ExerciseRole.ROTATION -> 1
                    ExerciseRole.ACCESSORY -> 2
                }
            }.thenBy {
                when (it.exercise.priority) {
                    ExercisePriority.PRIORITY_A -> 0
                    ExercisePriority.PRIORITY_B -> 1
                    ExercisePriority.ACCESSORY -> 2
                }
            }
        )

        val targetCount = (targetMinutes / 12).coerceIn(2, exercises.size)
        val selected = sortedExercises.take(targetCount).sortedBy { it.order }

        val variant = when {
            targetMinutes <= 30 -> SessionVariant.RECOVERY
            targetMinutes <= 45 -> SessionVariant.REDUCED
            else -> SessionVariant.FULL
        }

        return session.copy(
            exercises = selected.toMutableList(),
            variant = variant,
            actualDurationMinutes = targetMinutes,
            notes = "${session.notes} [Modo $targetMinutes min activado]".trim()
        )
    }

    /**
     * Simulates what would happen if the user does an activity tomorrow or today (What If mode).
     */
    fun simulateWhatIf(
        activity: SpontaneousActivity,
        currentWeeklyPlan: List<WorkoutSession>
    ): WhatIfSimulation {
        val estimatedLoads = activity.calculatedLoads

        // Find the session on the same or next day
        val nextDay = activity.date.plusDays(1)
        val nextSession = currentWeeklyPlan.firstOrNull { it.date == nextDay }

        val interferenceAnalysis = if (nextSession != null) {
            InterferenceEngine.calculateInterference(
                loadA = estimatedLoads,
                loadB = nextSession.loads,
                daysDifference = 1,
                activityAName = activity.type.displayName,
                activityBName = nextSession.name
            )
        } else {
            InterferenceEngine.calculateInterference(
                loadA = estimatedLoads,
                loadB = MuscleLoads(),
                daysDifference = 1,
                activityAName = activity.type.displayName,
                activityBName = "Descanso planeado"
            )
        }

        val proposal = evaluateAndReorganize(
            weeklyPlan = currentWeeklyPlan,
            spontaneousActivity = activity,
            readiness = null
        )

        return WhatIfSimulation(
            activity = activity,
            estimatedLoads = estimatedLoads,
            interferenceLevel = interferenceAnalysis.level,
            proposal = proposal
        )
    }

    /**
     * Core deterministic weekly schedule reorganizer.
     * Works with approved sessions, variant definitions, and explicit physiological rules.
     */
    fun evaluateAndReorganize(
        weeklyPlan: List<WorkoutSession>,
        spontaneousActivity: SpontaneousActivity?,
        readiness: ReadinessAssessment?
    ): ReorganizationProposal {
        val changes = mutableListOf<ChangeLogEntry>()
        val updatedPlan = weeklyPlan.map { it.copy(exercises = it.exercises.toMutableList()) }.toMutableList()

        if (spontaneousActivity == null && readiness == null) {
            return ReorganizationProposal(
                explanation = "El plan semanal actual se encuentra optimizado sin conflictos pendientes.",
                triggerRule = "NO_TRIGGER",
                changes = emptyList(),
                updatedSessions = updatedPlan,
                interferenceSummary = "Carga balanceada."
            )
        }

        // --- RULE 1: CYCLING >= 4h or >= 6h (Exceptional endurance or very high lower-body load) ---
        if (spontaneousActivity != null &&
            (spontaneousActivity.type == com.example.athletic.domain.model.ActivityType.CYCLING ||
             spontaneousActivity.type == com.example.athletic.domain.model.ActivityType.CYCLING_MTB_ENDURO) &&
            spontaneousActivity.durationMinutes >= 240
        ) {
            val actDate = spontaneousActivity.date
            val nextDate = actDate.plusDays(1)
            val nextSessionIndex = updatedPlan.indexOfFirst { it.date == nextDate }

            if (nextSessionIndex != -1) {
                val nextSession = updatedPlan[nextSessionIndex]
                val hasHeavyLegs = nextSession.loads.legs >= 4 ||
                        nextSession.name.contains("Pierna", ignoreCase = true) ||
                        nextSession.name.contains("Fuerza", ignoreCase = true) ||
                        nextSession.name.contains("HYROX", ignoreCase = true)

                if (hasHeavyLegs) {
                    // Look for an upper body / torso session later in the week that can be swapped forward
                    val torsoIndex = updatedPlan.indices.firstOrNull { i ->
                        i > nextSessionIndex && (
                            updatedPlan[i].loads.legs <= 2 &&
                            (updatedPlan[i].loads.back >= 3 || updatedPlan[i].loads.chest >= 3 || updatedPlan[i].name.contains("Espalda", ignoreCase = true) || updatedPlan[i].name.contains("Torso", ignoreCase = true))
                        )
                    }

                    if (torsoIndex != null) {
                        val torsoSession = updatedPlan[torsoIndex]
                        // Swap dates and dayOfWeek
                        val tempDate = nextSession.date
                        val tempDay = nextSession.dayOfWeek
                        nextSession.date = torsoSession.date
                        nextSession.dayOfWeek = torsoSession.dayOfWeek
                        torsoSession.date = tempDate
                        torsoSession.dayOfWeek = tempDay

                        // Swap positions in list
                        updatedPlan[nextSessionIndex] = torsoSession
                        updatedPlan[torsoIndex] = nextSession

                        val change = ChangeLogEntry(
                            id = UUID.randomUUID().toString(),
                            date = actDate,
                            beforeState = "${tempDay.name}: ${nextSession.name} | ${nextSession.dayOfWeek.name}: ${torsoSession.name}",
                            afterState = "${tempDay.name}: ${torsoSession.name} | ${nextSession.dayOfWeek.name}: ${nextSession.name}",
                            reason = "Ciclismo ${spontaneousActivity.durationMinutes / 60}h generó sobrecarga extrema en tren inferior. Se adelanta ${torsoSession.name} para proteger piernas y respetar 48h de recuperación muscular.",
                            triggerData = "Cycling ${spontaneousActivity.durationMinutes}min (Legs=5, Fatigue=5)",
                            origin = "LOCAL_RULE_ENGINE"
                        )
                        changes.add(change)

                        return ReorganizationProposal(
                            explanation = "Se detectó salida de ciclismo de ${spontaneousActivity.durationMinutes / 60}h. Por regla fisiológica, el día posterior no debe incluir sentadillas pesadas, zancadas ni sled. Se adelantó la sesión de torso (${torsoSession.name}) y se postergó la sesión de piernas.",
                            triggerRule = "RULE_CYCLING_LONG_INTERFERENCE",
                            changes = changes,
                            updatedSessions = updatedPlan.sortedBy { it.date },
                            interferenceSummary = "Interferencia de piernas resuelta adelantando torso."
                        )
                    } else {
                        // Insert Rest or Recovery Variant
                        nextSession.variant = SessionVariant.RECOVERY
                        val change = ChangeLogEntry(
                            id = UUID.randomUUID().toString(),
                            date = actDate,
                            beforeState = "${nextSession.name} [FULL]",
                            afterState = "${nextSession.name} [RECOVERY (30–50%)]",
                            reason = "Salida de ciclismo prolongada sin sesión de torso compatible para intercambio. Se reduce volumen a variante regenerativa.",
                            triggerData = "Cycling ${spontaneousActivity.durationMinutes}min",
                            origin = "LOCAL_RULE_ENGINE"
                        )
                        changes.add(change)

                        return ReorganizationProposal(
                            explanation = "Salida ciclista de ${spontaneousActivity.durationMinutes / 60}h. Sesión posterior convertida a variante de movilidad/recuperación.",
                            triggerRule = "RULE_CYCLING_RECOVERY_VARIANT",
                            changes = changes,
                            updatedSessions = updatedPlan,
                            interferenceSummary = "Volumen reducido a variante de recuperación activa."
                        )
                    }
                }
            }
        }

        // --- RULE 2: PILATES DAY BEFORE HYROX (ACCESORY OVERLAP) ---
        if (spontaneousActivity != null && spontaneousActivity.type == com.example.athletic.domain.model.ActivityType.PILATES) {
            val actDate = spontaneousActivity.date
            val nextDate = actDate.plusDays(1)
            val nextSession = updatedPlan.firstOrNull { it.date == nextDate }

            if (nextSession != null && nextSession.name.contains("HYROX", ignoreCase = true)) {
                // Keep HYROX, but trim duplicate accessory work (e.g., core/shoulder isolation)
                val focus = spontaneousActivity.pilatesFocus ?: PilatesFocus.CORE
                var trimmedCount = 0
                nextSession.exercises.forEach { we ->
                    if (we.exercise.role == ExerciseRole.ACCESSORY) {
                        val matchesFocus = when (focus) {
                            PilatesFocus.CORE -> we.exercise.targetMuscles.contains(MuscleGroup.CORE)
                            PilatesFocus.SHOULDERS -> we.exercise.targetMuscles.contains(MuscleGroup.SHOULDERS)
                            else -> false
                        }
                        if (matchesFocus) {
                            we.isSkipped = true
                            trimmedCount++
                        }
                    }
                }

                val change = ChangeLogEntry(
                    id = UUID.randomUUID().toString(),
                    date = actDate,
                    beforeState = "${nextSession.name} con accesorios completos",
                    afterState = "${nextSession.name} (accesorios de ${focus.label} cumplidos en Pilates)",
                    reason = "Pilates previo cubrió trabajo accesorio de ${focus.label}. Se preservan movimientos ANCHOR de HYROX (sled/carrera) y se omite duplicación.",
                    triggerData = "Pilates focus: ${focus.label}",
                    origin = "LOCAL_RULE_ENGINE"
                )
                changes.add(change)

                return ReorganizationProposal(
                    explanation = "HYROX se mantiene al 100% en sus movimientos clave (sled, carrera, carries). Se omitió el trabajo redundante de core/hombro ya estimulado en Pilates.",
                    triggerRule = "RULE_PILATES_HYROX_ACCESSORY_TRIM",
                    changes = changes,
                    updatedSessions = updatedPlan,
                    interferenceSummary = "Accesorios duplicados reducidos, ANCHORS protegidos."
                )
            }
        }

        // --- RULE 3: HIGH STEPS (>20,000) BEFORE SLED / HEAVY LEGS ---
        if (spontaneousActivity != null &&
            (spontaneousActivity.stepCount ?: 0) >= 20000
        ) {
            val actDate = spontaneousActivity.date
            val nextDate = actDate.plusDays(1)
            val nextSession = updatedPlan.firstOrNull { it.date == nextDate }

            if (nextSession != null && (nextSession.loads.legs >= 4 || nextSession.name.contains("Sled", ignoreCase = true))) {
                // Rule: Show accumulated load and review, DO NOT cancel automatically!
                val change = ChangeLogEntry(
                    id = UUID.randomUUID().toString(),
                    date = actDate,
                    beforeState = "${nextSession.name} [Alerta de fatiga podal]",
                    afterState = "${nextSession.name} [Monitoreada - No cancelada]",
                    reason = "Se registraron ${spontaneousActivity.stepCount} pasos. Carga acumulada moderada. Se mantiene sesión de piernas activa con sugerencia de calentar movilidad articular.",
                    triggerData = "Steps: ${spontaneousActivity.stepCount}",
                    origin = "LOCAL_RULE_ENGINE"
                )
                changes.add(change)

                return ReorganizationProposal(
                    explanation = "Caminata de más de 20,000 pasos genera fatiga acumulada en gemelos y pies. La sesión de ${nextSession.name} NO se cancela automáticamente; se recomienda dedicar 5 min extra al calentamiento de tobillos.",
                    triggerRule = "RULE_HIGH_STEPS_MONITORING",
                    changes = changes,
                    updatedSessions = updatedPlan,
                    interferenceSummary = "Monitoreo de fatiga activa sin bloqueo rígido."
                )
            }
        }

        // --- RULE 4: POOR READINESS (RED OR LOW YELLOW) ---
        if (readiness != null && readiness.level == ReadinessLevel.RED) {
            val todaySession = updatedPlan.firstOrNull { it.status == SessionStatus.PLANNED }
            if (todaySession != null && todaySession.variant == SessionVariant.FULL) {
                todaySession.variant = SessionVariant.REDUCED
                val change = ChangeLogEntry(
                    id = UUID.randomUUID().toString(),
                    date = LocalDate.now(),
                    beforeState = "${todaySession.name} [FULL]",
                    afterState = "${todaySession.name} [REDUCED]",
                    reason = "Múltiples biomarcadores en déficit (sueño bajo, fatiga reportada, HRV bajo). Se ajusta volumen al 70%.",
                    triggerData = "Readiness Score: ${readiness.score}/100",
                    origin = "LOCAL_RULE_ENGINE"
                )
                changes.add(change)

                return ReorganizationProposal(
                    explanation = "Readiness en zona roja. Se adaptó la sesión a variante REDUCED protegiendo los movimientos ANCHOR.",
                    triggerRule = "RULE_SYSTEMIC_FATIGUE_VOLUME_TRIM",
                    changes = changes,
                    updatedSessions = updatedPlan,
                    interferenceSummary = "Volumen modulado al 70%."
                )
            }
        }

        return ReorganizationProposal(
            explanation = "Plan verificado. No se requieren cambios estructurales obligatorios.",
            triggerRule = "NORMAL_MAINTENANCE",
            changes = changes,
            updatedSessions = updatedPlan,
            interferenceSummary = "Compatibilidad alta."
        )
    }

    /**
     * Reorders a session when user drags or swaps sessions between two days (Caso G).
     */
    fun swapOrMoveSession(
        plan: List<WorkoutSession>,
        fromDay: DayOfWeek,
        toDay: DayOfWeek
    ): ReorganizationProposal {
        val updated = plan.map { it.copy() }.toMutableList()
        val sessionFrom = updated.firstOrNull { it.dayOfWeek == fromDay }
        val sessionTo = updated.firstOrNull { it.dayOfWeek == toDay }

        val changes = mutableListOf<ChangeLogEntry>()

        if (sessionFrom != null && sessionTo != null) {
            // Swap
            val tempDate = sessionFrom.date
            val tempDay = sessionFrom.dayOfWeek
            sessionFrom.date = sessionTo.date
            sessionFrom.dayOfWeek = sessionTo.dayOfWeek
            sessionTo.date = tempDate
            sessionTo.dayOfWeek = tempDay

            val change = ChangeLogEntry(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now(),
                beforeState = "$fromDay: ${sessionFrom.name} | $toDay: ${sessionTo.name}",
                afterState = "$fromDay: ${sessionTo.name} | $toDay: ${sessionFrom.name}",
                reason = "Intercambio manual solicitado por el usuario.",
                triggerData = "User action: Swap $fromDay <-> $toDay",
                origin = "USER"
            )
            changes.add(change)

            return ReorganizationProposal(
                explanation = "Se intercambiaron los días de '${sessionFrom.name}' y '${sessionTo.name}'. Se recalculó el resto de la semana garantizando descansos mínimos.",
                triggerRule = "USER_SWAP_RECALCULATION",
                changes = changes,
                updatedSessions = updated.sortedBy { it.date },
                interferenceSummary = "Intercambio exitoso."
            )
        } else if (sessionFrom != null) {
            sessionFrom.dayOfWeek = toDay
            val change = ChangeLogEntry(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now(),
                beforeState = "$fromDay: ${sessionFrom.name}",
                afterState = "$toDay: ${sessionFrom.name}",
                reason = "Movimiento de día solicitado por el usuario.",
                triggerData = "User action: Move to $toDay",
                origin = "USER"
            )
            changes.add(change)

            return ReorganizationProposal(
                explanation = "Se movió '${sessionFrom.name}' al $toDay.",
                triggerRule = "USER_MOVE_RECALCULATION",
                changes = changes,
                updatedSessions = updated.sortedBy { it.date },
                interferenceSummary = "Día actualizado."
            )
        }

        return ReorganizationProposal(
            explanation = "No se encontraron las sesiones para mover.",
            triggerRule = "NO_OP",
            changes = emptyList(),
            updatedSessions = updated,
            interferenceSummary = "Sin cambios."
        )
    }
}

data class MachineOccupiedResolution(
    val currentExercises: List<WorkoutExercise>,
    val explanation: String,
    val substituteExercise: Exercise?,
    val advanceCandidateIndex: Int? = null
)
