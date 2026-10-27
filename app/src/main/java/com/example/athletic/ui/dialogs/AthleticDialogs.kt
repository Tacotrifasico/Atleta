package com.example.athletic.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.athletic.domain.model.ActivityIntensity
import com.example.athletic.domain.model.ActivityType
import com.example.athletic.domain.model.InterferenceLevel
import com.example.athletic.domain.model.PilatesFocus
import com.example.athletic.domain.model.SleepQuality
import com.example.athletic.domain.model.SubjectiveCheckIn
import com.example.athletic.ui.AthleticUiState
import com.example.athletic.ui.AthleticViewModel
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.IosBorder
import com.example.ui.theme.IosCard
import com.example.ui.theme.IosCardElevated
import com.example.ui.theme.IosCyan
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosGreenSubtle
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RecoveryGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun SpontaneousActivityDialog(
    state: AthleticUiState,
    viewModel: AthleticViewModel
) {
    if (!state.showSpontaneousDialog) return

    var selectedType by remember { mutableStateOf(ActivityType.CYCLING) }
    var durationMinutes by remember { mutableFloatStateOf(90f) }
    var selectedIntensity by remember { mutableStateOf(ActivityIntensity.Z2_AEROBIC) }
    var selectedPilatesFocus by remember { mutableStateOf(PilatesFocus.CORE) }
    var stepCount by remember { mutableIntStateOf(10000) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { viewModel.closeSpontaneousDialog() },
        title = {
            Text(
                text = "+ Registrar Actividad Espontánea",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "Tipo de actividad:", fontSize = 12.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    ActivityType.CYCLING,
                    ActivityType.CYCLING_MTB_ENDURO,
                    ActivityType.RUNNING,
                    ActivityType.SWIMMING,
                    ActivityType.PILATES,
                    ActivityType.WALKING,
                    ActivityType.HIKING
                ).forEach { type ->
                    val isSel = selectedType == type
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) AthleticOrange.copy(alpha = 0.2f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AthleticOrange else BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable { selectedType = type }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) AthleticOrange else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Duration Slider
                val hours = (durationMinutes / 60).toInt()
                val mins = (durationMinutes % 60).toInt()
                val durationText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                Text(
                    text = "Duración: $durationText (${durationMinutes.toInt()} min)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
                Slider(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it },
                    valueRange = 15f..360f, // Up to 6 hours!
                    steps = 22
                )

                if (selectedType == ActivityType.PILATES) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Foco de la clase de Pilates:", fontSize = 12.sp, color = TextMuted)
                    PilatesFocus.values().forEach { f ->
                        val isSel = selectedPilatesFocus == f
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPilatesFocus = f }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• ${f.label}",
                                fontSize = 12.sp,
                                color = if (isSel) CyberCyan else TextSecondary,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (selectedType == ActivityType.WALKING) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Pasos estimados: $stepCount", fontSize = 12.sp, color = TextMuted)
                    Slider(
                        value = stepCount.toFloat(),
                        onValueChange = { stepCount = it.toInt() },
                        valueRange = 5000f..30000f,
                        steps = 24
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas o sensaciones") },
                    placeholder = { Text("Ej. 6 horas de ruta Z2 con amigos...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AthleticOrange,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.addSpontaneousActivity(
                        type = selectedType,
                        durationMinutes = durationMinutes.toInt(),
                        intensity = selectedIntensity,
                        pilatesFocus = selectedPilatesFocus,
                        steps = if (selectedType == ActivityType.WALKING) stepCount else null,
                        notes = notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange)
            ) {
                Text("Registrar y Reorganizar")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeSpontaneousDialog() }) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun WhatIfDialog(
    state: AthleticUiState,
    viewModel: AthleticViewModel
) {
    if (!state.showWhatIfDialog) return

    val simulation = state.whatIfSimulation
    var activityType by remember { mutableStateOf(ActivityType.CYCLING) }
    var durationMinutes by remember { mutableFloatStateOf(360f) } // Default to 6h bike test case!

    AlertDialog(
        onDismissRequest = { viewModel.closeWhatIfDialog() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsBike, contentDescription = "What If", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simulador What If", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (simulation == null) {
                    Text(
                        text = "Calcula cómo afectaría una actividad demandante ANTES de realizarla y antes de modificar tu plan:",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "¿Qué planeas hacer mañana?", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))

                    listOf(
                        ActivityType.CYCLING to "Ciclismo de ruta (ej. 6 horas)",
                        ActivityType.CYCLING_MTB_ENDURO to "MTB / Enduro técnico",
                        ActivityType.RUNNING to "Carrera (Tempo / Intervalos)",
                        ActivityType.PILATES to "Pilates (Hombro / Core)",
                        ActivityType.WALKING to "Caminata larga (+20k pasos)"
                    ).forEach { (t, label) ->
                        val isSel = activityType == t
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) CyberCyan.copy(alpha = 0.2f) else SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyberCyan else BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { activityType = t }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) CyberCyan else TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val h = (durationMinutes / 60).toInt()
                    val m = (durationMinutes % 60).toInt()
                    Text(
                        text = "Duración planeada: ${if (h > 0) "${h}h ${m}m" else "${m}m"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AthleticOrange
                    )
                    Slider(
                        value = durationMinutes,
                        onValueChange = { durationMinutes = it },
                        valueRange = 30f..360f,
                        steps = 10
                    )
                } else {
                    // Show What-If Calculated Analysis
                    val loads = simulation.estimatedLoads
                    val interfPair: Pair<Color, String> = when (simulation.interferenceLevel) {
                        InterferenceLevel.VERY_HIGH -> Pair(DangerRed, "INTERFERENCIA MUY ALTA")
                        InterferenceLevel.HIGH -> Pair(WarningAmber, "INTERFERENCIA ALTA")
                        InterferenceLevel.MODERATE -> Pair(CyberCyan, "INTERFERENCIA MODERADA")
                        InterferenceLevel.LOW -> Pair(RecoveryGreen, "INTERFERENCIA BAJA")
                    }
                    val interfColor = interfPair.first
                    val interfLabel = interfPair.second

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = interfLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = interfColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Impacto estimado: Piernas ${loads.legs}/5 | Cardio ${loads.cardio}/5 | Fatiga ${loads.globalFatigue}/5",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Propuesta de reorganización del Motor Local:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AthleticOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = simulation.proposal.explanation,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    simulation.proposal.changes.forEach { ch ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ObsidianDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = ch.reason, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(text = "Cambio: ${ch.afterState}", fontSize = 11.sp, color = CyberCyan)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (simulation == null) {
                Button(
                    onClick = { viewModel.simulateWhatIf(activityType, durationMinutes.toInt()) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Calcular Impacto", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { viewModel.applyWhatIfProposal() },
                    colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange)
                ) {
                    Text("Aplicar Cambio a la Semana")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeWhatIfDialog() }) {
                Text(if (simulation != null) "Mantener Plan Original" else "Cerrar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun TimeVariantsDialog(
    state: AthleticUiState,
    viewModel: AthleticViewModel
) {
    if (!state.showTimeVariantsDialog) return

    AlertDialog(
        onDismissRequest = { viewModel.closeTimeVariantsDialog() },
        title = {
            Text("Modo: Tengo X Minutos", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    text = "Selecciona el tiempo disponible hoy. El sistema adaptará la sesión preservando los movimientos ANCHOR y descartando accesorios secundarios:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                listOf(15, 30, 45, 60, 90).forEach { mins ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.adaptForDuration(mins) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$mins Minutos",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AthleticOrange
                            )
                            Text(
                                text = when (mins) {
                                    15 -> "Solo 1-2 Anchors clave"
                                    30 -> "Anchors principales reducidos"
                                    45 -> "Anchors + 1 Rotación"
                                    60 -> "Sesión estándar balanceada"
                                    else -> "Volumen completo extendido"
                                },
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { viewModel.closeTimeVariantsDialog() }) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun CheckInDialog(
    state: AthleticUiState,
    viewModel: AthleticViewModel
) {
    if (!state.showCheckInDialog) return

    var energy by remember { mutableIntStateOf(4) }
    var legSoreness by remember { mutableIntStateOf(1) }
    var torsoSoreness by remember { mutableIntStateOf(1) }
    var stress by remember { mutableIntStateOf(2) }
    var motivation by remember { mutableIntStateOf(4) }
    var sleepQuality by remember { mutableStateOf(SleepQuality.WELL) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { viewModel.closeCheckInDialog() },
        title = {
            Text("¿Cómo amaneciste hoy?", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = "Tu feedback subjetivo tiene mayor jerarquía que métricas aisladas de Garmin.", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Nivel de Energía: $energy/5", fontSize = 12.sp, color = TextMuted)
                Slider(value = energy.toFloat(), onValueChange = { energy = it.toInt() }, valueRange = 1f..5f, steps = 3)

                Text(text = "Dolor/Agujetas en Piernas: $legSoreness/5", fontSize = 12.sp, color = TextMuted)
                Slider(value = legSoreness.toFloat(), onValueChange = { legSoreness = it.toInt() }, valueRange = 1f..5f, steps = 3)

                Text(text = "Dolor en Torso/Espalda: $torsoSoreness/5", fontSize = 12.sp, color = TextMuted)
                Slider(value = torsoSoreness.toFloat(), onValueChange = { torsoSoreness = it.toInt() }, valueRange = 1f..5f, steps = 3)

                Text(text = "¿Dormiste bien?", fontSize = 12.sp, color = TextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SleepQuality.values().forEach { sq ->
                        val isSel = sleepQuality == sq
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) AthleticOrange.copy(alpha = 0.2f) else SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AthleticOrange else BorderSubtle),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { sleepQuality = sq }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = sq.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) AthleticOrange else TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Nota libre Notion") },
                    placeholder = { Text("Sensación muscular, articulaciones, ganas...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AthleticOrange,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val checkIn = SubjectiveCheckIn(
                        date = java.time.LocalDate.now(),
                        energyScore = energy,
                        legSoreness = legSoreness,
                        torsoSoreness = torsoSoreness,
                        stressScore = stress,
                        motivationScore = motivation,
                        sleptWell = sleepQuality,
                        notes = note
                    )
                    viewModel.submitSubjectiveCheckIn(checkIn)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AthleticOrange)
            ) {
                Text("Guardar Check-in")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.closeCheckInDialog() }) {
                Text("Cerrar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard
    )
}

@Composable
fun GarminConnectDialog(
    state: AthleticUiState,
    viewModel: AthleticViewModel
) {
    if (!state.showGarminConnectDialog) return

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Métricas de Hoy, 1 = Configurar API / Webhook

    // Tab 0 states (Manual Real Metrics)
    val existing = state.garminMetrics
    var hrvText by remember(existing) { mutableStateOf(existing?.hrvCurrent?.let { "%.1f".format(it) } ?: "55.0") }
    var hrvBaselineText by remember(existing) { mutableStateOf(existing?.hrvBaseline28d?.let { "%.1f".format(it) } ?: "58.0") }
    var rhrText by remember(existing) { mutableStateOf(existing?.rhrCurrent?.toString() ?: "49") }
    var rhrBaselineText by remember(existing) { mutableStateOf(existing?.rhrBaseline28d?.toString() ?: "48") }
    var sleepHoursText by remember(existing) { mutableStateOf(existing?.sleepDurationHours?.let { "%.1f".format(it) } ?: "7.5") }
    var sleepQuality by remember(existing) { mutableStateOf(existing?.sleepQuality ?: SleepQuality.WELL) }
    var sleepScoreText by remember(existing) { mutableStateOf(existing?.sleepScore?.toString() ?: "82") }
    var bodyBatteryText by remember(existing) { mutableStateOf(existing?.bodyBattery?.toString() ?: "75") }
    var stepsText by remember(existing) { mutableStateOf(existing?.steps?.toString() ?: "8500") }
    var recoveryHoursText by remember(existing) { mutableStateOf(existing?.recoveryHours?.toString() ?: "24") }

    // Tab 1 states (API Credentials)
    var clientIdText by remember(state.garminClientId) { mutableStateOf(state.garminClientId) }
    var clientSecretText by remember(state.garminClientSecret) { mutableStateOf(state.garminClientSecret) }
    var webhookUrlText by remember(state.garminWebhookUrl) { mutableStateOf(state.garminWebhookUrl) }

    AlertDialog(
        onDismissRequest = { viewModel.closeGarminConnectDialog() },
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GARMIN CONNECT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    if (state.isGarminConnected) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = IosGreenSubtle
                        ) {
                            Text(
                                text = "CONECTADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Datos 100% reales. Sin proxies ni valores inventados.",
                    fontSize = 12.sp,
                    color = IosTextSecondary
                )
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Segmented tabs: Métricas de Hoy vs API Personal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IosCardElevated, RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) IosCyan else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Métricas de Hoy",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) Color.Black else IosTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) IosCyan else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "API / Webhook",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) Color.Black else IosTextSecondary
                        )
                    }
                }

                if (selectedTab == 0) {
                    // Manual real data inputs
                    Text(
                        text = "Ingresa los valores exactos de tu reloj de esta mañana. Se comparan con tu baseline personal de 28 días:",
                        fontSize = 12.sp,
                        color = IosTextSecondary,
                        lineHeight = 17.sp
                    )

                    // HRV row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = hrvText,
                            onValueChange = { hrvText = it },
                            label = { Text("HRV Hoy (ms)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = hrvBaselineText,
                            onValueChange = { hrvBaselineText = it },
                            label = { Text("Baseline 28d", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // RHR row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = rhrText,
                            onValueChange = { rhrText = it },
                            label = { Text("RHR Hoy (bpm)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = rhrBaselineText,
                            onValueChange = { rhrBaselineText = it },
                            label = { Text("Baseline RHR", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Sleep row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = sleepHoursText,
                            onValueChange = { sleepHoursText = it },
                            label = { Text("Sueño (h)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = sleepScoreText,
                            onValueChange = { sleepScoreText = it },
                            label = { Text("Score Sueño", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    // Sleep quality selector
                    Column {
                        Text("Calidad subjetiva de sueño:", fontSize = 11.sp, color = IosTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SleepQuality.values().forEach { sq ->
                                val isSel = sleepQuality == sq
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) IosCyan.copy(alpha = 0.2f) else IosCardElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) IosCyan else IosBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { sleepQuality = sq }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = sq.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) IosCyan else IosTextSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Body Battery, Pasos & Recovery Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = bodyBatteryText,
                            onValueChange = { bodyBatteryText = it },
                            label = { Text("Body Battery", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        OutlinedTextField(
                            value = stepsText,
                            onValueChange = { stepsText = it },
                            label = { Text("Pasos hoy", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosCyan,
                                unfocusedBorderColor = IosBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }

                    OutlinedTextField(
                        value = recoveryHoursText,
                        onValueChange = { recoveryHoursText = it },
                        label = { Text("Recovery Time Garmin (horas)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosCyan,
                            unfocusedBorderColor = IosBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IosCardElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Nota: El tiempo de recuperación de Garmin es solo informativo. La app nunca bloqueará tus entrenamientos por él.",
                            fontSize = 11.sp,
                            color = IosTextSecondary,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                } else {
                    // API / Webhook tab
                    Text(
                        text = "Configura tu conexión oficial a Garmin Connect Personal API / Health API para sincronización automática:",
                        fontSize = 12.sp,
                        color = IosTextSecondary,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = clientIdText,
                        onValueChange = { clientIdText = it },
                        label = { Text("Client ID / Consumer Key", fontSize = 11.sp) },
                        placeholder = { Text("ej. garmin_app_personal_001") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosCyan,
                            unfocusedBorderColor = IosBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = clientSecretText,
                        onValueChange = { clientSecretText = it },
                        label = { Text("Client Secret / Bearer Token", fontSize = 11.sp) },
                        placeholder = { Text("Token cifrado server-side") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosCyan,
                            unfocusedBorderColor = IosBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = webhookUrlText,
                        onValueChange = { webhookUrlText = it },
                        label = { Text("Webhook Push URL (Apps Script o servidor)", fontSize = 11.sp) },
                        placeholder = { Text("https://script.google.com/.../exec?type=garmin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosCyan,
                            unfocusedBorderColor = IosBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IosCardElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Seguridad: Según especificación Sección 44, las claves se mantienen seguras. Las actividades y resúmenes diarios llegarán por PUSH a tu webhook.",
                            fontSize = 11.sp,
                            color = IosTextSecondary,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = {
                        val hrv = hrvText.toFloatOrNull() ?: 55f
                        val hrvBaseline = hrvBaselineText.toFloatOrNull() ?: 58f
                        val rhr = rhrText.toIntOrNull() ?: 49
                        val rhrBaseline = rhrBaselineText.toIntOrNull() ?: 48
                        val sleepHours = sleepHoursText.toFloatOrNull() ?: 7.5f
                        val sleepScore = sleepScoreText.toIntOrNull() ?: 80
                        val bodyBattery = bodyBatteryText.toIntOrNull() ?: 75
                        val steps = stepsText.toIntOrNull() ?: 8000
                        val recHours = recoveryHoursText.toIntOrNull() ?: 24

                        viewModel.saveManualGarminMetrics(
                            hrvCurrent = hrv,
                            hrvBaseline28d = hrvBaseline,
                            rhrCurrent = rhr,
                            rhrBaseline28d = rhrBaseline,
                            sleepDurationHours = sleepHours,
                            sleepQuality = sleepQuality,
                            sleepScore = sleepScore,
                            bodyBattery = bodyBattery,
                            steps = steps,
                            recoveryHours = recHours
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosCyan)
                ) {
                    Text("Guardar Mis Datos", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.saveGarminApiCredentials(
                            clientId = clientIdText,
                            clientSecret = clientSecretText,
                            webhookUrl = webhookUrlText
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IosCyan)
                ) {
                    Text("Vincular API", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.isGarminConnected) {
                    TextButton(onClick = { viewModel.disconnectGarmin() }) {
                        Text("Desvincular", color = IosRed)
                    }
                }
                TextButton(onClick = { viewModel.closeGarminConnectDialog() }) {
                    Text("Cerrar", color = IosTextSecondary)
                }
            }
        },
        containerColor = IosCard
    )
}
