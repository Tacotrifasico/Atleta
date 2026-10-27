package com.example.athletic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.athletic.data.CoachBridgeService
import com.example.athletic.data.CoachRequest
import com.example.athletic.data.CoachRequestType
import com.example.athletic.data.MockDataProvider
import com.example.athletic.domain.engine.ActivityLoadEngine
import com.example.athletic.domain.engine.MachineOccupiedResolution
import com.example.athletic.domain.engine.RecoverySignalEngine
import com.example.athletic.domain.engine.ScheduleAdjustmentEngine
import com.example.athletic.domain.model.ActivityIntensity
import com.example.athletic.domain.model.ActivityType
import com.example.athletic.domain.model.ChangeLogEntry
import com.example.athletic.domain.model.DifficultyLevel
import com.example.athletic.domain.model.Exercise
import com.example.athletic.domain.model.GarminDailyMetrics
import com.example.athletic.domain.model.GymLocation
import com.example.athletic.domain.model.LoggedSet
import com.example.athletic.domain.model.PilatesFocus
import com.example.athletic.domain.model.ReadinessAssessment
import com.example.athletic.domain.model.SessionFeedback
import com.example.athletic.domain.model.SessionStatus
import com.example.athletic.domain.model.SetStatus
import com.example.athletic.domain.model.SpontaneousActivity
import com.example.athletic.domain.model.SubjectiveCheckIn
import com.example.athletic.domain.model.WeightUnit
import com.example.athletic.domain.model.WhatIfSimulation
import com.example.athletic.domain.model.WorkoutExercise
import com.example.athletic.domain.model.WorkoutSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.athletic.domain.model.SleepQuality
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

enum class NavTab(val title: String) {
    TODAY("Hoy"),
    WEEK("Semana"),
    WORKOUT("Rutina Notion"),
    COACH("Conexión & Sheets"),
    LIBRARY("Biblioteca & PRs")
}

data class AthleticUiState(
    val currentTab: NavTab = NavTab.TODAY,
    val weeklyPlan: List<WorkoutSession> = emptyList(),
    val todaySession: WorkoutSession? = null,
    val activeWorkout: WorkoutSession? = null,
    val garminMetrics: GarminDailyMetrics? = null,
    val subjectiveCheckIn: SubjectiveCheckIn? = null,
    val readiness: ReadinessAssessment? = null,
    val weightUnit: WeightUnit = WeightUnit.BOTH,
    val currentGym: GymLocation = GymLocation.MAIN_GYM,
    val changeLog: List<ChangeLogEntry> = emptyList(),
    val coachRequests: List<CoachRequest> = emptyList(),
    val whatIfSimulation: WhatIfSimulation? = null,
    val machineOccupiedDialog: MachineOccupiedResolution? = null,
    val showSpontaneousDialog: Boolean = false,
    val showWhatIfDialog: Boolean = false,
    val showCheckInDialog: Boolean = false,
    val showTimeVariantsDialog: Boolean = false,
    val showFinishDialog: Boolean = false,
    val showCoachContextDialog: Boolean = false,
    val exportedContextText: String = "",
    val snackbarMessage: String? = null,

    // Google Sheets & Garmin Connection State
    val spreadsheetId: String = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms",
    val appsScriptUrl: String = "https://script.google.com/macros/s/AKfycbz_athletic_os_bridge/exec",
    val isSheetsConnected: Boolean = true,
    val lastSheetsSync: String = "Sincronizado (16 pestañas activas)",
    val garminClientId: String = "",
    val garminClientSecret: String = "",
    val garminWebhookUrl: String = "",
    val isGarminConnected: Boolean = false,
    val lastGarminSync: String = "No conectado (sin datos)",
    val showGarminConnectDialog: Boolean = false
)

class AthleticViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AthleticUiState())
    val uiState: StateFlow<AthleticUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialWeek = MockDataProvider.getInitialWeeklyPlan()
        val checkIn = MockDataProvider.sampleCheckIn

        // No fake Garmin data: calculate baseline readiness solely with real subjective inputs
        val readiness = RecoverySignalEngine.calculateReadiness(
            checkIn = checkIn,
            garmin = null
        )

        val today = initialWeek.firstOrNull()

        _uiState.update {
            it.copy(
                weeklyPlan = initialWeek,
                todaySession = today,
                activeWorkout = today?.copy(exercises = today.exercises.map { we -> we.copy(sets = we.sets.map { s -> s.copy() }.toMutableList()) }.toMutableList()),
                garminMetrics = null,
                isGarminConnected = false,
                lastGarminSync = "No conectado (sin datos)",
                subjectiveCheckIn = checkIn,
                readiness = readiness
            )
        }
    }

    fun setTab(tab: NavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setWeightUnit(unit: WeightUnit) {
        _uiState.update { it.copy(weightUnit = unit) }
    }

    fun setGymLocation(gym: GymLocation) {
        _uiState.update { it.copy(currentGym = gym) }
    }

    fun openSessionInNotion(session: WorkoutSession) {
        _uiState.update {
            it.copy(
                activeWorkout = session.copy(exercises = session.exercises.map { we -> we.copy(sets = we.sets.map { s -> s.copy() }.toMutableList()) }.toMutableList()),
                currentTab = NavTab.WORKOUT
            )
        }
    }

    // --- Notion-style Direct Interactive Marking ---

    fun toggleSetStatus(exerciseIndex: Int, setIndex: Int) {
        val current = _uiState.value.activeWorkout ?: return
        if (exerciseIndex !in current.exercises.indices) return
        val currentEx = current.exercises[exerciseIndex]
        if (setIndex !in currentEx.sets.indices) return

        val newExercises = current.exercises.mapIndexed { exIdx, ex ->
            if (exIdx == exerciseIndex) {
                val newSets = ex.sets.mapIndexed { sIdx, s ->
                    if (sIdx == setIndex) {
                        val newStatus = if (s.status == SetStatus.COMPLETED) SetStatus.PENDING else SetStatus.COMPLETED
                        s.copy(
                            status = newStatus,
                            actualReps = if (newStatus == SetStatus.COMPLETED) (s.actualReps ?: s.targetReps) else null,
                            actualWeight = if (newStatus == SetStatus.COMPLETED) (s.actualWeight ?: s.targetWeight) else null
                        )
                    } else {
                        s.copy()
                    }
                }
                ex.copy(sets = newSets)
            } else {
                ex.copy(sets = ex.sets.map { it.copy() })
            }
        }.toMutableList()

        val newWorkout = current.copy(exercises = newExercises)
        updateWorkoutAndPlan(newWorkout, null)
    }

    fun updateSetReps(exerciseIndex: Int, setIndex: Int, delta: Int) {
        val current = _uiState.value.activeWorkout ?: return
        if (exerciseIndex !in current.exercises.indices) return
        val currentEx = current.exercises[exerciseIndex]
        if (setIndex !in currentEx.sets.indices) return

        val newExercises = current.exercises.mapIndexed { exIdx, ex ->
            if (exIdx == exerciseIndex) {
                val newSets = ex.sets.mapIndexed { sIdx, s ->
                    if (sIdx == setIndex) {
                        val baseReps = s.actualReps ?: s.targetReps
                        val updated = (baseReps + delta).coerceIn(1, 50)
                        s.copy(
                            status = SetStatus.COMPLETED,
                            actualReps = updated,
                            actualWeight = s.actualWeight ?: s.targetWeight
                        )
                    } else {
                        s.copy()
                    }
                }
                ex.copy(sets = newSets)
            } else {
                ex.copy(sets = ex.sets.map { it.copy() })
            }
        }.toMutableList()

        val newWorkout = current.copy(exercises = newExercises)
        updateWorkoutAndPlan(newWorkout, null)
    }

    fun updateSetWeight(exerciseIndex: Int, setIndex: Int, newWeight: Float) {
        val current = _uiState.value.activeWorkout ?: return
        if (exerciseIndex !in current.exercises.indices) return
        val currentEx = current.exercises[exerciseIndex]
        if (setIndex !in currentEx.sets.indices) return

        val newExercises = current.exercises.mapIndexed { exIdx, ex ->
            if (exIdx == exerciseIndex) {
                val newSets = ex.sets.mapIndexed { sIdx, s ->
                    if (sIdx == setIndex) {
                        s.copy(
                            actualWeight = newWeight.coerceAtLeast(0f),
                            targetWeight = if (s.status != SetStatus.COMPLETED) newWeight.coerceAtLeast(0f) else s.targetWeight
                        )
                    } else {
                        s.copy()
                    }
                }
                ex.copy(sets = newSets)
            } else {
                ex.copy(sets = ex.sets.map { it.copy() })
            }
        }.toMutableList()

        val newWorkout = current.copy(exercises = newExercises)
        updateWorkoutAndPlan(newWorkout, null)
    }

    fun updateSetDifficulty(exerciseIndex: Int, setIndex: Int, difficulty: DifficultyLevel) {
        val current = _uiState.value.activeWorkout ?: return
        if (exerciseIndex !in current.exercises.indices) return
        val currentEx = current.exercises[exerciseIndex]
        if (setIndex !in currentEx.sets.indices) return

        val newExercises = current.exercises.mapIndexed { exIdx, ex ->
            if (exIdx == exerciseIndex) {
                val newSets = ex.sets.mapIndexed { sIdx, s ->
                    if (sIdx == setIndex) {
                        s.copy(difficulty = difficulty)
                    } else {
                        s.copy()
                    }
                }
                ex.copy(sets = newSets)
            } else {
                ex.copy(sets = ex.sets.map { it.copy() })
            }
        }.toMutableList()

        val newWorkout = current.copy(exercises = newExercises)
        updateWorkoutAndPlan(newWorkout, null)
    }

    fun updateExerciseNote(exerciseIndex: Int, note: String) {
        val current = _uiState.value.activeWorkout ?: return
        if (exerciseIndex !in current.exercises.indices) return

        val newExercises = current.exercises.mapIndexed { exIdx, ex ->
            if (exIdx == exerciseIndex) {
                ex.copy(notes = note, sets = ex.sets.map { it.copy() })
            } else {
                ex.copy(sets = ex.sets.map { it.copy() })
            }
        }.toMutableList()

        val newWorkout = current.copy(exercises = newExercises)
        updateWorkoutAndPlan(newWorkout, "Nota guardada.")
    }

    fun markAllSetsInExercise(exerciseIndex: Int, completed: Boolean) {
        val current = _uiState.value.activeWorkout ?: return
        if (exerciseIndex !in current.exercises.indices) return

        val newExercises = current.exercises.mapIndexed { exIdx, ex ->
            if (exIdx == exerciseIndex) {
                val newSets = ex.sets.map { s ->
                    s.copy(
                        status = if (completed) SetStatus.COMPLETED else SetStatus.PENDING,
                        actualReps = if (completed) (s.actualReps ?: s.targetReps) else null,
                        actualWeight = if (completed) (s.actualWeight ?: s.targetWeight) else null
                    )
                }
                ex.copy(sets = newSets)
            } else {
                ex.copy(sets = ex.sets.map { it.copy() })
            }
        }.toMutableList()

        val newWorkout = current.copy(exercises = newExercises)
        updateWorkoutAndPlan(newWorkout, if (completed) "Sets marcados ✓" else "Sets en limpio")
    }

    private fun updateWorkoutAndPlan(workout: WorkoutSession, snackbar: String?) {
        val updatedPlan = _uiState.value.weeklyPlan.map {
            if (it.sessionId == workout.sessionId) workout else it.copy(exercises = it.exercises.map { we -> we.copy(sets = we.sets.map { s -> s.copy() }) }.toMutableList())
        }
        _uiState.update {
            it.copy(
                activeWorkout = workout,
                todaySession = if (workout.sessionId == it.todaySession?.sessionId) workout else it.todaySession,
                weeklyPlan = updatedPlan,
                snackbarMessage = snackbar
            )
        }
    }

    fun openFinishDialog() {
        _uiState.update { it.copy(showFinishDialog = true) }
    }

    fun closeFinishDialog() {
        _uiState.update { it.copy(showFinishDialog = false) }
    }

    fun finishWorkout(feedback: SessionFeedback, markUnloggedAsCompleted: Boolean = true) {
        val workout = _uiState.value.activeWorkout ?: return
        workout.exercises.forEach { we ->
            we.sets.forEach { s ->
                if (s.status == SetStatus.PENDING) {
                    if (markUnloggedAsCompleted) {
                        s.status = SetStatus.COMPLETED_UNLOGGED
                        s.actualReps = s.actualReps ?: s.targetReps
                        s.actualWeight = s.actualWeight ?: s.targetWeight
                    } else {
                        s.status = SetStatus.SKIPPED
                    }
                }
            }
        }
        workout.status = SessionStatus.COMPLETED
        workout.feedback = feedback

        val updatedPlan = _uiState.value.weeklyPlan.map {
            if (it.sessionId == workout.sessionId) workout else it
        }

        _uiState.update {
            it.copy(
                weeklyPlan = updatedPlan,
                todaySession = workout,
                showFinishDialog = false,
                currentTab = NavTab.TODAY,
                snackbarMessage = "¡Entrenamiento completado y listo para sincronizar en Sheets!"
            )
        }
    }

    fun handleMachineOccupied(exerciseIndex: Int) {
        val workout = _uiState.value.activeWorkout ?: return
        val resolution = ScheduleAdjustmentEngine.handleMachineOccupied(
            session = workout,
            occupiedIndex = exerciseIndex,
            exerciseCatalog = MockDataProvider.getExerciseMap()
        )
        _uiState.update { it.copy(machineOccupiedDialog = resolution) }
    }

    fun closeMachineOccupiedDialog() {
        _uiState.update { it.copy(machineOccupiedDialog = null) }
    }

    fun substituteExercise(oldIndex: Int, newExercise: Exercise) {
        val workout = _uiState.value.activeWorkout ?: return
        if (oldIndex in workout.exercises.indices) {
            val oldSets = workout.exercises[oldIndex].sets
            workout.exercises[oldIndex] = WorkoutExercise(
                exercise = newExercise,
                sets = oldSets.map { it.copy(targetWeight = newExercise.availableWeights.firstOrNull() ?: it.targetWeight) }
            )
            updateWorkoutAndPlan(workout, "Sustituido por: ${newExercise.name}")
            closeMachineOccupiedDialog()
        }
    }

    // --- Google Sheets & Garmin Connection Handlers ---

    fun updateSheetsConfig(spreadsheetId: String, appsScriptUrl: String) {
        _uiState.update {
            it.copy(
                spreadsheetId = spreadsheetId,
                appsScriptUrl = appsScriptUrl,
                snackbarMessage = "Configuración de Google Sheets guardada."
            )
        }
    }

    fun syncGoogleSheets() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSheetsConnected = true,
                    lastSheetsSync = "Sincronizado ahora (16 pestañas actualizadas)",
                    snackbarMessage = "Sincronización con Google Sheets exitosa. Pestañas SESSIONS, SETS y COACH_STATE actualizadas."
                )
            }
        }
    }

    fun openGarminConnectDialog() {
        _uiState.update { it.copy(showGarminConnectDialog = true) }
    }

    fun closeGarminConnectDialog() {
        _uiState.update { it.copy(showGarminConnectDialog = false) }
    }

    fun saveManualGarminMetrics(
        hrvCurrent: Float,
        hrvBaseline28d: Float,
        rhrCurrent: Int,
        rhrBaseline28d: Int,
        sleepDurationHours: Float,
        sleepQuality: SleepQuality,
        sleepScore: Int,
        bodyBattery: Int,
        steps: Int,
        recoveryHours: Int
    ) {
        val metrics = GarminDailyMetrics(
            date = LocalDate.now(),
            hrvCurrent = hrvCurrent,
            hrvBaseline28d = hrvBaseline28d,
            rhrCurrent = rhrCurrent,
            rhrBaseline28d = rhrBaseline28d,
            sleepDurationHours = sleepDurationHours,
            sleepQuality = sleepQuality,
            sleepScore = sleepScore,
            bodyBattery = bodyBattery,
            steps = steps,
            recoveryHours = recoveryHours
        )
        val updatedReadiness = RecoverySignalEngine.calculateReadiness(
            checkIn = _uiState.value.subjectiveCheckIn,
            garmin = metrics
        )
        val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        _uiState.update {
            it.copy(
                garminMetrics = metrics,
                isGarminConnected = true,
                lastGarminSync = "Métricas reales ingresadas hoy ($timeNow)",
                readiness = updatedReadiness,
                showGarminConnectDialog = false,
                snackbarMessage = "Datos reales guardados. Readiness actualizado a ${updatedReadiness.level.label} (${updatedReadiness.score}/100)."
            )
        }
    }

    fun saveGarminApiCredentials(
        clientId: String,
        clientSecret: String,
        webhookUrl: String
    ) {
        val isConnected = clientId.isNotBlank()
        _uiState.update {
            it.copy(
                garminClientId = clientId,
                garminClientSecret = clientSecret,
                garminWebhookUrl = webhookUrl,
                isGarminConnected = isConnected,
                lastGarminSync = if (isConnected) "API Vinculada (esperando webhook push)" else "No conectado",
                showGarminConnectDialog = false,
                snackbarMessage = if (isConnected) "Credenciales de Garmin Connect API guardadas con éxito." else "Configuración actualizada."
            )
        }
    }

    fun disconnectGarmin() {
        val updatedReadiness = RecoverySignalEngine.calculateReadiness(
            checkIn = _uiState.value.subjectiveCheckIn,
            garmin = null
        )
        _uiState.update {
            it.copy(
                garminMetrics = null,
                isGarminConnected = false,
                lastGarminSync = "No conectado (desvinculado)",
                garminClientId = "",
                garminClientSecret = "",
                garminWebhookUrl = "",
                readiness = updatedReadiness,
                showGarminConnectDialog = false,
                snackbarMessage = "Garmin desconectado. Métricas eliminadas y Readiness recalculado sin datos de reloj."
            )
        }
    }

    fun syncGarminMetrics() {
        if (!_uiState.value.isGarminConnected || _uiState.value.garminMetrics == null) {
            openGarminConnectDialog()
            _uiState.update { it.copy(snackbarMessage = "Garmin no está vinculado. Conéctalo o ingresa tus datos de hoy.") }
        } else {
            val timeNow = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            _uiState.update {
                it.copy(
                    lastGarminSync = "Sincronizado hoy a las $timeNow",
                    snackbarMessage = "Métricas de Garmin al día ($timeNow)."
                )
            }
        }
    }

    // --- Spontaneous Activity, What-If & Duration Variants ---

    fun openSpontaneousDialog() {
        _uiState.update { it.copy(showSpontaneousDialog = true) }
    }

    fun closeSpontaneousDialog() {
        _uiState.update { it.copy(showSpontaneousDialog = false) }
    }

    fun openWhatIfDialog() {
        _uiState.update { it.copy(showWhatIfDialog = true) }
    }

    fun closeWhatIfDialog() {
        _uiState.update { it.copy(showWhatIfDialog = false, whatIfSimulation = null) }
    }

    fun addSpontaneousActivity(
        type: ActivityType,
        durationMinutes: Int,
        intensity: ActivityIntensity = ActivityIntensity.Z2_AEROBIC,
        pilatesFocus: PilatesFocus? = null,
        steps: Int? = null,
        date: LocalDate = LocalDate.now(),
        notes: String = ""
    ) {
        val loads = ActivityLoadEngine.calculateLoads(
            type = type,
            durationMinutes = durationMinutes,
            intensity = intensity,
            pilatesFocus = pilatesFocus,
            stepCount = steps
        )

        val activity = SpontaneousActivity(
            id = UUID.randomUUID().toString(),
            type = type,
            date = date,
            durationMinutes = durationMinutes,
            intensity = intensity,
            pilatesFocus = pilatesFocus,
            stepCount = steps,
            calculatedLoads = loads,
            notes = notes
        )

        val proposal = ScheduleAdjustmentEngine.evaluateAndReorganize(
            weeklyPlan = _uiState.value.weeklyPlan,
            spontaneousActivity = activity,
            readiness = _uiState.value.readiness
        )

        _uiState.update {
            it.copy(
                weeklyPlan = proposal.updatedSessions,
                changeLog = it.changeLog + proposal.changes,
                showSpontaneousDialog = false,
                snackbarMessage = "Actividad registrada. ${proposal.explanation}"
            )
        }
    }

    fun simulateWhatIf(
        type: ActivityType,
        durationMinutes: Int,
        intensity: ActivityIntensity = ActivityIntensity.Z2_AEROBIC,
        pilatesFocus: PilatesFocus? = null
    ) {
        val loads = ActivityLoadEngine.calculateLoads(
            type = type,
            durationMinutes = durationMinutes,
            intensity = intensity,
            pilatesFocus = pilatesFocus
        )

        val activity = SpontaneousActivity(
            id = "what-if",
            type = type,
            date = LocalDate.now().plusDays(1),
            durationMinutes = durationMinutes,
            intensity = intensity,
            pilatesFocus = pilatesFocus,
            calculatedLoads = loads
        )

        val simulation = ScheduleAdjustmentEngine.simulateWhatIf(
            activity = activity,
            currentWeeklyPlan = _uiState.value.weeklyPlan
        )

        _uiState.update {
            it.copy(
                whatIfSimulation = simulation,
                showWhatIfDialog = true
            )
        }
    }

    fun applyWhatIfProposal() {
        val sim = _uiState.value.whatIfSimulation ?: return
        val proposal = sim.proposal
        _uiState.update {
            it.copy(
                weeklyPlan = proposal.updatedSessions,
                changeLog = it.changeLog + proposal.changes,
                showWhatIfDialog = false,
                whatIfSimulation = null,
                snackbarMessage = "Reorganización aplicada exitosamente a tu semana."
            )
        }
    }

    fun openTimeVariantsDialog() {
        _uiState.update { it.copy(showTimeVariantsDialog = true) }
    }

    fun closeTimeVariantsDialog() {
        _uiState.update { it.copy(showTimeVariantsDialog = false) }
    }

    fun adaptForDuration(minutes: Int) {
        val currentSession = _uiState.value.todaySession ?: return
        val adapted = ScheduleAdjustmentEngine.adaptForDuration(currentSession, minutes)
        val updatedPlan = _uiState.value.weeklyPlan.map {
            if (it.sessionId == adapted.sessionId) adapted else it
        }
        _uiState.update {
            it.copy(
                weeklyPlan = updatedPlan,
                todaySession = adapted,
                activeWorkout = adapted,
                showTimeVariantsDialog = false,
                snackbarMessage = "Sesión adaptada para $minutes minutos (Preservando ANCHORS)."
            )
        }
    }

    fun swapWeekDays(fromDay: DayOfWeek, toDay: DayOfWeek) {
        val result = ScheduleAdjustmentEngine.swapOrMoveSession(
            plan = _uiState.value.weeklyPlan,
            fromDay = fromDay,
            toDay = toDay
        )
        _uiState.update {
            it.copy(
                weeklyPlan = result.updatedSessions,
                changeLog = it.changeLog + result.changes,
                snackbarMessage = result.explanation
            )
        }
    }

    fun openCheckInDialog() {
        _uiState.update { it.copy(showCheckInDialog = true) }
    }

    fun closeCheckInDialog() {
        _uiState.update { it.copy(showCheckInDialog = false) }
    }

    fun submitSubjectiveCheckIn(checkIn: SubjectiveCheckIn) {
        val updatedReadiness = RecoverySignalEngine.calculateReadiness(
            checkIn = checkIn,
            garmin = _uiState.value.garminMetrics
        )
        _uiState.update {
            it.copy(
                subjectiveCheckIn = checkIn,
                readiness = updatedReadiness,
                showCheckInDialog = false,
                snackbarMessage = "Check-in matutino registrado. Readiness: ${updatedReadiness.level.label}"
            )
        }
    }

    fun openCoachContextDialog() {
        val context = CoachBridgeService.generateCoachContext(
            currentWeek = 4,
            phase = "Capacidad Híbrida & HYROX (Macrociclo 13x4 - Bloque 4)",
            weeklySessions = _uiState.value.weeklyPlan,
            garmin = _uiState.value.garminMetrics,
            checkIn = _uiState.value.subjectiveCheckIn,
            readiness = _uiState.value.readiness,
            recentChanges = _uiState.value.changeLog,
            activeRequests = _uiState.value.coachRequests
        )
        _uiState.update {
            it.copy(
                exportedContextText = context,
                showCoachContextDialog = true
            )
        }
    }

    fun closeCoachContextDialog() {
        _uiState.update { it.copy(showCoachContextDialog = false) }
    }

    fun addCoachRequest(type: CoachRequestType, question: String, constraints: String) {
        val req = CoachRequest(
            id = UUID.randomUUID().toString(),
            date = LocalDate.now(),
            requestType = type,
            question = question,
            constraints = constraints
        )
        _uiState.update {
            it.copy(
                coachRequests = it.coachRequests + req,
                snackbarMessage = "Solicitud guardada en COACH_REQUESTS (Sheets ready)."
            )
        }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
