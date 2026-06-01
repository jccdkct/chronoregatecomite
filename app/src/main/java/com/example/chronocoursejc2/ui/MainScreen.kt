package com.example.chronocoursejc2.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chronocoursejc2.R
import com.example.chronocoursejc2.ui.theme.Chronocoursejc2Theme
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTime by viewModel.currentTime.collectAsStateWithLifecycle()
    val batteryPercentage by viewModel.batteryPercentage.collectAsStateWithLifecycle()
    val arrivals by viewModel.arrivals.collectAsStateWithLifecycle()
    val raceState by viewModel.raceState.collectAsStateWithLifecycle()
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()
    val elapsedTime by viewModel.elapsedTime.collectAsStateWithLifecycle()
    val showProcedureDialog by viewModel.showProcedureDialog.collectAsStateWithLifecycle()
    val showPostRaceDialog by viewModel.showPostRaceDialog.collectAsStateWithLifecycle()
    val selectedProcedure by viewModel.selectedProcedure.collectAsStateWithLifecycle()
    val plannedDepartureTimeLabel by viewModel.plannedDepartureTimeLabel.collectAsStateWithLifecycle()
    val lastSavedFileContent by viewModel.lastSavedFileContent.collectAsStateWithLifecycle()

    var showStopDialog by remember { mutableStateOf(false) }
    var showTextViewer by remember { mutableStateOf(false) }

    val customDeepTeal = Color(0xFF003333)
    val customDarkRed = Color(0xFF771010)
    val customGray = Color(0xFF909090)

    if (showProcedureDialog) {
        ProcedureSelectionDialog(
            onProcedureSelected = { viewModel.selectProcedure(it) }
        )
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Arrêter la course") },
            text = { Text("Voulez-vous vraiment arrêter et sauvegarder ?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopAndSave()
                        showStopDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = customDarkRed, contentColor = Color.White)
                ) {
                    Text("arrêt+sauv")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showStopDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4EDDA), contentColor = Color(0xFF155724))
                ) {
                    Text("continuer")
                }
            }
        )
    }

    if (showPostRaceDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Course terminée") },
            text = {
                Column {
                    Text("Listing sauvegardé dans téléchargement.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = customGray, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ouvrir le dossier", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { showTextViewer = true },
                            colors = ButtonDefaults.buttonColors(containerColor = customGray, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ouvrir ici", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Quitter ou relancer l'application ?", fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { (context as? Activity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = customDarkRed, contentColor = Color.White),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Quitter")
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.resetRace() },
                    colors = ButtonDefaults.buttonColors(containerColor = customDeepTeal, contentColor = Color.White),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Relancer")
                }
            }
        )
    }

    if (showTextViewer) {
        Dialog(
            onDismissRequest = { showTextViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { showTextViewer = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Fermer")
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = lastSavedFileContent,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(currentTime, batteryPercentage)
        },
        bottomBar = {
            BottomButtons(
                raceState = raceState,
                onArrivalClick = { viewModel.recordArrival() },
                onStartStopClick = {
                    if (raceState == RaceState.READY || raceState == RaceState.IDLE) {
                        viewModel.triggerStartAction()
                    } else if (raceState == RaceState.RUNNING || raceState == RaceState.COUNTDOWN) {
                        showStopDialog = true
                    }
                },
                deepTealColor = customDeepTeal,
                darkRedColor = customDarkRed
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimingDisplay(raceState, remainingTime, elapsedTime)
            
            if (selectedProcedure != null && (raceState == RaceState.COUNTDOWN || raceState == RaceState.RUNNING)) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val departureInfo = buildAnnotatedString {
                        append("Course dont le départ est à ")
                        withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)) {
                            append(plannedDepartureTimeLabel)
                        }
                    }
                    Text(
                        text = departureInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "(type ${selectedProcedure?.label})",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            ArrivalList(arrivals)
        }
    }
}

@Composable
fun ProcedureSelectionDialog(onProcedureSelected: (Procedure) -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Choix de la procédure de départ",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onProcedureSelected(Procedure.PROC_6510) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "6 5 1 0",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { onProcedureSelected(Procedure.PROC_3210) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "3 2 1 0",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Button(
                            onClick = { onProcedureSelected(Procedure.NONE) },
                            modifier = Modifier
                                .fillMaxWidth(0.48f)
                                .fillMaxHeight(0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "Sans compte à rebours",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 28.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopBar(currentTime: String, batteryPercentage: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$batteryPercentage%",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Rounded.BatteryFull, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_custom),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
fun TimingDisplay(raceState: RaceState, remainingTime: Long, elapsedTime: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val (mainText, subText) = when (raceState) {
                RaceState.IDLE -> "00 h 00 m 00 s" to "En attente"
                RaceState.READY -> formatTime(remainingTime) to "Prêt au départ"
                RaceState.COUNTDOWN -> formatTime(remainingTime) to "avant le départ"
                RaceState.RUNNING -> formatTime(elapsedTime) to "depuis le départ"
                RaceState.STOPPED -> formatTime(elapsedTime) to "course terminée"
            }
            
            Text(
                text = mainText,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = subText,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d h %02d m %02d s", h, m, s)
}

@Composable
fun ArrivalList(arrivals: List<Arrival>) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(arrivals.size) {
        if (arrivals.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Rang", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
            Text("Durée", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
            Text("Heure", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
        }
        HorizontalDivider()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(arrivals) { arrival ->
                ArrivalRow(arrival)
            }
        }
    }
}

@Composable
fun ArrivalRow(arrival: Arrival) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = String.format(Locale.getDefault(), "%03d", arrival.rank), modifier = Modifier.weight(0.4f))
        Text(text = arrival.duration, modifier = Modifier.weight(1.3f))
        Text(text = arrival.arrivalTime, modifier = Modifier.weight(1.3f))
    }
}

@Composable
fun BottomButtons(
    raceState: RaceState,
    onArrivalClick: () -> Unit,
    onStartStopClick: () -> Unit,
    deepTealColor: Color,
    darkRedColor: Color
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isArrivalActive = raceState == RaceState.RUNNING
            Button(
                onClick = onArrivalClick,
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = isArrivalActive,
                colors = ButtonDefaults.buttonColors(containerColor = deepTealColor, contentColor = Color.White, disabledContainerColor = deepTealColor.copy(alpha = 0.5f), disabledContentColor = Color.White.copy(alpha = 0.5f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Arrivée d'un concurrent", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                    if (isArrivalActive) {
                        Text("(touche volume moins)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            
            val isRunning = raceState == RaceState.RUNNING || raceState == RaceState.COUNTDOWN
            val isStartActive = raceState == RaceState.IDLE || raceState == RaceState.READY
            
            FilledTonalButton(
                onClick = onStartStopClick,
                modifier = Modifier
                    .weight(1f)
                    .height(80.dp),
                shape = MaterialTheme.shapes.medium,
                colors = if (isRunning) {
                    ButtonDefaults.filledTonalButtonColors(containerColor = darkRedColor, contentColor = Color.White)
                } else {
                    ButtonDefaults.filledTonalButtonColors(containerColor = deepTealColor, contentColor = Color.White)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isRunning) "Arrêter" else "Démarrer", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
                    if (isStartActive) {
                        Text("(touche volume moins)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun MainScreenPreview() {
    Chronocoursejc2Theme {
        MainScreen()
    }
}
