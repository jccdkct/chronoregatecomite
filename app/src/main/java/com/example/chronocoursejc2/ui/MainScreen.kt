package com.example.chronocoursejc2.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chronocoursejc2.BuildConfig
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
    val savedFilesCount by viewModel.savedFilesCount.collectAsStateWithLifecycle()
    val selectedBeepTone by viewModel.selectedBeepTone.collectAsStateWithLifecycle()
    val soundLevel by viewModel.soundLevel.collectAsStateWithLifecycle()
    val brightnessLevel by viewModel.brightnessLevel.collectAsStateWithLifecycle()
    val latestVersion by viewModel.latestVersion.collectAsStateWithLifecycle()
    val isUpdateAvailable by viewModel.isUpdateAvailable.collectAsStateWithLifecycle()

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
        savedFilesCount = savedFilesCount,
        selectedBeepTone = selectedBeepTone,
        soundLevel = soundLevel,
        brightnessLevel = brightnessLevel,
        latestVersion = latestVersion,
        isUpdateAvailable = isUpdateAvailable,
        onProcedureSelected = { viewModel.selectProcedure(it) },
        onStopAndSave = { viewModel.stopAndSave() },
        onResetRace = { viewModel.resetRace() },
        onArrivalClick = { viewModel.recordArrival() },
        onTriggerStartAction = { viewModel.triggerStartAction() },
        onUpdateSail = { rankOrId, sail, isClassified -> viewModel.updateSailNumber(rankOrId, sail, isClassified) },
        onToggleExclusion = { viewModel.toggleArrivalExclusion(it) },
        onrecordNonClassified = { code, sail -> viewModel.recordNonClassified(code, sail) },
        onUpdateNonClassifiedCode = { id, code -> viewModel.updateNonClassifiedCode(id, code) },
        onRefreshFiles = { viewModel.updateSavedFilesCount() },
        onSetBeepTone = { viewModel.setBeepTone(it) },
        onSetSoundLevel = { viewModel.setSoundLevel(it) },
        onSetBrightnessLevel = { viewModel.setBrightnessLevel(it) }
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
    savedFilesCount: Int,
    selectedBeepTone: Int,
    soundLevel: Int,
    brightnessLevel: Int,
    latestVersion: String,
    isUpdateAvailable: Boolean,
    onProcedureSelected: (Procedure) -> Unit,
    onStopAndSave: () -> Unit,
    onResetRace: () -> Unit,
    onArrivalClick: () -> Unit,
    onTriggerStartAction: () -> Unit,
    onUpdateSail: (Long, String, Boolean) -> Unit,
    onToggleExclusion: (Long) -> Unit,
    onrecordNonClassified: (String, String) -> Unit,
    onUpdateNonClassifiedCode: (Long, String) -> Unit,
    onRefreshFiles: () -> Unit,
    onSetBeepTone: (Int) -> Unit,
    onSetSoundLevel: (Int) -> Unit,
    onSetBrightnessLevel: (Int) -> Unit
) {
    val context = LocalContext.current
    var showStopDialog by remember { mutableStateOf(false) }
    var showTextViewer by remember { mutableStateOf(false) }
    var forceShowPostRaceDialog by remember { mutableStateOf(false) }
    var editingSailForArrival by remember { mutableStateOf<Arrival?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNonClassifiedCodeDialog by remember { mutableStateOf(false) }
    var pendingNonClassifiedCode by remember { mutableStateOf<String?>(null) }
    var showNonClassifiedSailDialog by remember { mutableStateOf(false) }
    var editingCodeForArrival by remember { mutableStateOf<Arrival?>(null) }

    val customDeepTeal = Color(0xFF003333)
    val customDarkRed = Color(0xFF771010)
    val customGray = Color(0xFF909090)

    if (showNonClassifiedCodeDialog || editingCodeForArrival != null) {
        NonClassifiedCodeDialog(
            initialValue = editingCodeForArrival?.duration ?: "",
            onConfirm = { code ->
                if (editingCodeForArrival != null) {
                    onUpdateNonClassifiedCode(editingCodeForArrival!!.id, code)
                    editingCodeForArrival = null
                } else {
                    pendingNonClassifiedCode = code
                    showNonClassifiedCodeDialog = false
                    showNonClassifiedSailDialog = true
                }
            },
            onDismiss = { 
                showNonClassifiedCodeDialog = false 
                editingCodeForArrival = null
            }
        )
    }

    if (showNonClassifiedSailDialog) {
        SailNumberEntryDialog(
            initialValue = "",
            rank = 0, // Visual only
            onConfirm = { _, sail ->
                pendingNonClassifiedCode?.let { code ->
                    onrecordNonClassified(code, sail)
                }
                showNonClassifiedSailDialog = false
                pendingNonClassifiedCode = null
            },
            onDismiss = { 
                showNonClassifiedSailDialog = false
                pendingNonClassifiedCode = null
            }
        )
    }

    if (showProcedureDialog) {
        ProcedureSelectionDialog(
            latestVersion = latestVersion,
            isUpdateAvailable = isUpdateAvailable,
            onProcedureSelected = onProcedureSelected
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Suppression") },
            text = { Text("Voulez-vous supprimer toutes les sauvegardes de plus d'un mois ?") },
            confirmButton = {
                Button(
                    onClick = {
                        val documentsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS)
                        val folder = java.io.File(documentsDir, "Chronocourse")
                        if (folder.exists() && folder.isDirectory) {
                            val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                            folder.listFiles()?.forEach { file ->
                                if (file.lastModified() < oneMonthAgo) {
                                    file.delete()
                                }
                            }
                        }
                        showDeleteConfirm = false
                        onRefreshFiles()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = customDarkRed, contentColor = Color.White)
                ) {
                    Text("Oui")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = customDeepTeal, contentColor = Color.White)
                ) {
                    Text("Non")
                }
            }
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
                    Text("Continuer\nla course", textAlign = TextAlign.Center)
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
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.setDataAndType(android.net.Uri.parse("content://com.android.externalstorage.documents/root/primary"), "vnd.android.document/root")
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT)
                                    fallbackIntent.type = "*/*"
                                    context.startActivity(Intent.createChooser(fallbackIntent, "Ouvrir les documents"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = customGray, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("explorateur ($savedFilesCount)", style = MaterialTheme.typography.labelSmall)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = customGray, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Nettoyer > 1 mois", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        }
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
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Text("partager", style = MaterialTheme.typography.labelSmall)
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

    if (editingSailForArrival != null) {
        SailNumberEntryDialog(
            initialValue = editingSailForArrival?.sailNumber ?: "",
            rank = editingSailForArrival?.rank ?: 0,
            onConfirm = { rank, sail ->
                editingSailForArrival?.let {
                    onUpdateSail(if (it.isClassified) rank.toLong() else it.id, sail, it.isClassified)
                }
                editingSailForArrival = null
            },
            onDismiss = { editingSailForArrival = null }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                currentTime, 
                batteryPercentage, 
                selectedBeepTone, 
                soundLevel,
                brightnessLevel,
                onSetBeepTone,
                onSetSoundLevel,
                onSetBrightnessLevel
            )
        },
        bottomBar = {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                Spacer(modifier = Modifier.height(16.dp)) // Extra space above buttons
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
                    onNonClassifiedClick = { showNonClassifiedCodeDialog = true },
                    deepTealColor = customDeepTeal,
                    darkRedColor = customDarkRed
                )
            }
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
                            text = "(type ${selectedProcedure.label.replace("\n", " ")})",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                // The list now has weight(1f), expanding to cover the image
                ArrivalList(
                    arrivals = arrivals, 
                    onEditClick = { editingSailForArrival = it }, 
                    onEditCodeClick = { editingCodeForArrival = it },
                    onToggleExclusion = onToggleExclusion,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ProcedureSelectionDialog(
    latestVersion: String,
    isUpdateAvailable: Boolean,
    onProcedureSelected: (Procedure) -> Unit
) {
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

                            Spacer(modifier = Modifier.height(28.dp))

                            val context = LocalContext.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
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
                                    modifier = Modifier.height(32.dp).width(80.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Quitter", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(64.dp)) // Space for the banner (taller now)
                        }

                    // Update & Share Banner - Full Width at the bottom
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    val context = LocalContext.current
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .align(Alignment.BottomCenter),
                        color = Color.Black,
                        contentColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Line 1: Share Application
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clickable {
                                        val shareText = "Site internet de l'app ChronoCourseJC2 : https://github.com/jccdkct/Chronocoursejc2\nLien de téléchargement du dernier fichier d'installation APK de cette application : https://github.com/jccdkct/Chronocoursejc2/releases/latest/download/chronocoursejc2.apk"
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, null)
                                        context.startActivity(shareIntent)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Partager cette application",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White // Writing in white
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.3f), thickness = 0.5.dp)

                            // Line 2: Version & Update
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clickable(enabled = isUpdateAvailable) {
                                        if (isUpdateAvailable) {
                                            uriHandler.openUri("https://github.com/jccdkct/Chronocoursejc2/releases/latest/download/chronocoursejc2.apk")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isUpdateAvailable) {
                                        "Version ${BuildConfig.VERSION_NAME} - télécharger mise à jour $latestVersion"
                                    } else {
                                        "Version ${BuildConfig.VERSION_NAME} - Application à jour"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 10.sp
                                )
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TopBar(
    currentTime: String, 
    batteryPercentage: Int, 
    selectedBeepTone: Int,
    initialSoundLevel: Int,
    initialBrightnessLevel: Int,
    onSetBeepTone: (Int) -> Unit,
    onSetSoundLevel: (Int) -> Unit,
    onSetBrightnessLevel: (Int) -> Unit
) {
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    
    var brightnessLevel by remember { mutableIntStateOf(initialBrightnessLevel) }
    var soundLevel by remember { mutableIntStateOf(initialSoundLevel) }

    fun setBrightness(level: Int) {
        val activity = context as? Activity ?: return
        val layoutParams: WindowManager.LayoutParams = activity.window.attributes
        layoutParams.screenBrightness = level / 100f
        activity.window.attributes = layoutParams
        onSetBrightnessLevel(level)
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
        onSetSoundLevel(level)
    }

    // Apply max levels on launch
    LaunchedEffect(Unit) {
        setBrightness(brightnessLevel)
        setSound(soundLevel)
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
                
                Spacer(modifier = Modifier.width(6.dp))
                
                // Cycling Luminosity Button
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = lumIcon),
                        contentDescription = "Luminosité",
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(2.dp))
                
                // Cycling Sound Button
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
                    modifier = Modifier.size(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = soundIcon),
                        contentDescription = "Volume",
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Cycling Beep Button
                IconButton(
                    onClick = {
                        val nextTone = when(selectedBeepTone) {
                            android.media.ToneGenerator.TONE_CDMA_PIP -> android.media.ToneGenerator.TONE_PROP_BEEP2
                            android.media.ToneGenerator.TONE_PROP_BEEP2 -> android.media.ToneGenerator.TONE_CDMA_HIGH_L
                            else -> android.media.ToneGenerator.TONE_CDMA_PIP
                        }
                        onSetBeepTone(nextTone)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Bip",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        val badgeText = when(selectedBeepTone) {
                            android.media.ToneGenerator.TONE_CDMA_PIP -> "1"
                            android.media.ToneGenerator.TONE_PROP_BEEP2 -> "2"
                            else -> "3"
                        }
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.align(Alignment.BottomEnd).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.extraSmall).padding(1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_custom),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(28.dp)
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
fun ArrivalList(
    arrivals: List<Arrival>, 
    onEditClick: (Arrival) -> Unit, 
    onEditCodeClick: (Arrival) -> Unit,
    onToggleExclusion: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showDuration by remember { mutableStateOf(true) }
    
    // Sort: Classified first (by rank), then Non-Classified (by ID/entry time)
    val sortedArrivals = arrivals.sortedWith(
        compareByDescending<Arrival> { it.isClassified }
            .thenBy { it.id }
    )

    LaunchedEffect(arrivals.size) {
        if (arrivals.isNotEmpty()) {
            listState.animateScrollToItem(arrivals.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("", modifier = Modifier.width(28.dp))
            VerticalSeparator()
            Text("Rang", fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            VerticalSeparator()
            
            // Toggleable Column Header
            Row(
                modifier = Modifier.width(90.dp).clickable { showDuration = !showDuration },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (showDuration) "Durée" else "Heure",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
                Icon(
                    imageVector = Icons.Rounded.SwapHoriz,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            VerticalSeparator()
            Text("N° Voile", fontWeight = FontWeight.Bold, modifier = Modifier.width(90.dp), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            VerticalSeparator()
            Text("", modifier = Modifier.width(48.dp))
        }
        HorizontalDivider(thickness = 2.dp, color = Color.Black)
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                sortedArrivals,
                key = { it.id }
            ) { arrival ->
                val index = sortedArrivals.indexOf(arrival)
                ArrivalRow(
                    arrival = arrival, 
                    index = index, 
                    onEditClick = onEditClick, 
                    onEditCodeClick = onEditCodeClick,
                    onToggleExclusion = onToggleExclusion,
                    showDuration = showDuration,
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun VerticalSeparator() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(20.dp)
            .background(Color.Gray.copy(alpha = 0.5f))
    )
}

@Composable
fun ArrivalRow(
    arrival: Arrival, 
    index: Int, 
    onEditClick: (Arrival) -> Unit, 
    onEditCodeClick: (Arrival) -> Unit,
    onToggleExclusion: (Long) -> Unit,
    showDuration: Boolean,
    modifier: Modifier = Modifier
) {
    val violetColor = Color(0xFF8A2BE2)
    val baseTextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
    val textStyle = if (arrival.isClassified) baseTextStyle else baseTextStyle.copy(color = violetColor)
    
    val backgroundColor = if (index % 2 == 0) Color.White else Color(0xFFF9F9F9)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight()
                    .clickable { onToggleExclusion(arrival.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (arrival.isExcluded) Icons.Rounded.Refresh else Icons.Rounded.Close,
                    contentDescription = if (arrival.isExcluded) "Rétablir" else "Supprimer",
                    modifier = Modifier.size(20.dp),
                    tint = if (arrival.isExcluded) Color(0xFF003333) else Color(0xFF771010)
                )
            }
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
            
            // RANG COLUMN: Show code if non-classified
            Text(
                text = if (arrival.isExcluded) "" else (if (arrival.isClassified) arrival.rank.toString() else arrival.duration),
                modifier = Modifier.width(36.dp),
                style = textStyle.copy(fontSize = if (arrival.isClassified) 14.sp else 10.sp),
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
            
            // DATA COLUMN / EDIT CODE BUTTON
            if (arrival.isClassified) {
                Text(
                    text = if (showDuration) arrival.duration else arrival.arrivalTime,
                    modifier = Modifier.width(90.dp),
                    style = textStyle,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .clickable { onEditCodeClick(arrival) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.iconeeditviolet),
                        contentDescription = "Éditer Code",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
            Text(
                text = arrival.sailNumber,
                modifier = Modifier.width(90.dp),
                style = baseTextStyle,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.Gray.copy(alpha = 0.3f)))
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .clickable { onEditClick(arrival) },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconeedit),
                    contentDescription = "Éditer",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // RED STRIKE-THROUGH LINE when excluded
        if (arrival.isExcluded) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red.copy(alpha = 0.15f))
            ) {
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.Center),
                    thickness = 3.dp,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun BottomButtons(
    raceState: RaceState,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    onNonClassifiedClick: () -> Unit,
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
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Button (Large)
            Button(
                onClick = onLeftClick,
                modifier = Modifier
                    .weight(1.32f)
                    .height(64.dp),
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
                        text = if (isInitial) "Démarrer" else "Arrivée concurrent",
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

            // Right Column (Two buttons)
            Column(
                modifier = Modifier.weight(0.68f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Non-Classified Button (Half height)
                Button(
                    onClick = onNonClassifiedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = deepTealColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("+ non classé", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }

                // Stop Button (Half height)
                Button(
                    onClick = onRightClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = darkRedColor,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Arrêter", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SailNumberEntryDialog(
    initialValue: String,
    rank: Int,
    onConfirm: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentValue by remember { mutableStateOf(initialValue) }
    var isNumeric by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "N° de Voile\nRang $rank",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                    
                    Button(
                        onClick = { isNumeric = !isNumeric },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF607D8B))
                    ) {
                        Text(if (isNumeric) "ABC" else "123")
                    }
                }

                // Result Area
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = currentValue,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Keypad
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isNumeric) {
                        val keys = listOf(
                            listOf("FRA", "GBR", "BEL", "NED"),
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("EFF", "0", "⌫")
                        )
                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { key ->
                                    KeypadButton(key, modifier = Modifier.weight(1f), isSmall = key.length > 1) {
                                        when (key) {
                                            "EFF" -> currentValue = ""
                                            "⌫" -> if (currentValue.isNotEmpty()) currentValue = currentValue.dropLast(1)
                                            "FRA", "GBR", "BEL", "NED" -> if (currentValue.length < 12) currentValue += key
                                            else -> if (currentValue.length < 12) currentValue += key
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Alpha Keypad (Alphabetical Layout)
                        val alphabet = ('A'..'Z').map { it.toString() } + listOf(" ", "EFF", "⌫")
                        val rows = alphabet.chunked(5)
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                row.forEach { key ->
                                    KeypadButton(key, modifier = Modifier.weight(1f), isSmall = true) {
                                        when (key) {
                                            "EFF" -> currentValue = ""
                                            "⌫" -> if (currentValue.isNotEmpty()) currentValue = currentValue.dropLast(1)
                                            else -> if (currentValue.length < 12) currentValue += key
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Annuler", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm(rank, currentValue) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003333))
                    ) {
                        Text("Valider", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NonClassifiedCodeDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentValue by remember { mutableStateOf(initialValue) }
    val codes = listOf(
        "DNC" to "Did Not Come",
        "DNS" to "Did Not Start",
        "OCS" to "On Course Side",
        "BFD" to "Black Flag Disq.",
        "UFD" to "U Flag Disq.",
        "DNF" to "Did Not Finish",
        "NSC" to "Did Not Sail Course",
        "RET" to "Retired",
        "DSQ" to "Disqualified"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Code Classement",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Result Area (Similar to sail keypad)
                Surface(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = currentValue, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Predefined Codes with Descriptions
                    codes.chunked(3).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (code, desc) ->
                                Button(
                                    onClick = { currentValue = code },
                                    modifier = Modifier.weight(1f).height(74.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF505050)),
                                    contentPadding = PaddingValues(2.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                        Text(
                                            text = desc, 
                                            style = MaterialTheme.typography.labelSmall, 
                                            fontSize = 11.sp, 
                                            textAlign = TextAlign.Center, 
                                            lineHeight = 12.sp,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Alphabet Keypad
                    val alphabet = ('A'..'Z').map { it.toString() } + listOf(" ", "EFF", "⌫")
                    alphabet.chunked(6).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { key ->
                                KeypadButton(
                                    text = key, 
                                    modifier = Modifier.weight(1f).height(45.dp),
                                    isSmall = true
                                ) {
                                    when (key) {
                                        "EFF" -> currentValue = ""
                                        "⌫" -> if (currentValue.isNotEmpty()) currentValue = currentValue.dropLast(1)
                                        else -> if (currentValue.length < 8) currentValue += key
                                    }
                                }
                            }
                            // Fill empty slots in last row if needed
                            if (row.size < 6) {
                                repeat(6 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                        Text("Annuler", fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { onConfirm(currentValue) }, modifier = Modifier.weight(1f).fillMaxHeight(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003333))) {
                        Text("Valider", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    isSmall: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.small,
        colors = if (text == "EFF" || text == "⌫")
            ButtonDefaults.buttonColors(containerColor = Color(0xFF771010))
            else ButtonDefaults.buttonColors(containerColor = Color(0xFF505050))
    ) {
        Text(
            text = text, 
            style = if (isSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Bold
        )
    }
}
@Composable
fun MainScreenSmallPreview() {
    Chronocoursejc2Theme {
        MainScreenContent(
            currentTime = "10:30:00",
            batteryPercentage = 85,
            arrivals = listOf(Arrival(1L, 1, "00:10:05", "10:40:05", "123456")),
            raceState = RaceState.RUNNING,
            remainingTime = 0,
            elapsedTime = 605,
            showProcedureDialog = false,
            showPostRaceDialog = false,
            selectedProcedure = Procedure.PROC_6410,
            plannedDepartureTimeLabel = "10:30:00",
            lastSavedFileContent = "Application Chronocoursejc2\n...",
            savedFilesCount = 5,
            selectedBeepTone = android.media.ToneGenerator.TONE_CDMA_PIP,
            soundLevel = 3,
            brightnessLevel = 100,
            latestVersion = "v009",
            isUpdateAvailable = false,
            onProcedureSelected = {},
            onStopAndSave = {},
            onResetRace = {},
            onArrivalClick = {},
            onTriggerStartAction = {},
            onUpdateSail = { _, _, _ -> },
            onToggleExclusion = {},
            onrecordNonClassified = { _, _ -> },
            onUpdateNonClassifiedCode = { _, _ -> },
            onRefreshFiles = {},
            onSetBeepTone = {},
            onSetSoundLevel = {},
            onSetBrightnessLevel = {}
        )
    }
}
