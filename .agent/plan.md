# Project Plan

Chronocoursejc2: An Android application for timing races.
Features:
- Top Bar: Real-time clock (HH:mm:ss) and battery percentage.
- Timing Display: Countdown timer before start, Stopwatch after start. Format: "MM m SS s avant le départ" or "HH h MM m SS s depuis le départ".
- Arrival List: Scrollable list of runners' arrival times (Rank, Duration, Arrival Time). Most recent on top.
- Buttons: "Arrivée d'un coureur" (left) and "Démarrer/Arrêter" (right).
- Physical Button Mapping: Volume Down maps to "Démarrer" (if not started) or "Arrivée d'un coureur" (if started).
- Start Procedures:
  - "6 5 1 0": 6 min countdown with beeps at specific intervals (around 5m, 1m, and 0m marks).
  - "3 2 1 0": 3 min countdown with beeps at specific intervals (around 2m, 1m, and 0m marks).
- Stop & Save: Confirm stop, save text file to Downloads (format: App Name / Arrivals at [Start Time], then list of results), show toast, then dialog to Quit or Restart.
- App Settings: Keep screen on, 100% brightness, lock Portrait orientation.
- UI: Material Design 3, vibrant color scheme, Edge-to-Edge display.

## Project Brief

# Chronocoursejc2 - Project Brief

Chronocoursejc2 is a high-precision race timing application designed for Android. It provides race officials with a reliable tool to manage countdown starts, track runner arrivals in real-time using physical or on-screen triggers, and export formatted results directly to the device's storage.

## Features

*   **Precision Race Start Procedures:** Supports "6 5 1 0" and "3 2 1 0" countdown sequences with synchronized audio beeps at critical intervals to signal the start of the race.
*   **Dual-Input Arrival Tracking:** Record runner arrival times using a large on-screen button or the physical **Volume Down** key for tactile feedback, automatically capturing rank, duration, and time of day.
*   **Real-Time Status Dashboard:** A persistent header displaying a synchronized HH:mm:ss clock and battery percentage, ensuring the operator is aware of device status throughout the event.
*   **Automated Results Export:** Upon stopping the timer, the app generates a formatted text file in the `Downloads` folder containing the start time and a full list of runner results for easy sharing.
*   **Optimized Race Environment:** Automatically forces the screen to stay on at 100% brightness and locks the orientation to portrait to prevent accidental UI shifts or sleep during timing.


## High-Level Technical Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose with **Material Design 3** (M3)
*   **Navigation:** **Jetpack Navigation 3** (state-driven architecture)
*   **Layout Strategy:** **Compose Material Adaptive** library for edge-to-edge and responsive UI components
*   **Concurrency:** Kotlin Coroutines for high-accuracy timing loops and background file I/O
*   **Audio:** SoundPool for low-latency countdown beeps
*   **System Integration:** 
    *   `BatteryManager` for real-time power monitoring
    *   `PowerManager` / `Window` flags for screen persistence
    *   `MediaStore` API for exporting results to the Downloads folder

## Implementation Steps

### Task_1_Foundation_UI: Initialize Material 3 theme with vibrant colors, enable Edge-to-Edge, and build the main screen layout including the Top Bar (clock/battery), Timing Display, and Arrival List.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Vibrant M3 theme applied
  - Edge-to-Edge display active
  - Top Bar shows real-time clock and battery
  - Arrival List and Timing Display UI components are visible
  - Project builds successfully
- **StartTime:** 2026-06-01 21:01:04 CEST

### Task_2_Race_Logic_Audio: Implement race start procedures (6-5-1-0 and 3-2-1-0 countdowns) with SoundPool audio beeps, and the stopwatch logic for recording runner arrivals.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Countdowns work with synchronized beeps
  - Stopwatch accurately tracks elapsed time
  - Arrival button records rank and time
  - State persists correctly in ViewModel

### Task_3_System_Integration_Export: Integrate physical Volume Down key handling, apply screen/brightness settings (100% brightness, Keep screen on, Portrait lock), and implement the text file export to the Downloads folder via MediaStore.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Volume Down triggers arrival/start
  - Screen remains on at full brightness in Portrait
  - Results export successfully to a .txt file in Downloads
  - App permissions handled correctly

### Task_4_Run_And_Verify: Perform final UI refinements, implement stop/save/quit dialogs, create an adaptive app icon, and verify the application's stability and alignment with all requirements.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Confirm stop and save dialogs work
  - Application does not crash
  - Build passes
  - All features meet the project brief
  - Existing tests pass
  - Critic agent verifies stability and UI alignment

