package com.example.athletic.data

import com.example.athletic.domain.engine.ActivityLoadEngine
import com.example.athletic.domain.model.ActivityIntensity
import com.example.athletic.domain.model.ActivityType
import com.example.athletic.domain.model.DifficultyLevel
import com.example.athletic.domain.model.EquipmentType
import com.example.athletic.domain.model.Exercise
import com.example.athletic.domain.model.ExercisePriority
import com.example.athletic.domain.model.ExerciseRole
import com.example.athletic.domain.model.GarminDailyMetrics
import com.example.athletic.domain.model.GymLocation
import com.example.athletic.domain.model.LoggedSet
import com.example.athletic.domain.model.MovementPattern
import com.example.athletic.domain.model.MuscleGroup
import com.example.athletic.domain.model.MuscleLoads
import com.example.athletic.domain.model.SessionPriority
import com.example.athletic.domain.model.SessionStatus
import com.example.athletic.domain.model.SessionVariant
import com.example.athletic.domain.model.SetStatus
import com.example.athletic.domain.model.SleepQuality
import com.example.athletic.domain.model.SubjectiveCheckIn
import com.example.athletic.domain.model.WeightUnit
import com.example.athletic.domain.model.WorkoutExercise
import com.example.athletic.domain.model.WorkoutSession
import java.time.DayOfWeek
import java.time.LocalDate

data class PersonalRecord(
    val exerciseName: String,
    val prType: String,
    val value: String,
    val date: String,
    val note: String
)

data class AnnualMacroBlock(
    val blockNumber: Int,
    val name: String,
    val durationWeeks: Int,
    val focus: String,
    val isCurrent: Boolean = false
)

object MockDataProvider {

    // Realistic dumbbell increments (no impossible decimals like 27.5 lb!)
    val dumbbellWeights = listOf(10f, 15f, 20f, 25f, 30f, 35f, 40f, 45f, 50f, 55f, 60f)
    val barbellWeights = listOf(45f, 65f, 95f, 115f, 135f, 155f, 185f, 205f, 225f)
    val kettlebellWeights = listOf(18f, 26f, 35f, 44f, 53f, 70f)
    val cableWeights = listOf(15f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f)

