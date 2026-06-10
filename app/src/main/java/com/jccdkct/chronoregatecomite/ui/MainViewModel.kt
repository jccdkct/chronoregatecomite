package com.jccdkct.chronoregatecomite.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RaceState {
    IDLE, READY, COUNTDOWN, RUNNING, STOPPED
}

enum class Procedure(val totalSeconds: Long, val milestones: List<Long>, val label: String) {
    PROC_10(60, listOf(0), "1-0"),
    PROC_210(120, listOf(60, 0), "2-1-0"),
    PROC_3210(180, listOf(120, 60, 0), "3-2-1-0"),
    PROC_5410(300, listOf(240, 60, 0), "5-4-1-0"),
    PROC_6410(360, listOf(240, 60, 0), "6-4-1-0"),
    PROC_8410(480, listOf(240, 60, 0), "8-4-1-0"),
    PROC_10410(600, listOf(240, 60, 0), "10-4-1-0"),
    NONE(0, emptyList(), "Chrono")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("ChronoPrefs", Context.MODE_PRIVATE)

    private val _currentTime = MutableStateFlow(getCurrentFormattedTime())
    val currentTime: StateFlow<String> = _currentTime.asStateFlow()

    private val _batteryPercentage = MutableStateFlow(getInitialBatteryPercentage())
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _arrivals = MutableStateFlow<List<Arrival>>(emptyList())
    val arrivals: StateFlow<List<Arrival>> = _arrivals.asStateFlow()

    private val _raceState = MutableStateFlow(RaceState.IDLE)
    val raceState: StateFlow<RaceState> = _raceState.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime: StateFlow<Long> = _remainingTime.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _showProcedureDialog = MutableStateFlow(true)
    val showProcedureDialog: StateFlow<Boolean> = _showProcedureDialog.asStateFlow()

    private val _showPostRaceDialog = MutableStateFlow(false)
    val showPostRaceDialog: StateFlow<Boolean> = _showPostRaceDialog.asStateFlow()

    private val _selectedProcedure = MutableStateFlow<Procedure?>(null)
    val selectedProcedure: StateFlow<Procedure?> = _selectedProcedure.asStateFlow()

    private val _plannedDepartureTimeLabel = MutableStateFlow("")
    val plannedDepartureTimeLabel: StateFlow<String> = _plannedDepartureTimeLabel.asStateFlow()

    private val _lastSavedFileContent = MutableStateFlow("")
    val lastSavedFileContent: StateFlow<String> = _lastSavedFileContent.asStateFlow()

    private val _savedFilesCount = MutableStateFlow(0)
    val savedFilesCount: StateFlow<Int> = _savedFilesCount.asStateFlow()

    private val _selectedBeepTone = MutableStateFlow(prefs.getInt("beepTone", ToneGenerator.TONE_CDMA_PIP))
    val selectedBeepTone: StateFlow<Int> = _selectedBeepTone.asStateFlow()

    private val _soundLevel = MutableStateFlow(prefs.getInt("soundLevel", 3))
    val soundLevel: StateFlow<Int> = _soundLevel.asStateFlow()

    private val _brightnessLevel = MutableStateFlow(prefs.getInt("brightnessLevel", 100))
    val brightnessLevel: StateFlow<Int> = _brightnessLevel.asStateFlow()

