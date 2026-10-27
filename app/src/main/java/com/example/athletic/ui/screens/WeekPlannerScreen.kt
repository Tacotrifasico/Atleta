package com.example.athletic.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.athletic.domain.model.SessionPriority
import com.example.athletic.domain.model.SessionStatus
import com.example.athletic.domain.model.WorkoutSession
import com.example.athletic.ui.AthleticUiState
import com.example.athletic.ui.AthleticViewModel
import com.example.ui.theme.IosAmber
import com.example.ui.theme.IosBackground
import com.example.ui.theme.IosBorder
import com.example.ui.theme.IosCard
import com.example.ui.theme.IosCardElevated
import com.example.ui.theme.IosCyan
import com.example.ui.theme.IosGreen
import com.example.ui.theme.IosGreenSubtle
import com.example.ui.theme.IosOrange
import com.example.ui.theme.IosOrangeSubtle
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeekPlannerScreen(
    state: AthleticUiState,
    viewModel: AthleticViewModel,
    modifier: Modifier = Modifier
) {
    var swapSourceDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var swapTargetDay by remember { mutableStateOf<DayOfWeek?>(null) }
    var showSwapDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // iOS Large Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PLAN SEMANAL",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = IosTextPrimary
                )
                Text(
                    text = "Lunes a Domingo • Reorganizador Determinista",
                    fontSize = 13.sp,
                    color = IosTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = IosCardElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                modifier = Modifier.clickable { viewModel.openWhatIfDialog() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBike,
                        contentDescription = "Simulador What If",
                        tint = IosCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "What If",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("week_plan_list")
        ) {
            items(state.weeklyPlan) { session ->
                IosWeekSessionCard(
                    session = session,
                    onOpen = { viewModel.openSessionInNotion(session) },
                    onSwap = {
                        swapSourceDay = session.dayOfWeek
                        showSwapDialog = true
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // iOS Modal: Swap & Reorganize
    if (showSwapDialog && swapSourceDay != null) {
        AlertDialog(
            onDismissRequest = { showSwapDialog = false },
            title = {
                Text(
                    text = "Reorganizar Día (${swapSourceDay?.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES"))?.replaceFirstChar { it.uppercase() }})",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selecciona el día con el que deseas intercambiar esta sesión. El motor recalculará las interferencias.",
                        fontSize = 13.sp,
                        color = IosTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    DayOfWeek.values().forEach { day ->
                        val isSelected = swapTargetDay == day
                        val isSource = swapSourceDay == day
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) IosOrangeSubtle else IosCardElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) IosOrange else IosBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable(enabled = !isSource) { swapTargetDay = day }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() },
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSource) IosTextMuted else IosTextPrimary
                                )
                                if (isSource) {
                                    Text(text = "(Actual)", fontSize = 12.sp, color = IosTextMuted)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val source = swapSourceDay
                        val target = swapTargetDay
                        if (source != null && target != null) {
                            viewModel.swapWeekDays(source, target)
                        }
                        showSwapDialog = false
                        swapTargetDay = null
                    },
                    enabled = swapTargetDay != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
                ) {
                    Text("Aplicar Intercambio", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSwapDialog = false }) {
                    Text("Cancelar", color = IosTextSecondary)
                }
            },
            containerColor = IosCard
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IosWeekSessionCard(
    session: WorkoutSession,
    onOpen: () -> Unit,
    onSwap: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Day Badge & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IosOrangeSubtle
                    ) {
                        Text(
                            text = session.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("es", "ES")).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = IosOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = session.date.format(DateTimeFormatter.ofPattern("d MMMM", Locale("es", "ES"))),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = IosTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (session.status) {
                            SessionStatus.COMPLETED -> IosGreenSubtle
                            SessionStatus.REST -> IosCardElevated
                            else -> Color(0x330A84FF)
                        }
                    ) {
                        Text(
                            text = if (session.status == SessionStatus.COMPLETED) "COMPLETADA" else session.variant.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (session.status == SessionStatus.COMPLETED) IosGreen else IosCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onSwap,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Mover o intercambiar",
                            tint = IosTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = session.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = IosTextPrimary
            )

            if (session.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${session.exercises.size} ejercicios • ${session.plannedDurationMinutes} min estimados",
                    fontSize = 13.sp,
                    color = IosTextSecondary
                )
            } else if (session.status == SessionStatus.REST) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Día regenerativo / descanso",
                    fontSize = 13.sp,
                    color = IosTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Loads
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (session.loads.legs > 0) IosLoadTag("Piernas", session.loads.legs)
                if (session.loads.shoulders > 0) IosLoadTag("Hombro", session.loads.shoulders)
                if (session.loads.back > 0) IosLoadTag("Espalda", session.loads.back)
                if (session.loads.cardio > 0) IosLoadTag("Cardio", session.loads.cardio)
            }

            if (session.status != SessionStatus.COMPLETED && session.status != SessionStatus.REST) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onOpen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosCardElevated)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Abrir en Notion",
                        tint = IosCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Abrir en Notion",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary
                    )
                }
            }
        }
    }
}
