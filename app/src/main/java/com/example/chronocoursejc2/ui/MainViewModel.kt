package com.example.chronocoursejc2.ui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

enum class Procedure(val seconds: Long, val label: String) {
    PROC_6510(360, "6 5 1 0"),
    PROC_3210(180, "3 2 1 0"),
    NONE(0, "Sans compte à rebours")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

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
    }

    private fun formatTimeFull(time: Date): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(time)
    }

    fun selectProcedure(procedure: Procedure) {
        _selectedProcedure.value = procedure
        _showProcedureDialog.value = false
        _raceState.value = RaceState.READY
        _remainingTime.value = procedure.seconds
        
        val estDeparture = System.currentTimeMillis() + (procedure.seconds * 1000)
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
        departureTime = System.currentTimeMillis() + (procedure.seconds * 1000)
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
        val isBeep = (secondsLeft in 300..312) || (secondsLeft in 60..72) || (secondsLeft in 0..12) ||
                     (secondsLeft in 120..132 && (_selectedProcedure.value?.seconds ?: 0) <= 180)
        
        if (isBeep) {
            playBeep()
        }
    }

    private fun playBeep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    fun recordArrival() {
        if (_raceState.value != RaceState.RUNNING) return
        playBeep()
        
        val now = System.currentTimeMillis()
        val durationMillis = now - startTime
        val arrivalTime = formatTimeFull(Date(now))
        val rank = _arrivals.value.size + 1
        
        val newArrival = Arrival(
            rank = rank,
            duration = formatDuration(durationMillis),
            arrivalTime = arrivalTime
        )
        _arrivals.value = listOf(newArrival) + _arrivals.value
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
                append("ApplicationChronocoursejc2\n")
                val startDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(startTime))
                append("Arrivees de la course dont le depart a eu lieu\n")
                append("le $startDateStr à $startFormattedTime\n")
                append(String.format(Locale.getDefault(), "%-6s  %-12s  %-12s\n", "Rang", "Duree", "Heure"))
                _arrivals.value.sortedBy { it.rank }.forEach { arrival ->
                    append(String.format(Locale.getDefault(), "%03d   %-12s  %-12s\n", arrival.rank, arrival.duration, arrival.arrivalTime))
                }
            }
            _lastSavedFileContent.value = content

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = getApplication<Application>().contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { it.write(content.toByteArray()) }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(content.toByteArray()) }
            }
            
            _showPostRaceDialog.value = true
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
        super.onCleared()
        getApplication<Application>().unregisterReceiver(batteryReceiver)
    }
}

data class Arrival(
    val rank: Int,
    val duration: String,
    val arrivalTime: String
)
