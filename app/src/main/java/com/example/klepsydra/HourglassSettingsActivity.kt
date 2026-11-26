package com.example.klepsydra

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HourglassSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("klepsydra_prefs", Context.MODE_PRIVATE)
        val currentMinutes = prefs.getInt("duration_minutes", 1)

        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
            setBackgroundColor(android.graphics.Color.parseColor("#090D16"))
        }

        val titleView = TextView(this).apply {
            text = "Wygaszacz Klepsydra"
            textSize = 24f
            setTextColor(android.graphics.Color.WHITE)
        }

        val labelView = TextView(this).apply {
            text = "Czas przesypywania (w minutach, 1–120):"
            textSize = 16f
            setTextColor(android.graphics.Color.parseColor("#CBD5E1"))
            setPadding(0, 32, 0, 12)
        }

        val minutesInput = EditText(this).apply {
            setText(currentMinutes.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#1E293B"))
            setPadding(24, 20, 24, 20)
        }

        val saveButton = Button(this).apply {
            text = "Zapisz czas"
            setOnClickListener {
                val parsed = minutesInput.text.toString().toIntOrNull()
                if (parsed == null || parsed !in 1..120) {
                    Toast.makeText(
                        this@HourglassSettingsActivity,
                        "Podaj liczbę od 1 do 120",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    prefs.edit().putInt("duration_minutes", parsed).apply()
                    Toast.makeText(
                        this@HourglassSettingsActivity,
                        "Zapisano: $parsed min",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val openDreamsButton = Button(this).apply {
            text = "Otwórz ustawienia wygaszacza Androida"
            setOnClickListener {
                try {
                    startActivity(Intent(Settings.ACTION_DREAM_SETTINGS))
                } catch (_: Exception) {
                    startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                }
            }
        }

        container.addView(titleView)
        container.addView(labelView)
        container.addView(minutesInput)
        container.addView(saveButton)
        container.addView(openDreamsButton)

        setContentView(container)
    }
}
