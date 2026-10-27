package com.example.athletic.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.athletic.domain.model.DifficultyLevel
import com.example.athletic.domain.model.ExerciseRole
import com.example.athletic.domain.model.RepeatPreference
import com.example.athletic.domain.model.SessionFeedback
import com.example.athletic.domain.model.SetStatus
import com.example.athletic.domain.model.WeightUnit
import com.example.athletic.domain.model.WorkoutExercise
import com.example.athletic.domain.model.WorkoutSession
import com.example.athletic.ui.AthleticUiState
import com.example.athletic.ui.AthleticViewModel
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
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary

@Composable
fun TrainingModeScreen(
    state: AthleticUiState,
    viewModel: AthleticViewModel,
    modifier: Modifier = Modifier
) {
    val workout = state.activeWorkout ?: state.todaySession

    if (workout == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(IosBackground)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay entrenamiento programado.",
                color = IosTextSecondary,
                fontSize = 16.sp
            )
        }
        return
    }

    // Calculate sets count
    val allSets = workout.exercises.flatMap { it.sets }
    val completedCount = allSets.count { it.status == SetStatus.COMPLETED || it.status == SetStatus.COMPLETED_UNLOGGED }
    val totalCount = allSets.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // iOS Large Header Section
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RUTINA NOTION",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = IosOrange
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = IosCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder)
                    ) {
                        Text(
                            text = workout.variant.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IosCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = workout.name,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary,
                    lineHeight = 32.sp
                )

                if (workout.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = workout.notes,
                        fontSize = 14.sp,
                        color = IosTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // iOS Progress Inset Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = IosCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso de sets",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = IosTextSecondary
                            )
                            Text(
                                text = "$completedCount de $totalCount completados",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (progress >= 1f) IosGreen else IosOrange
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (progress >= 1f) IosGreen else IosOrange,
                            trackColor = IosCardElevated
                        )
                    }
                }
            }
        }

        // Exercise Inset Grouped Blocks
        itemsIndexed(workout.exercises) { exIndex, workoutEx ->
            IosNotionExerciseBlock(
                exerciseIndex = exIndex,
                workoutExercise = workoutEx,
                unit = state.weightUnit,
                onToggleSet = { setIndex ->
                    viewModel.toggleSetStatus(exIndex, setIndex)
                },
                onUpdateReps = { setIndex, delta ->
                    viewModel.updateSetReps(exIndex, setIndex, delta)
                },
                onUpdateWeight = { setIndex, newWeight ->
                    viewModel.updateSetWeight(exIndex, setIndex, newWeight)
                },
                onSaveNote = { note ->
                    viewModel.updateExerciseNote(exIndex, note)
                },
                onMarkAll = { completed ->
                    viewModel.markAllSetsInExercise(exIndex, completed)
                },
                onOccupied = {
                    viewModel.handleMachineOccupied(exIndex)
                }
            )
        }

        // Full-width Bottom Save Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.openFinishDialog() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("finish_workout_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GUARDAR Y SINCRONIZAR EN SHEETS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Occupied Machine dialog
    val occupiedDialog = state.machineOccupiedDialog
    if (occupiedDialog != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeMachineOccupiedDialog() },
            title = {
                Text(
                    text = "⚠️ Máquina Ocupada",
                    fontWeight = FontWeight.Bold,
                    color = IosAmber
                )
            },
            text = {
                Column {
                    Text(
                        text = occupiedDialog.explanation,
                        fontSize = 14.sp,
                        color = IosTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (occupiedDialog.substituteExercise != null) {
                        val sub = occupiedDialog.substituteExercise
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IosCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, IosOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.substituteExercise(0, sub) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Sustitución autorizada:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IosOrange
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = sub.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IosTextPrimary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeMachineOccupiedDialog() }) {
                    Text("Cerrar", color = IosCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = IosCard
        )
    }

    // Finish Workout Dialog
    if (state.showFinishDialog) {
        var feltTrained by remember { mutableIntStateOf(4) }
        var funScore by remember { mutableIntStateOf(5) }
        var energyRemaining by remember { mutableIntStateOf(50) }
        var repeatPref by remember { mutableStateOf(RepeatPreference.YES) }
        var markUnlogged by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { viewModel.closeFinishDialog() },
            title = {
                Text(
                    text = "Finalizar y Resumen",
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IosCardElevated,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { markUnlogged = !markUnlogged }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = markUnlogged,
                                onCheckedChange = { markUnlogged = it },
                                colors = CheckboxDefaults.colors(checkedColor = IosOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Marcar sets no registrados como completados (Default)",
                                fontSize = 13.sp,
                                color = IosTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "¿Sentiste que entrenaste? ($feltTrained / 5)", fontSize = 13.sp, color = IosTextSecondary)
                    Slider(
                        value = feltTrained.toFloat(),
                        onValueChange = { feltTrained = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "¿Qué tan divertido fue? ($funScore / 5)", fontSize = 13.sp, color = IosTextSecondary)
                    Slider(
                        value = funScore.toFloat(),
                        onValueChange = { funScore = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val feedback = SessionFeedback(
                            feltTrainedScore = feltTrained,
                            funScore = funScore,
                            energyRemainingPct = energyRemaining,
                            repeatPreference = repeatPref
                        )
                        viewModel.finishWorkout(feedback, markUnlogged)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
                ) {
                    Text("Guardar en Sheets", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeFinishDialog() }) {
                    Text("Cancelar", color = IosTextSecondary)
                }
            },
            containerColor = IosCard
        )
    }
}

@Composable
fun IosNotionExerciseBlock(
    exerciseIndex: Int,
    workoutExercise: WorkoutExercise,
    unit: WeightUnit,
    onToggleSet: (Int) -> Unit,
    onUpdateReps: (Int, Int) -> Unit,
    onUpdateWeight: (Int, Float) -> Unit,
    onSaveNote: (String) -> Unit,
    onMarkAll: (Boolean) -> Unit,
    onOccupied: () -> Unit
) {
    val exercise = workoutExercise.exercise
    var isExpanded by remember { mutableStateOf(true) }
    var noteText by remember(workoutExercise.notes) { mutableStateOf(workoutExercise.notes) }
    var isEditingNote by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IosCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val allCompleted = workoutExercise.sets.all { it.status == SetStatus.COMPLETED || it.status == SetStatus.COMPLETED_UNLOGGED }

            // Header: Notion Exercise Checkbox + Title + Role Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exercise-level Checkbox: marks or clears all sets in 1 tap
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onMarkAll(!allCompleted) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (allCompleted) "Desmarcar ejercicio" else "Completar ejercicio",
                            tint = if (allCompleted) IosGreen else IosTextMuted,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isExpanded = !isExpanded },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = exercise.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allCompleted) IosGreen else IosTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expandir o colapsar",
                            tint = IosTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (exercise.role) {
                        ExerciseRole.ANCHOR -> IosOrangeSubtle
                        ExerciseRole.ROTATION -> Color(0x330A84FF)
                        ExerciseRole.ACCESSORY -> IosCardElevated
                    }
                ) {
                    Text(
                        text = exercise.role.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (exercise.role) {
                            ExerciseRole.ANCHOR -> IosOrange
                            ExerciseRole.ROTATION -> IosCyan
                            ExerciseRole.ACCESSORY -> IosTextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle info: Equipment + Occupied machine button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 46.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${exercise.equipment.displayName} • ${workoutExercise.sets.size} sets",
                    fontSize = 13.sp,
                    color = IosTextSecondary
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x33FF9F0A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IosAmber),
                    modifier = Modifier.clickable { onOccupied() }
                ) {
                    Text(
                        text = "Máquina Ocupada ⚠️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IosAmber,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // Set Checklist Rows - iOS Inset List Style with generous height
                    workoutExercise.sets.forEachIndexed { setIdx, loggedSet ->
                        val isChecked = loggedSet.status == SetStatus.COMPLETED || loggedSet.status == SetStatus.COMPLETED_UNLOGGED
                        val reps = loggedSet.actualReps ?: loggedSet.targetReps
                        val weight = loggedSet.actualWeight ?: loggedSet.targetWeight
                        val kg = weight * 0.45359237f
                        val targetKg = loggedSet.targetWeight * 0.45359237f

                        val targetWeightStr = when (unit) {
                            WeightUnit.LB -> "${loggedSet.targetWeight.toInt()} lb"
                            WeightUnit.KG -> "%.1f kg".format(targetKg)
                            WeightUnit.BOTH -> "${loggedSet.targetWeight.toInt()} lb / %.1f kg".format(targetKg)
                        }
                        val actualWeightStr = when (unit) {
                            WeightUnit.LB -> "${weight.toInt()} lb"
                            WeightUnit.KG -> "%.1f kg".format(kg)
                            WeightUnit.BOTH -> "${weight.toInt()} lb / %.1f kg".format(kg)
                        }
                        val extraWeightNote = if (exercise.isPerHand) " por mano" else if (exercise.isPlateLoaded) " en discos" else ""

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isChecked) IosGreenSubtle else IosCardElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isChecked) IosGreen else IosBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clickable { onToggleSet(setIdx) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: Clean iOS Checkbox & Label
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .clickable { onToggleSet(setIdx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = if (isChecked) "Desmarcar set" else "Completar set",
                                            tint = if (isChecked) IosGreen else IosTextMuted,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        if (isChecked) {
                                            Text(
                                                text = "Set ${setIdx + 1} • ${loggedSet.actualReps} reps",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IosGreen
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$actualWeightStr$extraWeightNote",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = IosTextPrimary
                                            )
                                        } else {
                                            Text(
                                                text = "Set ${setIdx + 1} • En blanco",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = IosTextSecondary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Objetivo: ${loggedSet.targetReps} reps @ $targetWeightStr$extraWeightNote",
                                                fontSize = 13.sp,
                                                color = IosTextMuted
                                            )
                                        }
                                    }
                                }

                                // Right: iOS Spacious Segmented Stepper [ - ] reps [ + ]
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier
                                        .background(IosCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, IosBorder, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable { onUpdateReps(setIdx, -1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Menos reps",
                                            tint = if (isChecked) IosGreen else IosTextPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Text(
                                        text = if (isChecked) "${loggedSet.actualReps}" else "—",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isChecked) IosGreen else IosTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .clickable { onUpdateReps(setIdx, 1) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Más reps",
                                            tint = if (isChecked) IosGreen else IosTextPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notion Inline Note
                    if (!isEditingNote && noteText.isBlank()) {
                        Text(
                            text = "+ Añadir nota Notion sin filtro...",
                            fontSize = 13.sp,
                            color = IosTextSecondary,
                            modifier = Modifier
                                .clickable { isEditingNote = true }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                        )
                    } else if (isEditingNote) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text("Ej. Me costó la última rep pero subió sólido...", fontSize = 13.sp) },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = IosTextPrimary),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IosOrange,
                                    unfocusedBorderColor = IosBorder,
                                    focusedContainerColor = IosCardElevated,
                                    unfocusedContainerColor = IosCardElevated
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    onSaveNote(noteText)
                                    isEditingNote = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IosCyan)
                            ) {
                                Text("OK", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IosCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isEditingNote = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "💬 \"$noteText\"",
                                fontSize = 13.sp,
                                color = IosTextSecondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
