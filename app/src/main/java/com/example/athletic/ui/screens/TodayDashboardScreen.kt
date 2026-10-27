package com.example.athletic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.athletic.domain.model.ReadinessLevel
import com.example.athletic.domain.model.WeightUnit
import com.example.athletic.ui.AthleticUiState
import com.example.athletic.ui.AthleticViewModel
import com.example.athletic.ui.NavTab
import com.example.ui.theme.IosAmber
import com.example.ui.theme.IosBackground
import com.example.ui.theme.IosBorder
import com.example.ui.theme.IosCard
import com.example.ui.theme.IosCardElevated
import com.example.ui.theme.IosCyan
import com.example.ui.theme.IosDivider
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosGreenSubtle
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosOrangeSubtle
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TodayDashboardScreen(
    state: AthleticUiState,
    viewModel: AthleticViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // iOS Large Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ATHLETIC OS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = IosTextPrimary
                )
                Text(
                    text = state.todaySession?.date?.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("es", "ES")))?.replaceFirstChar { it.uppercase() } ?: "Hoy",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = IosTextSecondary
                )
            }

            // iOS Segmented Unit Selector (LB / KG / BOTH)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = IosCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    WeightUnit.values().forEach { unit ->
                        val isSelected = state.weightUnit == unit
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) IosOrange else Color.Transparent)
                                .clickable { viewModel.setWeightUnit(unit) }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else IosTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section 1: Rutina de Hoy (iOS Inset Card)
        val today = state.todaySession
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IosCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("today_workout_card")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IosOrangeSubtle
                    ) {
                        Text(
                            text = today?.variant?.label ?: "FULL (100%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Duración",
                            tint = IosTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${today?.plannedDurationMinutes ?: 60} min",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = IosTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = today?.name ?: "Descanso activo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary,
                    lineHeight = 28.sp
                )

                if (!today?.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = today!!.notes,
                        fontSize = 14.sp,
                        color = IosTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Muscle load pills
                if (today != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (today.loads.shoulders > 0) IosLoadTag("Hombro", today.loads.shoulders)
                        if (today.loads.back > 0) IosLoadTag("Espalda", today.loads.back)
                        if (today.loads.legs > 0) IosLoadTag("Piernas", today.loads.legs)
                        if (today.loads.arms > 0) IosLoadTag("Brazos", today.loads.arms)
                        if (today.loads.core > 0) IosLoadTag("Core", today.loads.core)
                        if (today.loads.cardio > 0) IosLoadTag("Cardio", today.loads.cardio)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.setTab(NavTab.WORKOUT) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("start_workout_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Abrir checklist",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ABRIR CHECKLIST NOTION",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Section 2: Readiness & Señal Fisiológica
        val readiness = state.readiness
        val (badgeColor, badgeText) = when (readiness?.level) {
            ReadinessLevel.GREEN -> Pair(IosGreen, "READINESS ÓPTIMO (VERDE)")
            ReadinessLevel.YELLOW -> Pair(IosAmber, "ATENCIÓN / AJUSTAR (AMARILLO)")
            ReadinessLevel.RED -> Pair(IosRed, "RECUPERACIÓN RECOMENDADA (ROJO)")
            null -> Pair(IosGreen, "READINESS EVALUADO")
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IosCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = badgeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }

                    Text(
                        text = "${readiness?.score ?: 80}/100",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = badgeColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = readiness?.recommendation ?: "Sistema preparado para volumen completo.",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IosTextPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                readiness?.bulletReasons?.forEach { reason ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "•", color = badgeColor, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = reason, fontSize = 13.sp, color = IosTextSecondary, lineHeight = 18.sp)
                    }
                }
            }
        }

        // Section 3: Métricas Garmin (2x2 Grid espacioso estilo Apple Health o Tarjeta de Conexión)
        val garmin = state.garminMetrics
        val isConnected = state.isGarminConnected && garmin != null

        if (isConnected && garmin != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IosCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SALUD GARMIN CONNECT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosCyan
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IosCardElevated,
                            modifier = Modifier.clickable { viewModel.openGarminConnectDialog() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = IosCyan, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Actualizar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IosCyan
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2x2 spacious grid
                    val hrvDiffPct = if (garmin.hrvBaseline28d > 0) {
                        ((garmin.hrvCurrent - garmin.hrvBaseline28d) / garmin.hrvBaseline28d) * 100f
                    } else 0f
                    val hrvSubtitle = if (hrvDiffPct >= 0) "+%.1f%% vs media 28d".format(hrvDiffPct) else "%.1f%% vs media 28d".format(hrvDiffPct)
                    val rhrDiff = garmin.rhrCurrent - garmin.rhrBaseline28d
                    val rhrSubtitle = if (rhrDiff == 0) "Estable con baseline" else if (rhrDiff > 0) "+$rhrDiff bpm vs baseline" else "$rhrDiff bpm vs baseline"

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IosHealthTile(
                            title = "HRV Nocturno",
                            value = "%.1f ms".format(garmin.hrvCurrent),
                            subtitle = hrvSubtitle,
                            color = IosCyan,
                            modifier = Modifier.weight(1f)
                        )
                        IosHealthTile(
                            title = "Freq. Reposo (RHR)",
                            value = "${garmin.rhrCurrent} bpm",
                            subtitle = rhrSubtitle,
                            color = IosGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IosHealthTile(
                            title = "Sueño",
                            value = "${garmin.sleepDurationHours} h",
                            subtitle = "${garmin.sleepScore} pts (${garmin.sleepQuality.label})",
                            color = IosTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IosHealthTile(
                            title = "Body Battery",
                            value = "${garmin.bodyBattery}",
                            subtitle = "${garmin.steps} pasos hoy",
                            color = IosOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = IosCardElevated,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Nota Garmin",
                                tint = IosTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Garmin estima ${garmin.recoveryHours}h de recuperación. Referencia informativa secundaria, nunca ley rígida.",
                                fontSize = 12.sp,
                                color = IosTextSecondary
                            )
                        }
                    }
                }
            }
        } else {
            // Unlinked / Real Data Connection Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IosCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "Garmin",
                                tint = IosCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GARMIN CONNECT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0x22FFFFFF)
                        ) {
                            Text(
                                text = "NO VINCULADO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextMuted,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sin datos inventados de pulso o HRV",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tu Readiness actual se calcula con tu check-in matutino y fatiga muscular. Vincula tu cuenta de Garmin Connect o ingresa tus métricas de hoy para calibrarlo con tu fisiología 100% real.",
                        fontSize = 13.sp,
                        color = IosTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.openGarminConnectDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("connect_garmin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = "Conectar Garmin",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Conectar Garmin o Ingresar Datos Reales",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Section 4: Acciones Modulares (iOS List Style con generoso padding táctil)
        Text(
            text = "ACCIONES MODULARES",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = IosTextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = IosCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                IosActionRow(
                    title = "+ Actividad Espontánea",
                    subtitle = "Bicicleta, MTB, Carrera, Natación, Pilates, Caminata...",
                    icon = Icons.Default.Add,
                    iconColor = IosOrange,
                    onClick = { viewModel.openSpontaneousDialog() }
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IosDivider))

                IosActionRow(
                    title = "Simulador What If",
                    subtitle = "¿Qué pasa si mañana hago 6h de bici o una carrera larga?",
                    icon = Icons.Default.DirectionsBike,
                    iconColor = IosCyan,
                    onClick = { viewModel.openWhatIfDialog() }
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IosDivider))

                IosActionRow(
                    title = "Tengo X Minutos",
                    subtitle = "15, 30, 45 o 60 min adaptando la sesión preservando ANCHORS",
                    icon = Icons.Default.Schedule,
                    iconColor = IosAmber,
                    onClick = { viewModel.openTimeVariantsDialog() }
                )

                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IosDivider))

                IosActionRow(
                    title = "¿Cómo amaneciste hoy?",
                    subtitle = "Check-in matutino subjetivo (energía, dolor, estrés)",
                    icon = Icons.Default.SelfImprovement,
                    iconColor = IosGreen,
                    onClick = { viewModel.openCheckInDialog() }
                )
            }
        }

        // Section 5: Explicabilidad del Motor Local
        if (state.changeLog.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = IosCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HISTORIAL DEL MOTOR DETERMINISTA",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosOrange
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    state.changeLog.takeLast(3).reversed().forEach { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(IosCardElevated, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = entry.reason,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IosTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Antes: ${entry.beforeState}", fontSize = 12.sp, color = IosTextSecondary)
                            Text(text = "Ahora: ${entry.afterState}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IosCyan)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun IosLoadTag(label: String, score: Int) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = IosCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label ", fontSize = 12.sp, color = IosTextSecondary)
            Text(
                text = "$score/5",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (score >= 4) IosOrange else IosCyan
            )
        }
    }
}

@Composable
fun IosHealthTile(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = IosCardElevated,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = IosTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 11.sp, color = IosTextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun IosActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = IosTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = IosTextSecondary,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Abrir",
            tint = IosTextMuted,
            modifier = Modifier.size(14.dp)
        )
    }
}
