package com.example.athletic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.athletic.data.MockDataProvider
import com.example.athletic.domain.model.EquipmentType
import com.example.athletic.domain.model.Exercise
import com.example.athletic.domain.model.ExerciseRole
import com.example.athletic.ui.AthleticUiState
import com.example.athletic.ui.AthleticViewModel
import com.example.ui.theme.AthleticOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.RecoveryGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ExerciseLibraryScreen(
    state: AthleticUiState,
    viewModel: AthleticViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<ExerciseRole?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Ejercicios, 1: PRs

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BIBLIOTECA & PRs",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Inventario real • Récords personales • Sustituciones",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Ejercicios vs PRs
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val isExercises = selectedTab == 0
            val isPrs = selectedTab == 1
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isExercises) AthleticOrange else SurfaceElevated,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 0 }
            ) {
                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Ejercicios (${MockDataProvider.exercises.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isExercises) Color.White else TextSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isPrs) CyberCyan else SurfaceElevated,
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedTab = 1 }
            ) {
                Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Récords Personales (PRs)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isPrs) Color.Black else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nombre, músculo o equipo...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextMuted) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AthleticOrange,
                    unfocusedBorderColor = BorderSubtle
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Role Filters (Anchor / Rotation / Accessory)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(null to "Todos", ExerciseRole.ANCHOR to "Anchors", ExerciseRole.ROTATION to "Rotations", ExerciseRole.ACCESSORY to "Accessories").forEach { (role, label) ->
                    val isSel = selectedRoleFilter == role
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSel) AthleticOrange.copy(alpha = 0.2f) else SurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) AthleticOrange else BorderSubtle),
                        modifier = Modifier.clickable { selectedRoleFilter = role }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) AthleticOrange else TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val filteredExercises = MockDataProvider.exercises.filter { ex ->
                (selectedRoleFilter == null || ex.role == selectedRoleFilter) &&
                        (searchQuery.isBlank() || ex.name.contains(searchQuery, ignoreCase = true) || ex.targetMuscles.any { it.displayName.contains(searchQuery, ignoreCase = true) })
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredExercises) { exercise ->
                    ExerciseCard(exercise = exercise)
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        } else {
            // PRs List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(MockDataProvider.personalRecords) { pr ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(WarningAmber.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "PR",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = pr.exerciseName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = pr.date,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = pr.value,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberCyan
                                )

                                if (pr.note.isNotBlank()) {
                                    Text(
                                        text = pr.note,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = when (exercise.role) {
                        ExerciseRole.ANCHOR -> AthleticOrange.copy(alpha = 0.2f)
                        ExerciseRole.ROTATION -> CyberCyan.copy(alpha = 0.2f)
                        ExerciseRole.ACCESSORY -> TextMuted.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = exercise.role.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (exercise.role) {
                            ExerciseRole.ANCHOR -> AthleticOrange
                            ExerciseRole.ROTATION -> CyberCyan
                            ExerciseRole.ACCESSORY -> TextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = exercise.equipment.displayName,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = exercise.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Músculos: " + exercise.targetMuscles.joinToString(", ") { it.displayName },
                fontSize = 11.sp,
                color = TextSecondary
            )

            if (exercise.substitutions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sustitutos aprobados: ${exercise.substitutions.size} registrados",
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }
        }
    }
}
