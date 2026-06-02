package com.example.chronocoursejc2.ui

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
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
    var forceShowPostRaceDialog by remember { mutableStateOf(false) }

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
                    colors = ButtonDefaults.buttonColors(containerColor = customDeepTeal, contentColor = Color.White)
                ) {
                    Text("continuer")
                }
            }
        )
    }

    if (showPostRaceDialog || forceShowPostRaceDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Course terminée") },
            text = {
                Column {
                    Text("Listing sauvegardé dans Documents/Chronocourse.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                // Attempt to open the file manager at the documents root
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(android.net.Uri.parse("content://com.android.externalstorage.documents/root/primary"), "vnd.android.document/root")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback to a generic picker if the specific root fails
                                    val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT)
                                    fallbackIntent.type = "*/*"
                                    context.startActivity(Intent.createChooser(fallbackIntent, "Ouvrir les documents"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = customGray, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ouvrir l'explorateur de fichiers", style = MaterialTheme.typography.labelSmall)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, lastSavedFileContent)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = customGray, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("partager", style = MaterialTheme.typography.labelSmall)
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
                    onClick = { 
                        forceShowPostRaceDialog = false
                        onResetRace() 
                    },
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
                onLeftClick = {
                    val isInitial = raceState == RaceState.IDLE || raceState == RaceState.READY
                    if (isInitial) {
                        onTriggerStartAction()
                    } else {
                        onArrivalClick()
                    }
                },
                onRightClick = {
                    val isInitial = raceState == RaceState.IDLE || raceState == RaceState.READY
                    if (isInitial) {
                        onStopAndSave() 
                        forceShowPostRaceDialog = true
                    } else {
                        showStopDialog = true
                    }
                },
                deepTealColor = customDeepTeal,
                darkRedColor = customDarkRed
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // FOOTER IMAGE: Single instance, aligned at the bottom of the screen area
            Image(
                painter = painterResource(id = R.drawable.fond3),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                alpha = 0.5f // Semi-transparent so text is readable over it
            )
            
            Column(
                modifier = Modifier
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
                // The list now has weight(1f), expanding to cover the image
                ArrivalList(arrivals, modifier = Modifier.weight(1f))
            }
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
                    .fillMaxHeight(0.9f) // Increased slightly
                    .padding(16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.fond3),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.5f
                    )
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top // Start from top for better small screen fit
                        ) {
                            Text(
                                text = "Choix du compte à rebours (minutes)",
                                style = MaterialTheme.typography.titleMedium, // Smaller title
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp), // Reduced height further
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProcedureButton(
                                    procedure = Procedure.PROC_10,
                                    color = Color(0xFFFF5722),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                                ProcedureButton(
                                    procedure = Procedure.PROC_210,
                                    color = Color(0xFF673AB7),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProcedureButton(
                                    procedure = Procedure.PROC_3210,
                                    color = Color(0xFF2196F3),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                                ProcedureButton(
                                    procedure = Procedure.PROC_5410,
                                    color = Color(0xFF4CAF50),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProcedureButton(
                                    procedure = Procedure.PROC_6410,
                                    color = Color(0xFFFF9800),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                                ProcedureButton(
                                    procedure = Procedure.PROC_8410,
                                    color = Color(0xFFE91E63),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProcedureButton(
                                    procedure = Procedure.PROC_10410,
                                    color = Color(0xFF00BCD4),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                                ProcedureButton(
                                    procedure = Procedure.NONE,
                                    color = Color(0xFF9C27B0),
                                    onProcedureSelected = onProcedureSelected,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val context = LocalContext.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { 
                                        var currentContext = context
                                        while (currentContext !is Activity && currentContext is android.content.ContextWrapper) {
                                            currentContext = currentContext.baseContext
                                        }
                                        (currentContext as? Activity)?.finishAffinity()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF771010), contentColor = Color.White),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.height(48.dp).width(110.dp)
                                ) {
                                    Text("Quitter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                }
            }
        }
    }
}

@Composable
fun ProcedureButton(
    procedure: Procedure,
    color: Color,
    onProcedureSelected: (Procedure) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onProcedureSelected(procedure) },
        modifier = modifier.fillMaxHeight(),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Text(
            text = procedure.label,
            style = if (procedure == Procedure.NONE) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TopBar(currentTime: String, batteryPercentage: Int) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    
    var brightnessLevel by remember { mutableIntStateOf(100) }
    var soundLevel by remember { mutableIntStateOf(3) }

    fun setBrightness(level: Int) {
        val activity = context as? Activity ?: return
        val layoutParams: WindowManager.LayoutParams = activity.window.attributes
        layoutParams.screenBrightness = level / 100f
        activity.window.attributes = layoutParams
    }

    fun setSound(level: Int) {
        val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
        val vol = when(level) {
            3 -> maxVol
            2 -> (maxVol * 0.6).toInt()
            1 -> (maxVol * 0.3).toInt()
            else -> maxVol
        }
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, vol, 0)
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Schedule, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentTime,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.BatteryFull, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$batteryPercentage%",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                val lumIcon = when(brightnessLevel) {
                    100 -> R.drawable.lum100
                    50 -> R.drawable.lum050
                    else -> R.drawable.lum020
                }
                IconButton(
                    onClick = {
                        brightnessLevel = when(brightnessLevel) {
                            100 -> 50
                            50 -> 20
                            else -> 100
                        }
                        setBrightness(brightnessLevel)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = lumIcon),
                        contentDescription = "Luminosité $brightnessLevel%",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                val soundIcon = when(soundLevel) {
                    3 -> R.drawable.son3
                    2 -> R.drawable.son2
                    else -> R.drawable.son1
                }
                IconButton(
                    onClick = {
                        soundLevel = when(soundLevel) {
                            3 -> 2
                            2 -> 1
                            else -> 3
                        }
                        setSound(soundLevel)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(id = soundIcon),
                        contentDescription = "Volume niveau $soundLevel",
                        modifier = Modifier.size(32.dp)
                    )
                }

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
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
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
fun ArrivalList(arrivals: List<Arrival>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(arrivals.size) {
        if (arrivals.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
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
            itemsIndexed(arrivals) { index, arrival ->
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
        Spacer(modifier = Modifier.width(28.dp))
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
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    deepTealColor: Color,
    darkRedColor: Color
) {
    val isInitial = raceState == RaceState.IDLE || raceState == RaceState.READY
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
            Button(
                onClick = onLeftClick,
                modifier = Modifier
                    .weight(1.15f)
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

            Button(
                onClick = onRightClick,
                modifier = Modifier
                    .weight(0.85f)
                    .height(96.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkRedColor,
                    contentColor = Color.White
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
            selectedProcedure = Procedure.PROC_6410,
            plannedDepartureTimeLabel = "10:30:00",
            lastSavedFileContent = "ApplicationChronocoursejc2\n...",
            onProcedureSelected = {},
            onStopAndSave = {},
            onResetRace = {},
            onArrivalClick = {},
            onTriggerStartAction = {}
        )
    }
}
