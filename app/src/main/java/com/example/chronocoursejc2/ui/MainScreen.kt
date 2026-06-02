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

    MainScreenContent(
        currentTime = currentTime,
        batteryPercentage = batteryPercentage,
        arrivals = arrivals,
        raceState = raceState,
        remainingTime = remainingTime,
        elapsedTime = elapsedTime,
        showProcedureDialog = showProcedureDialog,
        showPostRaceDialog = showPostRaceDialog,
        selectedProcedure = selectedProcedure,
        plannedDepartureTimeLabel = plannedDepartureTimeLabel,
        lastSavedFileContent = lastSavedFileContent,
        onProcedureSelected = { viewModel.selectProcedure(it) },
        onStopAndSave = { viewModel.stopAndSave() },
        onResetRace = { viewModel.resetRace() },
        onArrivalClick = { viewModel.recordArrival() },
        onTriggerStartAction = { viewModel.triggerStartAction() }
    )
}

@Composable
fun MainScreenContent(
    currentTime: String,
    batteryPercentage: Int,
    arrivals: List<Arrival>,
    raceState: RaceState,
    remainingTime: Long,
    elapsedTime: Long,
    showProcedureDialog: Boolean,
    showPostRaceDialog: Boolean,
    selectedProcedure: Procedure?,
    plannedDepartureTimeLabel: String,
    lastSavedFileContent: String,
    onProcedureSelected: (Procedure) -> Unit,
    onStopAndSave: () -> Unit,
    onResetRace: () -> Unit,
    onArrivalClick: () -> Unit,
    onTriggerStartAction: () -> Unit
) {
    val context = LocalContext.current
    var showStopDialog by remember { mutableStateOf(false) }
    var showTextViewer by remember { mutableStateOf(false) }

    val customDeepTeal = Color(0xFF003333)
    val customDarkRed = Color(0xFF771010)
    val customGray = Color(0xFF909090)

    if (showProcedureDialog) {
        ProcedureSelectionDialog(onProcedureSelected = onProcedureSelected)
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Arrêter la course") },
            text = { Text("Voulez-vous vraiment arrêter et sauvegarder ?") },
            confirmButton = {
                Button(
                    onClick = {
                        onStopAndSave()
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
                    onClick = { onResetRace() },
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
                onArrivalClick = onArrivalClick,
                onStartStopClick = {
                    if (raceState == RaceState.READY || raceState == RaceState.IDLE) {
                        onTriggerStartAction()
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
                        text = "(type ${selectedProcedure.label})",
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
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Choix de la procédure de départ",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onProcedureSelected(Procedure.PROC_6510) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "6 5 1 0",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                        Button(
                            onClick = { onProcedureSelected(Procedure.PROC_3210) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "3 2 1 0",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top
                    ) {
                        Button(
                            onClick = { onProcedureSelected(Procedure.NONE) },
                            modifier = Modifier
                                .fillMaxWidth(0.48f)
                                .fillMaxHeight(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                            shape = MaterialTheme.shapes.extraLarge
                        ) {
                            Text(
                                text = "Sans compte à rebours",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
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
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val (mainText, subText) = when (raceState) {
                RaceState.IDLE -> "00:00:00" to "En attente"
                RaceState.READY -> formatTime(remainingTime) to "Prêt au départ"
                RaceState.COUNTDOWN -> formatTime(remainingTime) to "avant le départ"
                RaceState.RUNNING -> formatTime(elapsedTime) to "depuis le départ"
                RaceState.STOPPED -> formatTime(elapsedTime) to "course terminée"
            }
            
            Text(
                text = mainText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
                lineHeight = 12.sp,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
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
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Text("Rang", fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Duree", fontWeight = FontWeight.Bold, modifier = Modifier.width(85.dp), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(28.dp))
            Text("Heure", fontWeight = FontWeight.Bold, modifier = Modifier.width(85.dp), style = MaterialTheme.typography.labelSmall)
        }
        HorizontalDivider()
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(arrivals) { arrival ->
                ArrivalRow(arrival)
            }
        }
    }
}

@Composable
fun ArrivalRow(arrival: Arrival) {
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format(Locale.getDefault(), "%03d", arrival.rank),
            modifier = Modifier.width(36.dp),
            style = textStyle
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = arrival.duration,
            modifier = Modifier.width(85.dp),
            style = textStyle,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(28.dp)) // Shifted right by ~2 chars
        Text(
            text = arrival.arrivalTime,
            modifier = Modifier.width(85.dp),
            style = textStyle,
            maxLines = 1
        )
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
    val isInitial = raceState == RaceState.IDLE || raceState == RaceState.READY
    val isRunning = raceState == RaceState.RUNNING || raceState == RaceState.COUNTDOWN
    val isArrivalActive = raceState == RaceState.RUNNING

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
            // LEFT BUTTON: Démarrer or Arrivée
            Button(
                onClick = { if (isInitial) onStartStopClick() else onArrivalClick() },
                modifier = Modifier
                    .weight(1.3f)
                    .height(96.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = if (isInitial) true else isArrivalActive,
                colors = ButtonDefaults.buttonColors(
                    containerColor = deepTealColor,
                    contentColor = Color.White,
                    disabledContainerColor = deepTealColor.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isInitial) "Démarrer" else "Arrivée d'un concurrent",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (isInitial || isArrivalActive) {
                        Text(
                            text = "(touche volume moins)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // RIGHT BUTTON: Arrêter
            Button(
                onClick = onStartStopClick,
                modifier = Modifier
                    .weight(0.7f)
                    .height(96.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = isRunning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkRedColor,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContentColor = Color.Black.copy(alpha = 0.3f)
                )
            ) {
                Text("Arrêter", textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=740dp,dpi=311")
@Composable
fun MainScreenSmallPreview() {
    Chronocoursejc2Theme {
        MainScreenContent(
            currentTime = "10:30:00",
            batteryPercentage = 85,
            arrivals = listOf(Arrival(1, "00:10:05", "10:40:05")),
            raceState = RaceState.RUNNING,
            remainingTime = 0,
            elapsedTime = 605,
            showProcedureDialog = false,
            showPostRaceDialog = false,
            selectedProcedure = Procedure.PROC_6510,
            plannedDepartureTimeLabel = "10:30:00",
            lastSavedFileContent = "",
            onProcedureSelected = {},
            onStopAndSave = {},
            onResetRace = {},
            onArrivalClick = {},
            onTriggerStartAction = {}
        )
    }
}
