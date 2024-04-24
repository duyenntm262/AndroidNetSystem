package com.example.netsystem

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var layoutParams: WindowManager.LayoutParams

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOpenYoutube: Button = findViewById(R.id.btnOpenYoutube)

        // Thiết lập sự kiện click cho Button
        btnOpenYoutube.setOnClickListener {
            openYoutubeApp()
        }

        val screenOffReceiver = ScreenOffReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, intentFilter)

        val screenTimeoutEditText: EditText = findViewById(R.id.screenTimeoutEditText)
        val timeoutTextView: TextView = findViewById(R.id.timeoutTextView)

        screenTimeoutEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val timeoutSeconds = screenTimeoutEditText.text.toString().toIntOrNull()
                if (timeoutSeconds != null && timeoutSeconds > 0) {
                    setScreenTimeout(timeoutSeconds)
                    true
                } else {
                    Toast.makeText(this, "Please enter a valid timeout value", Toast.LENGTH_SHORT).show()
                    false
                }
            } else {
                false
            }
        }

        fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(screenOffReceiver)
        }

        screenTimeoutEditText.setText("15")

        val btn: Button = findViewById(R.id.lanSettingsButton)
        btn.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        }


        val inShiftJis = byteArrayOf(
            -125, 67, -125, -109, -125, 94, -127, 91, -125, 108, -125, 98, -125, 103, -126,
            -16, -126, -32, -126, -63, -126, -58, -119, -11, -109, 75, -126, -55
        )

        val decodedShiftJis = String(inShiftJis, Charset.forName("Shift-JIS"))
        Log.d("ShiftJISDecoder", "Decoded Shift-JIS: $decodedShiftJis")

        val spinnerBrightness: Spinner = findViewById(R.id.spinner)
        val editText: EditText = findViewById(R.id.editText)

        /******************************************* DISABLE KEYBOARD ************************************/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.inputType = EditorInfo.TYPE_NULL
        }

        /******************************************* BACKLIGHT ******************************************/
        ArrayAdapter.createFromResource(
            this,
            R.array.backlight_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBrightness.adapter = adapter
        }

        layoutParams = window.attributes

        // Call the function to check and request WRITE_SETTINGS permission
        checkWriteSettingsPermission()

        spinnerBrightness.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedBrightness = parent?.getItemAtPosition(position).toString()
                var brightnessValue = 1.0f // Default brightness

                // Set brightness based on selected option
                when (selectedBrightness) {
                    "Normal" -> brightnessValue = 0.5f
                    "Bright" -> brightnessValue = 0.75f
                    "Brightest" -> brightnessValue = 1.0f
                }

                // Apply brightness changes
                setBrightness(brightnessValue)

                // Show a Toast message
                Toast.makeText(this@MainActivity, "Brightness set to $selectedBrightness", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


        /************************************* READING VOLUME ************************************/
        val readingVolumeSpinner: Spinner = findViewById(R.id.readingVolumeSpinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.reading_volume_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            readingVolumeSpinner.adapter = adapter
        }

        readingVolumeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedVolume = parent?.getItemAtPosition(position).toString()

                // Set volume based on selected option
                when (selectedVolume) {
                    "Off" -> setReadingVolume(0) // Set volume to 0
                    "Low" -> setReadingVolume(4) // Set volume to 20%
                    "Medium" -> setReadingVolume(7) // Set volume to 50%
                    "High" -> setReadingVolume(15) // Set volume to 100%
                }

                // Show a Toast message
                Toast.makeText(this@MainActivity, "Reading volume set to $selectedVolume", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


        /***************************************SYSTEM VOLUME**********************************/
        val systemVolumeSpinner: Spinner = findViewById(R.id.systemVolumeSpinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.system_volume_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            systemVolumeSpinner.adapter = adapter
        }

        systemVolumeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedVolume = parent?.getItemAtPosition(position).toString()

                // Set system volume based on selected option
                when (selectedVolume) {
                    "Off" -> setSystemVolume(0) // Set volume to 0
                    "Low" -> setSystemVolume(4) // Set volume to 20%
                    "Medium" -> setSystemVolume(7) // Set volume to 50%
                    "High" -> setSystemVolume(15) // Set volume to 100%
                }

                // Show a Toast message
                Toast.makeText(this@MainActivity, "System volume set to $selectedVolume", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


        /************************************** SYSTEM VIBRATION ***********************************/
        // Setup system vibration spinner
        val vibrationSpinner: Spinner = findViewById(R.id.systemVibrationSpinner)

        ArrayAdapter.createFromResource(
            this,
            R.array.system_vibration_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            vibrationSpinner.adapter = adapter
        }

        vibrationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position).toString()
                val enableVibration = selectedOption == "On"

                // Set system vibration based on selected option
                setSystemVibration(enableVibration)

                // Show a Toast message
                Toast.makeText(this@MainActivity, "System vibration turned $selectedOption", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun openYoutubeApp() {
        val intent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")

        if (intent != null) {
            startActivity(intent)
        } else {
            val youtubeUrl = "https://www.youtube.com"
            val browserIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(youtubeUrl))
            startActivity(browserIntent)
        }
    }

    private fun setSystemVolume(volume: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Set system volume
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, volume, 0)
    }
    fun performLogout() {

    }

    inner class ScreenOffReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                performLogout()
            }
        }
    }
    private fun setScreenTimeout(timeoutSeconds: Int) {
        val contentResolver = contentResolver

        if (Settings.System.canWrite(this)) {
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeoutSeconds * 1000)

            Toast.makeText(this, "Screen timeout set to $timeoutSeconds seconds", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please grant permission to modify system settings", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setReadingVolume(volume: Int) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Check and request MODIFY_AUDIO_SETTINGS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                startActivity(intent)
                return
            }
        }

        // Set volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
    }

    private fun setSystemVibration(enable: Boolean) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val ringerMode = if (enable) AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_NORMAL
        audioManager.ringerMode = ringerMode
    }

    // Function to check and request WRITE_SETTINGS permission
    private fun checkWriteSettingsPermission() {
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please grant permission to modify system settings to adjust brightness", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to set the brightness of the screen
    private fun setBrightness(brightness: Float) {
        val contentResolver = contentResolver

        // Check if the application has permission to change brightness settings
        if (Settings.System.canWrite(this)) {
            // Set the brightness of the device
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, (brightness * 255).toInt())

            // Update brightness setting for the current window
            val layoutParams = window.attributes
            layoutParams.screenBrightness = brightness
            window.attributes = layoutParams
        } else {
            // If permission is not granted, request permission from the user
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please grant permission to modify system settings to adjust brightness", Toast.LENGTH_SHORT).show()
        }
    }
}