    val exercises: List<Exercise> = listOf(
        // Anchors
        Exercise(
            id = "ex-pullups",
            name = "Dominadas Pronas Estrictas",
            targetMuscles = listOf(MuscleGroup.BACK, MuscleGroup.ARMS),
            pattern = MovementPattern.PULL,
            equipment = EquipmentType.BODYWEIGHT,
            role = ExerciseRole.ANCHOR,
            priority = ExercisePriority.PRIORITY_A,
            recommendedOrder = 1,
            availableWeights = listOf(0f, 10f, 20f, 25f, 35f, 45f),
            substitutions = listOf("ex-lat-pulldown", "ex-neutral-pullups"),
            notes = "Mantener escápulas activas, rango completo sin balanceo."
        ),
        Exercise(
            id = "ex-ohp",
            name = "Press Militar de Pie (Overhead Press)",
            targetMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.ARMS, MuscleGroup.CORE),
            pattern = MovementPattern.PUSH,
            equipment = EquipmentType.BARBELL,
            role = ExerciseRole.ANCHOR,
            priority = ExercisePriority.PRIORITY_A,
            recommendedOrder = 1,
            availableWeights = barbellWeights,
            substitutions = listOf("ex-db-seated-press", "ex-landmine-press"),
            notes = "Glúteos y core firmes para proteger zona lumbar."
        ),
        Exercise(
            id = "ex-sled-push",
            name = "Sled Push (Empuje de Trineo)",
            targetMuscles = listOf(MuscleGroup.LEGS, MuscleGroup.CARDIO, MuscleGroup.CORE),
            pattern = MovementPattern.CARDIO_INTERVALS,
            equipment = EquipmentType.SLED,
            gymLocation = GymLocation.HYROX_GYM,
            role = ExerciseRole.ANCHOR,
            priority = ExercisePriority.PRIORITY_A,
            recommendedOrder = 1,
            availableWeights = listOf(50f, 90f, 100f, 135f, 180f, 225f),
            isPlateLoaded = true,
            substitutions = listOf("ex-farmer-carries"),
            notes = "Pasto sintético HYROX: 100 lb discos + 30 lb trineo = 130 lb total."
        ),
        Exercise(
            id = "ex-lat-pulldown",
            name = "Jalón al Pecho en Polea",
            targetMuscles = listOf(MuscleGroup.BACK, MuscleGroup.ARMS),
            pattern = MovementPattern.PULL,
            equipment = EquipmentType.CABLE,
            role = ExerciseRole.ANCHOR,
            priority = ExercisePriority.PRIORITY_A,
            recommendedOrder = 2,
            availableWeights = cableWeights,
            substitutions = listOf("ex-pullups"),
            notes = "Agarre medio neutro o supino."
        ),
        // Rotations
        Exercise(
            id = "ex-bulgarian",
            name = "Sentadilla Búlgara con Mancuernas",
            targetMuscles = listOf(MuscleGroup.LEGS, MuscleGroup.CORE),
            pattern = MovementPattern.LUNGE,
            equipment = EquipmentType.DUMBBELL,
            isUnilateral = true,
            isPerHand = true,
            role = ExerciseRole.ROTATION,
            priority = ExercisePriority.PRIORITY_B,
            recommendedOrder = 2,
            availableWeights = dumbbellWeights,
            substitutions = listOf("ex-step-ups", "ex-leg-press"),
            notes = "Inclinación del torso a 45° para mayor activación de glúteo."
        ),
        Exercise(
            id = "ex-landmine-press",
            name = "Press Unilateral Landmine",
            targetMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.CHEST, MuscleGroup.CORE),
            pattern = MovementPattern.PUSH,
            equipment = EquipmentType.BARBELL,
            isUnilateral = true,
            role = ExerciseRole.ROTATION,
            priority = ExercisePriority.PRIORITY_B,
            recommendedOrder = 2,
            availableWeights = listOf(25f, 35f, 45f, 55f),
            substitutions = listOf("ex-ohp", "ex-db-seated-press"),
            notes = "Excelente para salud de hombro y transferencia a empuje."
        ),
        Exercise(
            id = "ex-db-bench",
            name = "Press con Mancuernas en Banco Inclinado",
            targetMuscles = listOf(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.ARMS),
            pattern = MovementPattern.PUSH,
            equipment = EquipmentType.DUMBBELL,
            isPerHand = true,
            role = ExerciseRole.ROTATION,
            priority = ExercisePriority.PRIORITY_B,
            recommendedOrder = 2,
            availableWeights = dumbbellWeights,
            substitutions = listOf("ex-chest-press-machine"),
            notes = "30-40 lb por mano (total 60-80 lb)."
        ),
        Exercise(
            id = "ex-farmer-carries",
            name = "Farmer Carries (Paseo del Granjero)",
            targetMuscles = listOf(MuscleGroup.CORE, MuscleGroup.CARDIO, MuscleGroup.ARMS),
            pattern = MovementPattern.CARRY,
            equipment = EquipmentType.KETTLEBELL,
            isPerHand = true,
            role = ExerciseRole.ANCHOR,
            priority = ExercisePriority.PRIORITY_A,
            recommendedOrder = 3,
            availableWeights = kettlebellWeights,
            substitutions = listOf("ex-sled-push"),
            notes = "Postura erguida, hombros atrás y marcha controlada."
        ),
        // Accessories
        Exercise(
            id = "ex-lateral-raises",
            name = "Elevaciones Laterales con Mancuernas",
            targetMuscles = listOf(MuscleGroup.SHOULDERS),
            pattern = MovementPattern.ISOLATION,
            equipment = EquipmentType.DUMBBELL,
            isPerHand = true,
            role = ExerciseRole.ACCESSORY,
            priority = ExercisePriority.ACCESSORY,
            recommendedOrder = 4,
            availableWeights = listOf(10f, 15f, 20f, 25f),
            substitutions = listOf("ex-cable-lateral-raises"),
            notes = "Controlar la bajada en 2 segundos."
        ),
        Exercise(
            id = "ex-pallof",
            name = "Pallof Press Antirrotacional",
            targetMuscles = listOf(MuscleGroup.CORE),
            pattern = MovementPattern.ROTATION,
            equipment = EquipmentType.CABLE,
            role = ExerciseRole.ACCESSORY,
            priority = ExercisePriority.ACCESSORY,
            recommendedOrder = 4,
            availableWeights = listOf(15f, 20f, 25f, 30f),
            substitutions = listOf("ex-hollow-body"),
            notes = "Pausa de 2 segundos en extensión completa."
        ),
        Exercise(
            id = "ex-incline-curl",
            name = "Curl de Bíceps en Banco Inclinado",
            targetMuscles = listOf(MuscleGroup.ARMS),
            pattern = MovementPattern.ISOLATION,
            equipment = EquipmentType.DUMBBELL,
            isPerHand = true,
            role = ExerciseRole.ACCESSORY,
            priority = ExercisePriority.ACCESSORY,
            recommendedOrder = 5,
            availableWeights = listOf(15f, 20f, 25f, 30f),
            substitutions = emptyList(),
            notes = "Máxima elongación de cabeza larga del bíceps."
        ),
        Exercise(
            id = "ex-triceps-rope",
            name = "Extensión de Tríceps en Cuerda",
            targetMuscles = listOf(MuscleGroup.ARMS),
            pattern = MovementPattern.ISOLATION,
            equipment = EquipmentType.CABLE,
            role = ExerciseRole.ACCESSORY,
            priority = ExercisePriority.ACCESSORY,
            recommendedOrder = 5,
            availableWeights = cableWeights,
            substitutions = emptyList(),
            notes = "Abrir la cuerda al final de la contracción."
        )
    )

    fun getExerciseMap(): Map<String, Exercise> = exercises.associateBy { it.id }

    fun getInitialWeeklyPlan(startDate: LocalDate = LocalDate.now()): List<WorkoutSession> {
        val exMap = getExerciseMap()

        // Lunes: Fuerza Torso & Hombros (Anchor: Press Militar, Dominadas)
        val mondayExercises = listOf(
            WorkoutExercise(
                exercise = exMap["ex-ohp"]!!,
                sets = listOf(
                    LoggedSet(1, 8, null, 85f, null, status = SetStatus.PENDING),
                    LoggedSet(2, 8, null, 95f, null, status = SetStatus.PENDING),
                    LoggedSet(3, 8, null, 95f, null, status = SetStatus.PENDING),
                    LoggedSet(4, 8, null, 95f, null, status = SetStatus.PENDING)
                ),
                order = 1
            ),
            WorkoutExercise(
                exercise = exMap["ex-pullups"]!!,
                sets = listOf(
                    LoggedSet(1, 6, null, 0f, null, status = SetStatus.PENDING),
                    LoggedSet(2, 6, null, 0f, null, status = SetStatus.PENDING),
                    LoggedSet(3, 6, null, 0f, null, status = SetStatus.PENDING),
                    LoggedSet(4, 6, null, 0f, null, status = SetStatus.PENDING)
                ),
                order = 2
            ),
            WorkoutExercise(
                exercise = exMap["ex-lateral-raises"]!!,
                sets = listOf(
                    LoggedSet(1, 12, null, 20f, null, isPerHand = true, status = SetStatus.PENDING),
                    LoggedSet(2, 12, null, 20f, null, isPerHand = true, status = SetStatus.PENDING),
                    LoggedSet(3, 12, null, 20f, null, isPerHand = true, status = SetStatus.PENDING)
                ),
                order = 3
            ),
            WorkoutExercise(
                exercise = exMap["ex-pallof"]!!,
                sets = listOf(
                    LoggedSet(1, 10, null, 25f, null, status = SetStatus.PENDING),
                    LoggedSet(2, 10, null, 25f, null, status = SetStatus.PENDING)
                ),
                order = 4
            )
        )

        val monday = WorkoutSession(
            sessionId = "s-1",
            name = "Fuerza Torso: Hombro & Espalda V",
            dayOfWeek = DayOfWeek.MONDAY,
            date = startDate,
            priority = SessionPriority.HIGH,
            isMovable = true,
            isAnchorSession = true,
            plannedDurationMinutes = 60,
            variant = SessionVariant.FULL,
            exercises = mondayExercises.toMutableList(),
            loads = MuscleLoads(shoulders = 4, back = 4, arms = 3, core = 3, globalFatigue = 3),
            notes = "Foco en mantener hombros densos y amplitud dorsal."
        )

        // Martes: Piernas & Fuerza Unilateral
        val tuesdayExercises = listOf(
            WorkoutExercise(
                exercise = exMap["ex-bulgarian"]!!,
                sets = listOf(
                    LoggedSet(1, 8, null, 30f, null, isPerHand = true),
                    LoggedSet(2, 8, null, 35f, null, isPerHand = true),
                    LoggedSet(3, 8, null, 35f, null, isPerHand = true)
                ),
                order = 1
            ),
            WorkoutExercise(
                exercise = exMap["ex-farmer-carries"]!!,
                sets = listOf(
                    LoggedSet(1, 1, null, 44f, null, isPerHand = true, notes = "30 metros"),
                    LoggedSet(2, 1, null, 44f, null, isPerHand = true, notes = "30 metros"),
                    LoggedSet(3, 1, null, 44f, null, isPerHand = true, notes = "30 metros")
                ),
                order = 2
            )
        )

        val tuesday = WorkoutSession(
            sessionId = "s-2",
            name = "Fuerza Piernas & Carries",
            dayOfWeek = DayOfWeek.TUESDAY,
            date = startDate.plusDays(1),
            priority = SessionPriority.HIGH,
            isMovable = true,
            isAnchorSession = false,
            plannedDurationMinutes = 55,
            variant = SessionVariant.FULL,
            exercises = tuesdayExercises.toMutableList(),
            loads = MuscleLoads(legs = 5, core = 4, grip = 4, cardio = 2, globalFatigue = 4),
            notes = "Pierna atlética sin volumen excesivo."
        )

        // Miércoles: Descanso Activo / Movilidad
        val wednesday = WorkoutSession(
            sessionId = "s-3",
            name = "Descanso Activo / Movilidad",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            date = startDate.plusDays(2),
            priority = SessionPriority.LOW,
            isMovable = true,
            plannedDurationMinutes = 30,
            variant = SessionVariant.RECOVERY,
            exercises = mutableListOf(),
            loads = MuscleLoads(cardio = 1, globalFatigue = 1),
            status = SessionStatus.REST,
            notes = "Caminata suave, estiramiento dinámico o natación ligera."
        )

        // Jueves: HYROX Capacidad & Sled
        val thursdayExercises = listOf(
            WorkoutExercise(
                exercise = exMap["ex-sled-push"]!!,
                sets = listOf(
                    LoggedSet(1, 1, null, 100f, null, isPlateLoaded = true, notes = "20 m"),
                    LoggedSet(2, 1, null, 100f, null, isPlateLoaded = true, notes = "20 m"),
                    LoggedSet(3, 1, null, 100f, null, isPlateLoaded = true, notes = "20 m")
                ),
                order = 1
            ),
            WorkoutExercise(
                exercise = exMap["ex-farmer-carries"]!!,
                sets = listOf(
                    LoggedSet(1, 1, null, 53f, null, isPerHand = true, notes = "50 m"),
                    LoggedSet(2, 1, null, 53f, null, isPerHand = true, notes = "50 m")
                ),
                order = 2
            ),
            WorkoutExercise(
                exercise = exMap["ex-pallof"]!!,
                sets = listOf(
                    LoggedSet(1, 12, null, 25f, null),
                    LoggedSet(2, 12, null, 25f, null)
                ),
                order = 3
            )
        )

        val thursday = WorkoutSession(
            sessionId = "s-4",
            name = "HYROX Sled & Potencia",
            dayOfWeek = DayOfWeek.THURSDAY,
            date = startDate.plusDays(3),
            priority = SessionPriority.HIGH,
            isMovable = true,
            isAnchorSession = true,
            plannedDurationMinutes = 60,
            variant = SessionVariant.FULL,
            exercises = thursdayExercises.toMutableList(),
            loads = MuscleLoads(legs = 4, cardio = 4, core = 3, grip = 3, globalFatigue = 5),
            notes = "Ritmo sostenido con transición a trineo."
        )

        // Viernes: Hipertrofia Torso & Brazos
        val fridayExercises = listOf(
            WorkoutExercise(
                exercise = exMap["ex-db-bench"]!!,
                sets = listOf(
                    LoggedSet(1, 10, null, 40f, null, isPerHand = true),
                    LoggedSet(2, 10, null, 40f, null, isPerHand = true),
                    LoggedSet(3, 10, null, 40f, null, isPerHand = true)
                ),
                order = 1
            ),
            WorkoutExercise(
                exercise = exMap["ex-lat-pulldown"]!!,
                sets = listOf(
                    LoggedSet(1, 10, null, 70f, null),
                    LoggedSet(2, 10, null, 70f, null),
                    LoggedSet(3, 10, null, 70f, null)
                ),
                order = 2
            ),
            WorkoutExercise(
                exercise = exMap["ex-incline-curl"]!!,
                sets = listOf(
                    LoggedSet(1, 12, null, 25f, null, isPerHand = true),
                    LoggedSet(2, 12, null, 25f, null, isPerHand = true)
                ),
                order = 3
            ),
            WorkoutExercise(
                exercise = exMap["ex-triceps-rope"]!!,
                sets = listOf(
                    LoggedSet(1, 12, null, 40f, null),
                    LoggedSet(2, 12, null, 40f, null)
                ),
                order = 4
            )
        )

        val friday = WorkoutSession(
            sessionId = "s-5",
            name = "Hipertrofia Torso & Brazos",
            dayOfWeek = DayOfWeek.FRIDAY,
            date = startDate.plusDays(4),
            priority = SessionPriority.MEDIUM,
            isMovable = true,
            plannedDurationMinutes = 55,
            variant = SessionVariant.FULL,
            exercises = fridayExercises.toMutableList(),
            loads = MuscleLoads(chest = 4, back = 4, arms = 4, shoulders = 2, globalFatigue = 3),
            notes = "Bombear pecho superior y brazos densos."
        )

        // Sábado: Salida Ciclismo Z2 / MTB
        val saturday = WorkoutSession(
            sessionId = "s-6",
            name = "Ciclismo Z2 o Aventura Libre",
            dayOfWeek = DayOfWeek.SATURDAY,
            date = startDate.plusDays(5),
            priority = SessionPriority.MEDIUM,
            isMovable = true,
            plannedDurationMinutes = 120,
            variant = SessionVariant.FULL,
            exercises = mutableListOf(),
            loads = MuscleLoads(legs = 3, cardio = 3, globalFatigue = 3),
            notes = "Rodar al aire libre disfrutando del paisaje."
        )

        // Domingo: Recuperación & Planificación
        val sunday = WorkoutSession(
            sessionId = "s-7",
            name = "Recuperación / Movilidad",
            dayOfWeek = DayOfWeek.SUNDAY,
            date = startDate.plusDays(6),
            priority = SessionPriority.LOW,
            isMovable = true,
            plannedDurationMinutes = 25,
            variant = SessionVariant.RECOVERY,
            exercises = mutableListOf(),
            loads = MuscleLoads(globalFatigue = 1),
            status = SessionStatus.REST,
            notes = "Caminar, sauna, preparar semana."
        )

        return listOf(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
    }

    val sampleGarmin = GarminDailyMetrics(
        date = LocalDate.now(),
        hrvCurrent = 62.4f,
        hrvBaseline28d = 64.0f, // -2.5% vs baseline (stable)
        rhrCurrent = 49,
        rhrBaseline28d = 48,
        sleepDurationHours = 7.6f,
        sleepScore = 84,
        sleepQuality = SleepQuality.WELL,
        bodyBattery = 82,
        stressScore = 22,
        steps = 11420,
        recoveryHours = 20
    )

    val sampleCheckIn = SubjectiveCheckIn(
        date = LocalDate.now(),
        energyScore = 4,
        legSoreness = 2,
        torsoSoreness = 1,
        musclePain = 1,
        stressScore = 2,
        motivationScore = 5,
        sleptWell = SleepQuality.WELL,
        notes = "Me desperté con ganas de tirar pesado. Cero dolor de articulaciones."
    )

    val personalRecords = listOf(
        PersonalRecord("Dominadas Pronas", "REP_PR", "12 reps con peso corporal", "12 Oct 2026", "Rango estricto sin impulso."),
        PersonalRecord("Press Militar", "LOAD_PR", "115 lb × 5 reps", "18 Oct 2026", "Subió sólido y con bloqueo total."),
        PersonalRecord("Sled Push HYROX", "CAPACITY_PR", "100 lb discos + trineo × 4 × 25 m", "05 Oct 2026", "Pasto de pista con buen empuje."),
        PersonalRecord("Farmer Carries", "LOAD_PR", "53 lb / mano × 40 m", "22 Oct 2026", "Agarre en hierro fundido."),
        PersonalRecord("Ciclismo Fondo", "CAPACITY_PR", "128 km / 4h 15m Z2", "14 Sep 2026", "Buena recarga de carbohidratos en ruta.")
    )

    val macroBlocks = listOf(
        AnnualMacroBlock(1, "Base Técnica & Capacidad", 4, "Alineación postural y capacidad aeróbica"),
        AnnualMacroBlock(2, "Hipertrofia Atlética I", 4, "Espalda en V, hombros y brazos densos"),
        AnnualMacroBlock(3, "Fuerza Relativa & Anchors", 4, "Consolidación de dominadas y press militar"),
        AnnualMacroBlock(4, "Capacidad Híbrida & HYROX", 4, "Fuerza funcional + transferencias de sled", isCurrent = true),
        AnnualMacroBlock(5, "Hipertrofia Torso II", 4, "Pectoral superior y deltoides lateral"),
        AnnualMacroBlock(6, "Fuerza II & Potencia", 4, "Cargas pesadas con descansos completos"),
        AnnualMacroBlock(7, "HYROX Técnica de Pistas", 4, "Simulación de eventos y carrera"),
        AnnualMacroBlock(8, "Potencia & Resistencia", 4, "Kettlebells y transferencias explosivas"),
        AnnualMacroBlock(9, "Hipertrofia Compacta III", 4, "Densidad sin ganar peso innecesario"),
        AnnualMacroBlock(10, "Fuerza Relativa Máxima", 4, "Ratio fuerza/peso Spider-Man"),
        AnnualMacroBlock(11, "HYROX Capacidad Pico", 4, "Test general de simulación"),
        AnnualMacroBlock(12, "PR / Test Week", 4, "Semana de marcas personales"),
        AnnualMacroBlock(13, "Transición & Deload Regenerativo", 4, "Descanso y mantenimiento articular")
    )
}
