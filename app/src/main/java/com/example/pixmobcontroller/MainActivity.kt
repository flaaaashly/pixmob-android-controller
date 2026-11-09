package com.example.pixmobcontroller

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var irManager: ConsumerIrManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ConsumerIrManager system service
        irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager

        val btnCheckHardware = findViewById<Button>(R.id.btnCheckHardware)
        val btnTransmit = findViewById<Button>(R.id.btnTransmit)

        btnCheckHardware.setOnClickListener {
            checkIrCapabilities()
        }

        btnTransmit.setOnClickListener {
            playPixmobInstruction()
        }
    }

    private fun checkIrCapabilities() {
        if (!irManager.hasIrEmitter()) {
            Toast.makeText(this, "Device has NO built-in IR blaster!", Toast.LENGTH_LONG).show()
            return
        }

        val frequencyRanges = irManager.getCarrierFrequencies()
        if (frequencyRanges.isEmpty()) {
            Toast.makeText(this, "IR Emitter present, but no carrier ranges reported.", Toast.LENGTH_SHORT).show()
            return
        }

        var info = "IR Supported Ranges:\n"
        var supports38kHz = false

        for (range in frequencyRanges) {
            info += "${range.minFrequency} Hz - ${range.maxFrequency} Hz\n"
            if (38000 in range.minFrequency..range.maxFrequency) {
                supports38kHz = true
            }
        }

        if (supports38kHz) {
            info += "\n38 kHz (Pixmob frequency) is supported!"
        } else {
            info += "\nWarning: 38 kHz not explicitly reported."
        }

        Toast.makeText(this, info, Toast.LENGTH_LONG).show()
    }

    private fun playPixmobInstruction() {
        if (!irManager.hasIrEmitter()) {
            Toast.makeText(this, "Cannot transmit: No IR emitter found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Example instruction array equivalent to your web implementation data
        val sampleInst = intArrayOf(1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 0)

        val carrierFrequency = 38000 // 38 kHz carrier frequency
        val pperunit = 26.0
        
        // Calculate single unit duration in microseconds (~684.21 µs based on pperunit & carrier)
        val unitDurationUs = ((pperunit * 1_000_000.0) / carrierFrequency).toLong()

        // Convert instruction array into alternating Mark (ON) and Space (OFF) microsecond intervals
        val pattern = convertInstToIrPattern(sampleInst, unitDurationUs)

        try {
            irManager.transmit(carrierFrequency, pattern)
            Toast.makeText(this, "Pixmob IR Signal Transmitted!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Transmission failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun convertInstToIrPattern(inst: IntArray, unitDurationUs: Long): IntArray {
        if (inst.isEmpty()) return intArrayOf()

        val compressed = mutableListOf<Long>()
        
        // ConsumerIrManager requires patterns to strictly start with a Mark (ON).
        // If the instruction starts with 0 (Space), insert an initial 0-duration Mark.
        if (inst[0] == 0) {
            compressed.add(0L)
        }

        var currentCount = 0
        var currentState = inst[0] != 0

        for (value in inst) {
            val isMark = value != 0
            if (isMark == currentState) {
                currentCount++
            } else {
                compressed.add(currentCount * unitDurationUs)
                currentState = isMark
                currentCount = 1
            }
        }
        compressed.add(currentCount * unitDurationUs)

        // Ensure array length is even (pairs of Mark and Space durations)
        if (compressed.size % 2 != 0) {
            compressed.add(0L)
        }

        return compressed.map { it.toInt() }.toIntArray()
    }
}
