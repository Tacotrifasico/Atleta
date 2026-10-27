package com.example.athletic

import com.example.athletic.domain.engine.ActivityLoadEngine
import com.example.athletic.domain.engine.InterferenceEngine
import com.example.athletic.domain.engine.RecoverySignalEngine
import com.example.athletic.domain.engine.ScheduleAdjustmentEngine
import com.example.athletic.domain.model.ActivityIntensity
import com.example.athletic.domain.model.ActivityType
import com.example.athletic.domain.model.DifficultyLevel
import com.example.athletic.domain.model.EquipmentType
import com.example.athletic.domain.model.Exercise
import com.example.athletic.domain.model.ExercisePriority
import com.example.athletic.domain.model.ExerciseRole
import com.example.athletic.domain.model.GarminDailyMetrics
import com.example.athletic.domain.model.InterferenceLevel
import com.example.athletic.domain.model.LoggedSet
import com.example.athletic.domain.model.MovementPattern
import com.example.athletic.domain.model.MuscleGroup
import com.example.athletic.domain.model.MuscleLoads
import com.example.athletic.domain.model.PilatesFocus
import com.example.athletic.domain.model.ReadinessLevel
import com.example.athletic.domain.model.SessionPriority
import com.example.athletic.domain.model.SessionStatus
import com.example.athletic.domain.model.SessionVariant
import com.example.athletic.domain.model.SleepQuality
import com.example.athletic.domain.model.SpontaneousActivity
import com.example.athletic.domain.model.SubjectiveCheckIn
import com.example.athletic.domain.model.WorkoutExercise
import com.example.athletic.domain.model.WorkoutSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class EngineScenariosTest {

    private val baseDate = LocalDate.of(2026, 10, 26) // Monday

    private fun createSampleExercise(
        id: String,
        name: String,
        muscles: List<MuscleGroup>,
        role: ExerciseRole,
        priority: ExercisePriority,
        substitutions: List<String> = emptyList()
    ): Exercise {
        return Exercise(
            id = id,
            name = name,
            targetMuscles = muscles,
            pattern = MovementPattern.PUSH,
            equipment = EquipmentType.BARBELL,
            role = role,
            priority = priority,
            substitutions = substitutions
        )
    }

    private fun createSampleSession(
        id: String,
        name: String,
        day: DayOfWeek,
        date: LocalDate,
        legsLoad: Int = 0,
        backLoad: Int = 0,
        chestLoad: Int = 0,
        exercises: List<WorkoutExercise> = emptyList()
    ): WorkoutSession {
        return WorkoutSession(
            sessionId = id,
            name = name,
            dayOfWeek = day,
            date = date,
            priority = SessionPriority.HIGH,
            isMovable = true,
            plannedDurationMinutes = 60,
            variant = SessionVariant.FULL,
            exercises = exercises.toMutableList(),
            loads = MuscleLoads(legs = legsLoad, back = backLoad, chest = chestLoad, globalFatigue = 3),
            status = SessionStatus.PLANNED
        )
    }

    // --- CASO A: Domingo 6h bici -> Lunes pierna pesada ---
    @Test
    fun testCasoA_Sunday6hBike_ReorganizesMondayLegs() {
        val sunday = baseDate.minusDays(1) // Sunday
        val monday = baseDate // Monday
        val tuesday = baseDate.plusDays(1) // Tuesday

        val bikeActivity = SpontaneousActivity(
            id = "act-sunday-bike",
            type = ActivityType.CYCLING,
            date = sunday,
            durationMinutes = 360, // 6 hours!
            intensity = ActivityIntensity.Z2_AEROBIC,
            calculatedLoads = ActivityLoadEngine.calculateLoads(ActivityType.CYCLING, 360)
        )

        val mondayLegs = createSampleSession("s-mon", "Pierna / Fuerza", DayOfWeek.MONDAY, monday, legsLoad = 5)
        val tuesdayUpper = createSampleSession("s-tue", "Espalda / Brazos", DayOfWeek.TUESDAY, tuesday, backLoad = 4, legsLoad = 0)
        val plan = listOf(mondayLegs, tuesdayUpper)

        val proposal = ScheduleAdjustmentEngine.evaluateAndReorganize(
            weeklyPlan = plan,
            spontaneousActivity = bikeActivity,
            readiness = null
        )

        // Must swap Tuesday torso forward to Monday and move legs to Tuesday!
        assertEquals("RULE_CYCLING_LONG_INTERFERENCE", proposal.triggerRule)
        assertTrue(proposal.changes.isNotEmpty())
        val firstSessionAfter = proposal.updatedSessions.first { it.date == monday }
        assertEquals("Espalda / Brazos", firstSessionAfter.name)
        val nextSessionAfter = proposal.updatedSessions.first { it.date == tuesday }
        assertEquals("Pierna / Fuerza", nextSessionAfter.name)
    }

    // --- CASO B: Miércoles Pilates hombro/core -> Jueves HYROX ---
    @Test
    fun testCasoB_PilatesBeforeHyrox_ProtectsAnchorsAndTrimsAccessories() {
        val wednesday = baseDate.plusDays(2)
        val thursday = baseDate.plusDays(3)

        val pilates = SpontaneousActivity(
            id = "act-pilates",
            type = ActivityType.PILATES,
            date = wednesday,
            durationMinutes = 55,
            pilatesFocus = PilatesFocus.CORE,
            calculatedLoads = ActivityLoadEngine.calculateLoads(ActivityType.PILATES, 55, pilatesFocus = PilatesFocus.CORE)
        )

        val sledExercise = createSampleExercise("ex-sled", "Sled Push & Pull", listOf(MuscleGroup.LEGS, MuscleGroup.CARDIO), ExerciseRole.ANCHOR, ExercisePriority.PRIORITY_A)
        val coreAccessory = createSampleExercise("ex-pallof", "Pallof Press", listOf(MuscleGroup.CORE), ExerciseRole.ACCESSORY, ExercisePriority.ACCESSORY)

        val hyroxSession = createSampleSession(
            "s-hyrox",
            "HYROX Potencia & Capacidad",
            DayOfWeek.THURSDAY,
            thursday,
            legsLoad = 4,
            exercises = listOf(
                WorkoutExercise(sledExercise, listOf(LoggedSet(1, 15, null, 100f))),
                WorkoutExercise(coreAccessory, listOf(LoggedSet(1, 12, null, 25f)))
            )
        )

        val proposal = ScheduleAdjustmentEngine.evaluateAndReorganize(
            weeklyPlan = listOf(hyroxSession),
            spontaneousActivity = pilates,
            readiness = null
        )

        assertEquals("RULE_PILATES_HYROX_ACCESSORY_TRIM", proposal.triggerRule)
        // HYROX is maintained
        val hyrox = proposal.updatedSessions.first()
        assertEquals("HYROX Potencia & Capacidad", hyrox.name)
        // Anchor sled is NOT skipped
        val sled = hyrox.exercises.first { it.exercise.id == "ex-sled" }
        assertFalse(sled.isSkipped)
        // Core accessory is trimmed because Pilates already worked core!
        val core = hyrox.exercises.first { it.exercise.id == "ex-pallof" }
        assertTrue(core.isSkipped)
    }

    // --- CASO C: Martes 20,000 pasos -> Miércoles Sled pesado ---
    @Test
    fun testCasoC_HighSteps_MonitorsWithoutAutoCanceling() {
        val tuesday = baseDate.plusDays(1)
        val wednesday = baseDate.plusDays(2)

        val highSteps = SpontaneousActivity(
            id = "act-steps",
            type = ActivityType.WALKING,
            date = tuesday,
            durationMinutes = 180,
            stepCount = 21500,
            calculatedLoads = ActivityLoadEngine.calculateLoads(ActivityType.WALKING, 180, stepCount = 21500)
        )

        val sledSession = createSampleSession("s-sled", "Sled Pesado + Fuerza Pierna", DayOfWeek.WEDNESDAY, wednesday, legsLoad = 5)

        val proposal = ScheduleAdjustmentEngine.evaluateAndReorganize(
            weeklyPlan = listOf(sledSession),
            spontaneousActivity = highSteps,
            readiness = null
        )

        // Must NOT cancel session
        assertEquals("RULE_HIGH_STEPS_MONITORING", proposal.triggerRule)
        val updated = proposal.updatedSessions.first()
        assertNotEquals(SessionStatus.REST, updated.status)
        assertTrue(proposal.changes.first().reason.contains("Se mantiene sesión de piernas activa"))
    }

    // --- CASO D: Garmin Recovery 60h, pero usuario bien (HRV normal, buen sueño) ---
    @Test
    fun testCasoD_Garmin60hRecovery_DoesNotBlockWhenUserFeelsGood() {
        val garmin = GarminDailyMetrics(
            date = baseDate,
            hrvCurrent = 58f,
            hrvBaseline28d = 60f, // -3.3% within normal range!
            rhrCurrent = 50,
            rhrBaseline28d = 50, // perfectly normal
            sleepDurationHours = 8.0f,
            sleepQuality = SleepQuality.WELL,
            recoveryHours = 60 // High Garmin estimate!
        )

        val checkIn = SubjectiveCheckIn(
            date = baseDate,
            energyScore = 4,
            legSoreness = 1,
            torsoSoreness = 1,
            musclePain = 1,
            stressScore = 1,
            motivationScore = 5,
            sleptWell = SleepQuality.WELL
        )

        val readiness = RecoverySignalEngine.calculateReadiness(
            checkIn = checkIn,
            garmin = garmin
        )

        // Must NOT be RED and must NOT block training!
        assertNotEquals(ReadinessLevel.RED, readiness.level)
        assertTrue(readiness.score >= 70)
        // Recovery hours is noted as secondary signal
        assertTrue(readiness.bulletReasons.any { it.contains("60h") && it.contains("referencia secundaria") })
    }

    // --- CASO E: HRV baja + RHR alta + sueño pobre + usuario cansado ---
    @Test
    fun testCasoE_LowHrvHighRhrPoorSleep_SuggestsReductionOrRecovery() {
        val garmin = GarminDailyMetrics(
            date = baseDate,
            hrvCurrent = 42f,
            hrvBaseline28d = 60f, // -30% drop!
            rhrCurrent = 58,
            rhrBaseline28d = 50, // +8 bpm elevated!
            sleepDurationHours = 4.5f,
            sleepQuality = SleepQuality.POOR,
            recoveryHours = 48
        )

        val checkIn = SubjectiveCheckIn(
            date = baseDate,
            energyScore = 1,
            legSoreness = 4,
            torsoSoreness = 3,
            musclePain = 4,
            stressScore = 5,
            motivationScore = 2,
            sleptWell = SleepQuality.POOR
        )

        val readiness = RecoverySignalEngine.calculateReadiness(
            checkIn = checkIn,
            garmin = garmin
        )

        // Must indicate RED or alert level with recommendation to modulate volume
        assertEquals(ReadinessLevel.RED, readiness.level)
        assertTrue(readiness.score < 45)

        val plannedSession = createSampleSession("s-mon", "Fuerza Pesada", DayOfWeek.MONDAY, baseDate, legsLoad = 4)
        val proposal = ScheduleAdjustmentEngine.evaluateAndReorganize(
            weeklyPlan = listOf(plannedSession),
            spontaneousActivity = null,
            readiness = readiness
        )

        assertEquals("RULE_SYSTEMIC_FATIGUE_VOLUME_TRIM", proposal.triggerRule)
        assertEquals(SessionVariant.REDUCED, proposal.updatedSessions.first().variant)
    }

    // --- CASO F: Máquina ocupada -> Reordenar / sustitución aprobada ---
    @Test
    fun testCasoF_MachineOccupied_ProvidesSafeAdvanceOrApprovedSubstitution() {
        val benchPress = createSampleExercise(
            "ex-bench",
            "Press Militar / Banca",
            listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS),
            ExerciseRole.ANCHOR,
            ExercisePriority.PRIORITY_A,
            substitutions = listOf("ex-db-bench")
        )
        val dumbbellBench = createSampleExercise(
            "ex-db-bench",
            "Press con Mancuernas en Banco",
            listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS),
            ExerciseRole.ROTATION,
            ExercisePriority.PRIORITY_B
        )
        val pullups = createSampleExercise(
            "ex-pullup",
            "Dominadas Pronas",
            listOf(MuscleGroup.BACK, MuscleGroup.ARMS),
            ExerciseRole.ANCHOR,
            ExercisePriority.PRIORITY_A
        )

        val session = WorkoutSession(
            sessionId = "s-fullbody",
            name = "Torso Potencia",
            dayOfWeek = DayOfWeek.MONDAY,
            date = baseDate,
            exercises = mutableListOf(
                WorkoutExercise(benchPress, listOf(LoggedSet(1, 8, null, 150f))),
                WorkoutExercise(pullups, listOf(LoggedSet(1, 8, null, 0f)))
            ),
            loads = MuscleLoads(chest = 4, back = 4)
        )

        val catalog = mapOf(
            "ex-bench" to benchPress,
            "ex-db-bench" to dumbbellBench,
            "ex-pullup" to pullups
        )

        val resolution = ScheduleAdjustmentEngine.handleMachineOccupied(
            session = session,
            occupiedIndex = 0, // Bench is occupied
            exerciseCatalog = catalog
        )

        // Approved substitution is offered
        assertNotNull(resolution.substituteExercise)
        assertEquals("Press con Mancuernas en Banco", resolution.substituteExercise?.name)
        // Pullups is safe to advance as it's non-competing (Back vs Chest)
        assertEquals(1, resolution.advanceCandidateIndex)
    }

    // --- CASO G: Usuario mueve HYROX de jueves a miércoles ---
    @Test
    fun testCasoG_UserMovesHyrox_RecalculatesPlan() {
        val wednesdaySession = createSampleSession("s-wed", "Descanso Activo", DayOfWeek.WEDNESDAY, baseDate.plusDays(2))
        val thursdayHyrox = createSampleSession("s-thu", "HYROX Sled & Run", DayOfWeek.THURSDAY, baseDate.plusDays(3))
        val plan = listOf(wednesdaySession, thursdayHyrox)

        val result = ScheduleAdjustmentEngine.swapOrMoveSession(
            plan = plan,
            fromDay = DayOfWeek.THURSDAY,
            toDay = DayOfWeek.WEDNESDAY
        )

        assertEquals("USER_SWAP_RECALCULATION", result.triggerRule)
        val wednesdayAfter = result.updatedSessions.first { it.dayOfWeek == DayOfWeek.WEDNESDAY }
        assertEquals("HYROX Sled & Run", wednesdayAfter.name)
        val thursdayAfter = result.updatedSessions.first { it.dayOfWeek == DayOfWeek.THURSDAY }
        assertEquals("Descanso Activo", thursdayAfter.name)
    }

    // --- VERIFICACIÓN DE CHECKLIST EN LIMPIO (Sin sets inventados) ---
    @Test
    fun testNotionChecklist_StartsCleanAndToggles() {
        val plan = com.example.athletic.data.MockDataProvider.getInitialWeeklyPlan()
        val allSets = plan.flatMap { it.exercises }.flatMap { it.sets }

        // Todos los sets deben iniciar en estado limpio (PENDING, sin reps ni peso pre-marcado)
        assertTrue(allSets.all { it.status == com.example.athletic.domain.model.SetStatus.PENDING })
        assertTrue(allSets.all { it.actualReps == null })
        assertTrue(allSets.all { it.actualWeight == null })
    }
}
