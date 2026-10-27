package com.example.athletic.data

import com.example.athletic.domain.model.ChangeLogEntry
import com.example.athletic.domain.model.GarminDailyMetrics
import com.example.athletic.domain.model.ReadinessAssessment
import com.example.athletic.domain.model.SubjectiveCheckIn
import com.example.athletic.domain.model.WorkoutSession
import java.time.LocalDate

enum class CoachRequestType(val label: String) {
    NEXT_WEEK("Planificar siguiente semana"),
    PLAN_REVIEW("Revisión general de plan"),
    DELOAD_REVIEW("Consultar semana de descarga (Deload)"),
    EXERCISE_CHANGE("Sustitución o rotación de ejercicios"),
    ANNUAL_REVIEW("Ajuste de macrociclo anual"),
    INJURY_CONTEXT("Contexto de sobrecarga articular o molestia"),
    OTHER("Otra consulta estratégica")
}

data class CoachRequest(
    val id: String,
    val date: LocalDate,
    val requestType: CoachRequestType,
    val question: String,
    val constraints: String,
    val status: String = "PENDIENTE"
)

object CoachBridgeService {

    /**
     * Prepares the condensed human-readable prompt snapshot for ChatGPT (COACH_STATE).
     * The user clicks "PREPARAR CONTEXTO" and copies this directly into external ChatGPT.
     */
    fun generateCoachContext(
        currentWeek: Int = 4,
        phase: String = "Capacidad Híbrida & HYROX (Macrociclo 13x4 - Bloque 4)",
        weeklySessions: List<WorkoutSession>,
        garmin: GarminDailyMetrics?,
        checkIn: SubjectiveCheckIn?,
        readiness: ReadinessAssessment?,
        recentChanges: List<ChangeLogEntry>,
        activeRequests: List<CoachRequest>
    ): String {
        val sb = StringBuilder()
        sb.append("=== ATHLETIC OS -> CONTEXTO DE ENTRENAMIENTO PARA COACH CHATGPT ===\n")
        sb.append("Fecha de generación: ${LocalDate.now()}\n")
        sb.append("Semana anual: Sem $currentWeek | Fase: $phase\n")
        sb.append("Objetivo físico: Físico atlético Spider-Man (espalda V, hombros 3D, core denso, buena fuerza relativa y cardio híbrido)\n\n")

        sb.append("--- 1. ESTADO DE RECUPERACIÓN & FISIOLOGÍA ---\n")
        if (readiness != null) {
            sb.append("Readiness: ${readiness.level.label} (Score: ${readiness.score}/100)\n")
            sb.append("Recomendación del motor local: ${readiness.recommendation}\n")
            readiness.bulletReasons.forEach { sb.append(" • $it\n") }
        }
        if (garmin != null) {
            val hrvPct = ((garmin.hrvCurrent - garmin.hrvBaseline28d) / garmin.hrvBaseline28d) * 100f
            sb.append("Garmin HRV: %.1f ms (Baseline 28d: %.1f ms | %.1f%%)\n".format(garmin.hrvCurrent, garmin.hrvBaseline28d, hrvPct))
            sb.append("Frecuencia Reposo (RHR): ${garmin.rhrCurrent} bpm (Baseline: ${garmin.rhrBaseline28d} bpm)\n")
            sb.append("Sueño: ${garmin.sleepDurationHours} h (Calidad: ${garmin.sleepQuality.label}, Score: ${garmin.sleepScore})\n")
            sb.append("Body Battery: ${garmin.bodyBattery}/100 | Estrés: ${garmin.stressScore} | Pasos: ${garmin.steps}\n")
            sb.append("Garmin Recovery Time: ${garmin.recoveryHours} h [Informativo]\n")
        }
        if (checkIn != null) {
            sb.append("Sensación subjetiva: Energía ${checkIn.energyScore}/5 | Dolor piernas ${checkIn.legSoreness}/5 | Dolor torso ${checkIn.torsoSoreness}/5 | Motivación ${checkIn.motivationScore}/5\n")
            if (checkIn.notes.isNotBlank()) sb.append("Nota del usuario: \"${checkIn.notes}\"\n")
        }
        sb.append("\n")

        sb.append("--- 2. DISTRIBUCIÓN DE SESIONES DE LA SEMANA ---\n")
        weeklySessions.forEach { session ->
            sb.append("[${session.dayOfWeek.name.take(3)}] ${session.name} (${session.variant.label}, ${session.plannedDurationMinutes} min)\n")
            sb.append("   Cargas: Piernas ${session.loads.legs}/5 | Torso ${session.loads.chest + session.loads.back}/10 | Cardio ${session.loads.cardio}/5 | Fatiga ${session.loads.globalFatigue}/5\n")
            session.exercises.forEach { we ->
                val setsStr = we.sets.joinToString(", ") { s ->
                    val weight = s.actualWeight ?: s.targetWeight
                    val reps = s.actualReps ?: s.targetReps
                    "${reps}r@${weight.toInt()}lb"
                }
                sb.append("   - ${we.exercise.name} [${we.exercise.role.name}]: $setsStr\n")
            }
        }
        sb.append("\n")

        sb.append("--- 3. CAMBIOS RECIENTES REALIZADOS POR EL MOTOR DETERMINISTA ---\n")
        if (recentChanges.isEmpty()) {
            sb.append("Sin reorganizaciones pendientes; el plan original se mantiene intacto.\n")
        } else {
            recentChanges.takeLast(3).forEach { ch ->
                sb.append(" • Motivo: ${ch.reason}\n   Antes: ${ch.beforeState}\n   Ahora: ${ch.afterState}\n")
            }
        }
        sb.append("\n")

        sb.append("--- 4. MARCAS RECIENTES (PRs DESTACADOS) ---\n")
        MockDataProvider.personalRecords.take(4).forEach { pr ->
            sb.append(" • ${pr.exerciseName}: ${pr.value} (${pr.date})\n")
        }
        sb.append("\n")

        sb.append("--- 5. SOLICITUD PARA CHATGPT ---\n")
        if (activeRequests.isNotEmpty()) {
            activeRequests.forEach { req ->
                sb.append("Tipo: ${req.requestType.label}\nPregunta: ${req.question}\nRestricciones: ${req.constraints}\n")
            }
        } else {
            sb.append("Instrucción: Revisa mi semana, analiza la fatiga acumulada y programa los microajustes para la siguiente semana respetando los movimientos ancla.\n")
        }
        sb.append("====================================================================\n")

        return sb.toString()
    }

