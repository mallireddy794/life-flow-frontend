package com.blooddonation.app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatbotActivity : AppCompatActivity() {

    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var llSuggestions: LinearLayout
    private lateinit var adapter: ChatbotAdapter

    // Read from SharedPreferences — same keys used by DonorDashboardActivity etc.
    private var userId: Int = -1
    private var userRole: String = "patient"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        // ── Load user session (same pattern as all other activities) ──
        val appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userId   = appPrefs.getInt("user_id", -1)
        userRole = appPrefs.getString("user_role", "patient")?.lowercase() ?: "patient"
        Log.d("ChatbotActivity", "Loaded session: userId=$userId role=$userRole")

        // ── Views ──
        rvMessages     = findViewById(R.id.rvMessages)
        etMessage      = findViewById(R.id.etMessage)
        btnSend        = findViewById(R.id.btnSend)
        llSuggestions  = findViewById(R.id.llSuggestions)

        setupRecyclerView()
        setupSuggestions()

        btnSend.setOnClickListener {
            val msg = etMessage.text.toString().trim()
            if (msg.isNotEmpty()) {
                sendMessage(msg)
                etMessage.setText("")
            }
        }

        // ── Welcome message (no API needed) ──
        addBotMessage(
            "Hello! I'm your LifeFlow Assistant 🩸\n\n" +
            "I can help with blood donation, blood requests, donor eligibility, " +
            "emergency support, hospital guidance, and app usage. " +
            "How can I help you today?"
        )
    }

    // ─────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = ChatbotAdapter()
        rvMessages.adapter = adapter
        rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
    }

    private fun setupSuggestions(chips: List<String> = listOf(
        "Register as Donor", "Request Blood", "Emergency Help",
        "Blood Groups", "Donor Eligibility", "Nearby Donors"
    )) {
        llSuggestions.removeAllViews()
        chips.forEach { label ->
            val btn = Button(this).apply {
                text            = label
                isAllCaps       = false
                textSize        = 12f
                setTextColor(resources.getColor(android.R.color.black, theme))
                setBackgroundResource(R.drawable.bg_suggestion_chip)
                val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = 8.dpToPx() }
                layoutParams = lp
                setPadding(16.dpToPx(), 4.dpToPx(), 16.dpToPx(), 4.dpToPx())
                setOnClickListener { sendMessage(label) }
            }
            llSuggestions.addView(btn)
        }
    }

    // ─────────────────────────────────────────────
    // Messaging
    // ─────────────────────────────────────────────

    private fun sendMessage(text: String) {
        addUserMessage(text)

        val req = ChatbotRequest(
            userId  = if (userId != -1) userId else null,
            role    = userRole,
            message = text
        )

        Log.d("ChatbotActivity", "Sending to /chatbot/message → $req")

        ApiClient.instance.sendChatbotMessage(req).enqueue(object : Callback<ChatbotResponse> {
            override fun onResponse(call: Call<ChatbotResponse>, response: Response<ChatbotResponse>) {
                Log.d("ChatbotActivity", "HTTP ${response.code()} body=${response.body()} err=${response.errorBody()?.string()}")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Update suggestion chips from backend's quick_replies
                    val qr = body.quickReplies
                    if (!qr.isNullOrEmpty()) setupSuggestions(qr)
                    addBotMessage(body.reply)
                } else {
                    addBotMessage("⚠️ Server returned an error (${response.code()}). Please try again.")
                }
            }

            override fun onFailure(call: Call<ChatbotResponse>, t: Throwable) {
                Log.e("ChatbotActivity", "API call failed: ${t.message}", t)
                addBotMessage(
                    "⚠️ Unable to reach LifeFlow Assistant.\n\n" +
                    "Make sure your backend is running and the server is reachable."
                )
            }
        })
    }

    private fun addUserMessage(text: String) {
        val ts = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        adapter.addMessage(ChatbotMessage(text, isUser = true, timestamp = ts))
        scrollToBottom()
    }

    private fun addBotMessage(text: String) {
        val ts = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        adapter.addMessage(ChatbotMessage(text, isUser = false, timestamp = ts))
        scrollToBottom()
    }

    private fun scrollToBottom() {
        rvMessages.post {
            if (adapter.itemCount > 0)
                rvMessages.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun Int.dpToPx(): Int =
        (this * resources.displayMetrics.density + 0.5f).toInt()
}
