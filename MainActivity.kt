package com.indue.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var output: TextView
    private lateinit var status: TextView
    private lateinit var tts: TextToSpeech
    private var recognizer: SpeechRecognizer? = null
    private var wakeMode = false
    private val requestAudio = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(5, 17, 34)
        window.navigationBarColor = Color.rgb(5, 17, 34)
        tts = TextToSpeech(this, this)
        buildUi()
    }

    private fun buildUi() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 36, 28, 28)
            setBackgroundColor(Color.rgb(5, 17, 34))
        }
        page.addView(TextView(this).apply {
            text = "INDUE"; textSize = 30f; setTextColor(Color.WHITE)
            gravity = Gravity.CENTER; letterSpacing = 0.25f
        })
        page.addView(TextView(this).apply {
            text = "PERSONAL ANDROID AI ASSISTANT"; textSize = 11f
            setTextColor(Color.rgb(111, 194, 235)); gravity = Gravity.CENTER
        })
        status = TextView(this).apply {
            text = "READY • Tap the mic or enable wake-word mode"
            textSize = 13f; setTextColor(Color.rgb(92, 226, 225))
            gravity = Gravity.CENTER; setPadding(0, 24, 0, 18)
        }
        output = TextView(this).apply {
            text = "Namaskar! मी INDUE आहे. मला मराठी, हिंदी किंवा English मध्ये बोला."
            textSize = 17f; setTextColor(Color.WHITE); setPadding(18, 22, 18, 22)
            setBackgroundColor(Color.rgb(14, 37, 62))
        }
        page.addView(status)
        page.addView(ScrollView(this).apply { addView(output) },
            LinearLayout.LayoutParams(-1, 0, 1f))
        page.addView(makeButton("🎙  TALK TO INDUE") { startOneShotListening() })
        page.addView(makeButton("◉  ENABLE / DISABLE HEY INDUE") {
            if (!hasAudioPermission()) requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
            else {
                wakeMode = !wakeMode
                status.text = if (wakeMode) "WAKE MODE ON • Keep INDUE open" else "WAKE MODE OFF"
                if (wakeMode) listenForWakeWord() else recognizer?.cancel()
            }
        })
        val command = EditText(this).apply {
            hint = "Type: call 9876543210 / open camera"
            setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); textSize = 15f
            setPadding(14, 10, 14, 10); setBackgroundColor(Color.rgb(17, 43, 69))
        }
        page.addView(command)
        page.addView(makeButton("RUN COMMAND") { handleCommand(command.text.toString()) })
        page.addView(TextView(this).apply {
            text = "Calls open the dialer for your confirmation. Android may require unlock for protected actions. Wake-word listening works only while this app is active in this starter build."
            textSize = 11f; setTextColor(Color.rgb(145, 169, 192)); setPadding(2, 18, 2, 0)
        })
        setContentView(page)
    }

    private fun makeButton(label: String, action: () -> Unit) =
        Button(this).apply {
            text = label; textSize = 13f; setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(18, 111, 171))
            setOnClickListener { action() }
        }

    private fun hasAudioPermission() =
        checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun startOneShotListening() {
        if (!hasAudioPermission()) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio); return
        }
        startRecognition(false)
    }

    private fun listenForWakeWord() {
        if (wakeMode && hasAudioPermission()) startRecognition(true)
    }

    private fun startRecognition(isWake: Boolean) {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            respond("या फोनमध्ये speech recognition service उपलब्ध नाही."); return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                status.text = if (isWake) "LISTENING FOR HEY INDUE" else "LISTENING…"
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { status.text = "PROCESSING…" }
            override fun onError(error: Int) {
                if (wakeMode && isWake) status.postDelayed({ listenForWakeWord() }, 1000)
                else status.text = "READY"
            }
            override fun onResults(results: Bundle?) {
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull().orEmpty()
                if (isWake) {
                    val lower = spoken.lowercase()
                    if (lower.contains("hey indue") || lower.contains("hi indue") ||
                        lower.contains("हे इंड्यू") || lower.contains("इंड्यू")) {
                        val command = spoken.replace(Regex("(?i)hey\\s+indue|hi\\s+indue|हे\\s*इंड्यू|इंड्यू"), "").trim()
                        if (command.isNotBlank()) handleCommand(command)
                        else { respond("Yes, मी ऐकत आहे. सांगा."); startOneShotListening() }
                    }
                    if (wakeMode) status.postDelayed({ listenForWakeWord() }, 1200)
                } else handleCommand(spoken)
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "mr-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "mr-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        recognizer?.startListening(intent)
    }

    private fun handleCommand(raw: String) {
        val c = raw.trim().lowercase(Locale.ROOT)
        if (c.isBlank()) { respond("कृपया command सांगा."); return }
        when {
            c.contains("help") || c.contains("मदत") ->
                respond("म्हणा: call 9876543210, open camera, open YouTube, open settings, किंवा time काय आहे.")
            c.contains("time") || c.contains("वेळ") || c.contains("वाजले") ->
                respond("आत्ता " + java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()).format(java.util.Date()) + " वाजले आहेत.")
            c.contains("date") || c.contains("तारीख") ->
                respond("आज " + java.text.SimpleDateFormat("dd MMMM yyyy", Locale("mr", "IN")).format(java.util.Date()) + " आहे.")
            c.contains("camera") || c.contains("कॅमेरा") || c.contains("फोटो") -> {
                respond("Camera उघडत आहे.")
                launchIntent(Intent("android.media.action.IMAGE_CAPTURE"))
            }
            c.contains("call") || c.contains("कॉल") || c.contains("फोन कर") || c.contains("डायल") -> {
                val number = Regex("""\+?\d[\d\s-]{5,}\d""").find(raw)?.value?.replace(Regex("""[\s-]"""), "")
                if (number == null) respond("कृपया नंबरसह सांगा. उदाहरण: call 9876543210.")
                else {
                    respond("Dialer उघडत आहे. नंबर तपासून Call दाबा.")
                    launchIntent(Intent(Intent.ACTION_DIAL).apply { data = android.net.Uri.parse("tel:$number") })
                }
            }
            c.contains("youtube") || c.contains("यूट्यूब") ->
                openAppOrWeb("com.google.android.youtube", "https://www.youtube.com")
            c.contains("chrome") || c.contains("browser") || c.contains("ब्राउजर") ->
                openAppOrWeb("com.android.chrome", "https://www.google.com")
            c.contains("settings") || c.contains("सेटिंग") ->
                launchIntent(Intent(android.provider.Settings.ACTION_SETTINGS))
            c.contains("flashlight") || c.contains("torch") || c.contains("टॉर्च") ->
                respond("Flashlight control पुढील native module मध्ये जोडता येईल.")
            else -> respond("हा command अजून जोडलेला नाही. Help म्हणा.")
        }
    }

    private fun openAppOrWeb(packageName: String, url: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) launchIntent(launch)
        else launchIntent(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)))
    }

    private fun launchIntent(intent: Intent) {
        try { startActivity(intent) }
        catch (_: Exception) { respond("ही action या फोनवर उपलब्ध नाही किंवा Android ने block केली आहे.") }
    }

    private fun respond(message: String) {
        output.text = message; status.text = "INDUE • READY"
        if (::tts.isInitialized) tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "indue_reply")
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) tts.language = Locale("mr", "IN")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudio && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            if (wakeMode) listenForWakeWord() else startOneShotListening()
        } else if (requestCode == requestAudio) respond("Microphone permission शिवाय voice control चालणार नाही.")
    }

    override fun onDestroy() {
        wakeMode = false; recognizer?.destroy()
        if (::tts.isInitialized) tts.shutdown()
        super.onDestroy()
    }
}