    private var raceJob: Job? = null
    private var startTime: Long = 0L
    private var departureTime: Long = 0L
    private var startFormattedTime: String = ""

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level != -1 && scale != -1) {
                    _batteryPercentage.value = (level * 100 / scale.toFloat()).toInt()
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = getCurrentFormattedTime()
                delay(1000)
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryReceiver, filter)
        updateSavedFilesCount()
    }

    fun updateSavedFilesCount() {
        viewModelScope.launch {
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val folder = File(documentsDir, "ChronoRegateComite")
            if (folder.exists() && folder.isDirectory) {
                val count = folder.listFiles { file -> file.isFile && file.name.endsWith(".txt") }?.size ?: 0
                _savedFilesCount.value = count
            } else {
                _savedFilesCount.value = 0
            }
        }
    }

    private fun formatTimeFull(time: Date): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(time)
    }

    fun selectProcedure(procedure: Procedure) {
        _selectedProcedure.value = procedure
        _showProcedureDialog.value = false
        _raceState.value = RaceState.READY
        _remainingTime.value = procedure.totalSeconds
        
        val estDeparture = System.currentTimeMillis() + (procedure.totalSeconds * 1000)
        _plannedDepartureTimeLabel.value = formatTimeFull(Date(estDeparture))
    }

    fun triggerStartAction() {
        if (_raceState.value == RaceState.IDLE) {
            _showProcedureDialog.value = true
        } else if (_raceState.value == RaceState.READY) {
            startCountdown()
        }
    }

    fun startCountdown() {
        val procedure = _selectedProcedure.value ?: return
        raceJob?.cancel()
        playBeep()
        
        if (procedure == Procedure.NONE) {
            _raceState.value = RaceState.RUNNING
            startTime = System.currentTimeMillis()
            startFormattedTime = formatTimeFull(Date(startTime))
            _plannedDepartureTimeLabel.value = startFormattedTime
            
            raceJob = viewModelScope.launch {
                while (_raceState.value == RaceState.RUNNING) {
                    val now = System.currentTimeMillis()
                    _elapsedTime.value = (now - startTime) / 1000
                    delay(100)
                }
            }
            return
        }

        _raceState.value = RaceState.COUNTDOWN
        departureTime = System.currentTimeMillis() + (procedure.totalSeconds * 1000)
        _plannedDepartureTimeLabel.value = formatTimeFull(Date(departureTime))

        raceJob = viewModelScope.launch {
            while (_remainingTime.value > 0) {
                val now = System.currentTimeMillis()
                val diff = (departureTime - now) / 1000
                if (diff != _remainingTime.value) {
                    _remainingTime.value = diff.coerceAtLeast(0)
                    checkBeep(_remainingTime.value)
                }
                delay(100)
            }
            
            _raceState.value = RaceState.RUNNING
            startTime = departureTime
            startFormattedTime = formatTimeFull(Date(startTime))
            
            while (_raceState.value == RaceState.RUNNING) {
                val now = System.currentTimeMillis()
                _elapsedTime.value = (now - startTime) / 1000
                delay(100)
            }
        }
    }

    private fun checkBeep(secondsLeft: Long) {
        val procedure = _selectedProcedure.value ?: return
        val isBeep = procedure.milestones.any { milestone ->
            secondsLeft in milestone..(milestone + 12)
        }
        
        if (isBeep) {
            playBeep()
        }
    }

    private fun playBeep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        toneGen.startTone(_selectedBeepTone.value, 150)
    }

    fun recordArrival() {
        if (_raceState.value != RaceState.RUNNING) return
        playBeep()
        
        val now = System.currentTimeMillis()
        val durationMillis = now - startTime
        val arrivalTime = formatTimeFull(Date(now))
        
        val newArrival = Arrival(
            id = System.nanoTime(),
            rank = 0, // Will be set by refreshRanks
            duration = formatDuration(durationMillis),
            arrivalTime = arrivalTime,
            sailNumber = ""
        )
        _arrivals.value = _arrivals.value + listOf(newArrival)
        refreshRanks()
    }

    fun recordNonClassified(code: String, sailNumber: String) {
        val newArrival = Arrival(
            id = System.nanoTime(),
            rank = 0,
            duration = code,
            arrivalTime = code,
            sailNumber = sailNumber,
            isClassified = false
        )
        _arrivals.value = _arrivals.value + listOf(newArrival)
        // Ranks for non-classified stay 0, classified ones are re-ranked
        refreshRanks()
    }

    fun updateSailNumber(id: Long, sailNumber: String) {
        _arrivals.value = _arrivals.value.map {
            if (it.id == id) it.copy(sailNumber = sailNumber) else it
        }
    }

    fun updateNonClassifiedCode(id: Long, code: String) {
        _arrivals.value = _arrivals.value.map {
            if (it.id == id) it.copy(duration = code, arrivalTime = code) else it
        }
    }

    fun toggleArrivalExclusion(arrivalId: Long) {
        _arrivals.value = _arrivals.value.map {
            if (it.id == arrivalId) it.copy(isExcluded = !it.isExcluded) else it
        }
        refreshRanks()
    }

    private fun refreshRanks() {
        var currentRank = 1
        _arrivals.value = _arrivals.value.map { arrival ->
            if (arrival.isExcluded || !arrival.isClassified) {
                arrival.copy(rank = 0)
            } else {
                arrival.copy(rank = currentRank++)
            }
        }
    }

    fun setBeepTone(tone: Int) {
        _selectedBeepTone.value = tone
        prefs.edit().putInt("beepTone", tone).apply()
        playBeep()
    }

    fun setSoundLevel(level: Int) {
        _soundLevel.value = level
        prefs.edit().putInt("soundLevel", level).apply()
    }

    fun setBrightnessLevel(level: Int) {
        _brightnessLevel.value = level
        prefs.edit().putInt("brightnessLevel", level).apply()
    }

    fun stopAndSave() {
        _raceState.value = RaceState.STOPPED
        raceJob?.cancel()
        saveResultsToFile()
    }

    private fun saveResultsToFile() {
        viewModelScope.launch {
            val fileDateStr = SimpleDateFormat("yyyy-MM-dd--HH'h'mm'm'ss's'", Locale.getDefault()).format(Date(startTime))
            val fileName = "ccjc2_$fileDateStr.txt"
            val content = buildString {
                append("\uFEFF") // UTF-8 BOM for Windows encoding compatibility
                append("Application Chrono Régate Comité\n")
                val startDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(startTime))
                append("Arrivees de la course dont le depart a eu lieu\n")
                append("le $startDateStr à $startFormattedTime\n")
                append("______________________________________\n")
                append(String.format(Locale.getDefault(), "%-4s  %-8s  %-8s  %s\n", "Rang", "Duree", "Heure", "N° Voile"))
                append("______________________________________\n")
                _arrivals.value.filter { !it.isExcluded }.sortedWith(
                    compareByDescending<Arrival> { it.isClassified }
                        .thenBy { it.id }
                ).forEach { arrival ->
                    if (arrival.isClassified) {
                        val rankStr = String.format(Locale.getDefault(), " %03d", arrival.rank)
                        append(String.format(Locale.getDefault(), "%s  %-8s  %-8s  %s\n", rankStr, arrival.duration, arrival.arrivalTime, arrival.sailNumber))
                    } else {
                        // Non-classified: code (stored in duration) in the rank column, empty time columns
                        val codeStr = String.format(Locale.getDefault(), " %-3s", arrival.duration)
                        append(String.format(Locale.getDefault(), "%s  %-8s  %-8s  %s\n", codeStr, "", "", arrival.sailNumber))
                    }
                }
            }
            _lastSavedFileContent.value = content

            val relativePath = "${Environment.DIRECTORY_DOCUMENTS}/ChronoRegateComite"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = getApplication<Application>().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                }
                // Use MediaStore.Files for Documents folder
                val uri: Uri? = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { it.write(content.toByteArray()) }
                }
            } else {
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val folder = File(documentsDir, "ChronoRegateComite")
                if (!folder.exists()) folder.mkdirs()
                val file = File(folder, fileName)
                FileOutputStream(file).use { it.write(content.toByteArray()) }
            }
            
            _showPostRaceDialog.value = true
            updateSavedFilesCount()
        }
    }
    
    fun resetRace() {
        _raceState.value = RaceState.IDLE
        _arrivals.value = emptyList()
        _elapsedTime.value = 0
        _remainingTime.value = 0
        _showPostRaceDialog.value = false
        _selectedProcedure.value = null
        _showProcedureDialog.value = true
        _lastSavedFileContent.value = ""
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    private fun getCurrentFormattedTime(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun getInitialBatteryPercentage(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            getApplication<Application>().registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0
    }

    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(batteryReceiver)
    }
}

data class Arrival(
    val id: Long,
    val rank: Int,
    val duration: String,
    val arrivalTime: String,
    val sailNumber: String = "",
    val isExcluded: Boolean = false,
    val isClassified: Boolean = true
)
