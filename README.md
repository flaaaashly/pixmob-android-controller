# Pixmob Android IR Controller

A native Android application written in Kotlin that uses the built-in hardware IR blaster (`ConsumerIrManager`) to transmit Pixmob infrared instructions. This adapts the timing and carrier frequency logic from web-based controllers for native Android hardware.

## Requirements
- An Android device with a built-in hardware IR blaster (e.g., select Xiaomi, POCO, or older Samsung devices).
- Android Studio (Koala or newer recommended).
- Minimum SDK: 21 (Android 5.0) or higher.
- Target SDK: 34+

## Usage
1. Open this project in Android Studio.
2. Build and run the app on an IR-equipped Android device.
3. Tap **"Check IR Hardware & Frequencies"** to verify compatibility.
4. Tap **"Transmit Pixmob Instruction"** to send the raw signal pattern.