    /**
     * Simulates Google Sheets 16 tabs synchronization summary.
     */
    fun getSheetsTabCatalog(): List<Pair<String, String>> {
        return listOf(
            "CONFIG" to "Preferencias, unidades lb/kg, semana actual, fase y gimnasio activo",
            "ANNUAL_PLAN" to "52 semanas, 13 bloques de 4 semanas, estados y semanas de deload",
            "WEEKLY_PLAN" to "Distribución de lunes a domingo, prioridades y variantes",
            "SESSIONS" to "Registro de sesiones, duraciones, notas Notion y satisfacción",
            "SETS" to "Series detalladas, repeticiones reales, pesos por lado/mano",
            "EXERCISES" to "Biblioteca completa con patrones, roles (Anchor/Rotation) y sustitutos",
            "ACTIVITIES" to "Actividades espontáneas (ciclismo, carrera, Pilates, etc.)",
            "GARMIN_DAILY" to "Historial HRV, RHR, sueño, pasos y recovery time informativo",
            "GARMIN_ACTIVITIES" to "Tracks GPS, zonas FC Z1-Z5, potencia y desnivel",
            "PRS" to "Récords de carga, repeticiones, volumen y capacidad",
            "NOTES" to "Notas sin censura por set, ejercicio, día y semana",
            "COACH_STATE" to "Snapshot condensado listo para que ChatGPT lo lea en un solo vistazo",
            "COACH_REQUESTS" to "Solicitudes de revisión enviadas desde la app",
            "COACH_PLAN" to "Plan devuelto por ChatGPT para importación automática",
            "COACH_DECISIONS" to "Registro de decisiones tomadas por ChatGPT vs Motor Local",
            "CHANGE_LOG" to "Auditoría completa de reorganizaciones (Antes, Después, Motivo)"
        )
    }
}
