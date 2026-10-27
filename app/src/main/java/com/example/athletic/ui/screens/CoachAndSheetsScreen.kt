package com.example.athletic.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.athletic.data.CoachBridgeService
import com.example.athletic.data.CoachRequestType
import com.example.athletic.data.MockDataProvider
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
import com.example.ui.theme.IosRed
import com.example.ui.theme.IosTextMuted
import com.example.ui.theme.IosTextPrimary
import com.example.ui.theme.IosTextSecondary

@Composable
fun CoachAndSheetsScreen(
    state: AthleticUiState,
    viewModel: AthleticViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRequestDialog by remember { mutableStateOf(false) }
    var showSheetsConfig by remember { mutableStateOf(false) }
    var showGarminConfig by remember { mutableStateOf(false) }

    var editSheetId by remember(state.spreadsheetId) { mutableStateOf(state.spreadsheetId) }
    var editAppsScriptUrl by remember(state.appsScriptUrl) { mutableStateOf(state.appsScriptUrl) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IosBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // iOS Large Title
        Column {
            Text(
                text = "CONEXIÓN & SHEETS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = IosTextPrimary
            )
            Text(
                text = "Sheets almacena • Garmin observa • ChatGPT programa",
                fontSize = 13.sp,
                color = IosTextSecondary
            )
        }

        // Section 1: GOOGLE SHEETS
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
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Sheets",
                            tint = IosGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "GOOGLE SHEETS (16 PESTAÑAS)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosGreen
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = IosGreenSubtle
                    ) {
                        Text(
                            text = "CONECTADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.lastSheetsSync,
                    fontSize = 13.sp,
                    color = IosTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Full-width spacious buttons
                Button(
                    onClick = { viewModel.syncGoogleSheets() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosGreen)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sincronizar Sheets Ahora", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showSheetsConfig = !showSheetsConfig },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IosTextPrimary)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurar", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Configuración de Spreadsheet ID y Webhook", fontSize = 13.sp)
                }

                // Collapsible Sheets Configuration
                AnimatedVisibility(visible = showSheetsConfig) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(IosCardElevated, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "¿Cómo conectar tu hoja de Google Sheets?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosOrange
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Abre Google Sheets y crea tu hoja 'Athletic OS'.\n" +
                                    "2. En la URL (docs.google.com/spreadsheets/d/ID/edit), copia el ID y pégalo abajo.\n" +
                                    "3. En la hoja, abre Extensiones → Apps Script e implementa como aplicación web (Web App).\n" +
                                    "4. Pega la URL del Apps Script abajo para sincronización bidireccional.",
                            fontSize = 12.sp,
                            color = IosTextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = editSheetId,
                            onValueChange = { editSheetId = it },
                            label = { Text("Google Spreadsheet ID", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = IosTextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosOrange,
                                unfocusedBorderColor = IosBorder,
                                focusedContainerColor = IosCard,
                                unfocusedContainerColor = IosCard
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editAppsScriptUrl,
                            onValueChange = { editAppsScriptUrl = it },
                            label = { Text("URL de Webhook (Google Apps Script)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = IosTextPrimary),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IosOrange,
                                unfocusedBorderColor = IosBorder,
                                focusedContainerColor = IosCard,
                                unfocusedContainerColor = IosCard
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                viewModel.updateSheetsConfig(editSheetId, editAppsScriptUrl)
                                showSheetsConfig = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IosOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text("Guardar Configuración", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 2: GARMIN CONNECT PERSONAL API
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "GARMIN PERSONAL API",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosCyan
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (state.isGarminConnected) IosGreenSubtle else Color(0x22FFFFFF)
                    ) {
                        Text(
                            text = if (state.isGarminConnected) "CONECTADO" else "DESCONECTADO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (state.isGarminConnected) IosGreen else IosTextMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.lastGarminSync,
                    fontSize = 13.sp,
                    color = IosTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isGarminConnected) {
                    Button(
                        onClick = { viewModel.syncGarminMetrics() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosCyan)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sincronizar Garmin Ahora", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.openGarminConnectDialog() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosTextPrimary)
                        ) {
                            Text("Calibrar Datos", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.disconnectGarmin() },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosRed)
                        ) {
                            Text("Desconectar", fontSize = 12.sp, color = IosRed)
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.openGarminConnectDialog() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IosCyan)
                    ) {
                        Icon(Icons.Default.Watch, contentDescription = "Conectar", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vincular o Ingresar Datos Reales", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showGarminConfig = !showGarminConfig },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IosTextPrimary)
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = "Detalles", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Detalles de Conexión y API", fontSize = 13.sp)
                }

                AnimatedVisibility(visible = showGarminConfig) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .background(IosCardElevated, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "¿Cómo se conectan los datos de Garmin?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Garmin Connect envía Webhooks automáticos (Daily Health Summary y Activities).\n" +
                                    "• Athletic OS procesa: HRV, Frecuencia Cardíaca en Reposo (RHR), Sueño y Pasos.\n" +
                                    "• Regla crítica: El 'Recovery Time' de Garmin se muestra solo como dato informativo; nunca bloquea tus sesiones.\n" +
                                    "• Tu check-in matutino tiene mayor peso que el sensor si te sientes recuperado.",
                            fontSize = 12.sp,
                            color = IosTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Section 3: CHATGPT BRIDGE
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = IosCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "CHATGPT: CEREBRO DE PROGRAMACIÓN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = IosOrange
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Genera el snapshot listo para copiar a ChatGPT. Analiza tu fatiga y programa tu siguiente semana respetando los anchors:",
                    fontSize = 13.sp,
                    color = IosTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.openCoachContextDialog() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("prepare_context_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = "Contexto", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PREPARAR CONTEXTO PARA CHATGPT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showRequestDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IosCyan)
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = "Solicitud", tint = IosCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Solicitar Revisión a ChatGPT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Macrociclo Anual
        Text(
            text = "MACROCICLO ANUAL (13 BLOQUES × 4 SEMANAS)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = IosTextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )

        MockDataProvider.macroBlocks.forEach { block ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (block.isCurrent) IosOrangeSubtle else IosCard,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (block.isCurrent) IosOrange else IosBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Bloque ${block.blockNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (block.isCurrent) IosOrange else IosTextSecondary
                            )
                            if (block.isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = IosOrange
                                ) {
                                    Text(
                                        text = "ACTUAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = block.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = IosTextPrimary
                        )
                        Text(
                            text = block.focus,
                            fontSize = 12.sp,
                            color = IosTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "${block.durationWeeks} semanas",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = IosTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Modal: Prepared Context for External ChatGPT
    if (state.showCoachContextDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeCoachContextDialog() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contexto para ChatGPT",
                        fontWeight = FontWeight.Bold,
                        color = IosTextPrimary,
                        fontSize = 19.sp
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Athletic OS Context", state.exportedContextText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar contexto",
                            tint = IosCyan
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Pega este resumen directamente en ChatGPT para que programe tu siguiente semana:",
                        fontSize = 13.sp,
                        color = IosTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = IosBackground,
                        border = androidx.compose.foundation.BorderStroke(1.dp, IosBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.exportedContextText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = IosTextPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Athletic OS Context", state.exportedContextText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copiado al portapapeles", Toast.LENGTH_SHORT).show()
                        viewModel.closeCoachContextDialog()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
                ) {
                    Text("Copiar al Portapapeles y Salir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeCoachContextDialog() }) {
                    Text("Cerrar", color = IosTextSecondary)
                }
            },
            containerColor = IosCard
        )
    }

    // Modal: Submit Request to Coach
    if (showRequestDialog) {
        var selectedType by remember { mutableStateOf(CoachRequestType.NEXT_WEEK) }
        var questionText by remember { mutableStateOf("") }
        var constraintsText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = {
                Text(
                    text = "Solicitar Revisión a ChatGPT",
                    fontWeight = FontWeight.Bold,
                    color = IosTextPrimary,
                    fontSize = 19.sp
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(text = "Tipo de consulta:", fontSize = 13.sp, color = IosTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))

                    CoachRequestType.values().forEach { t ->
                        val isSel = selectedType == t
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) IosOrangeSubtle else IosCardElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) IosOrange else IosBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { selectedType = t }
                                .padding(10.dp)
                        ) {
                            Text(
                                text = t.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) IosOrange else IosTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Pregunta u objetivo específico") },
                        placeholder = { Text("Ej. Incrementar volumen en hombro...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosOrange,
                            unfocusedBorderColor = IosBorder,
                            focusedContainerColor = IosCardElevated,
                            unfocusedContainerColor = IosCardElevated
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = constraintsText,
                        onValueChange = { constraintsText = it },
                        label = { Text("Restricciones (tiempo, viaje, molestias)") },
                        placeholder = { Text("Ej. Solo tengo 45 min el viernes...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IosOrange,
                            unfocusedBorderColor = IosBorder,
                            focusedContainerColor = IosCardElevated,
                            unfocusedContainerColor = IosCardElevated
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addCoachRequest(selectedType, questionText, constraintsText)
                        showRequestDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosOrange)
                ) {
                    Text("Registrar Solicitud", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Cancelar", color = IosTextSecondary)
                }
            },
            containerColor = IosCard
        )
    }
}
