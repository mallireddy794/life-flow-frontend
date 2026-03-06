package com.simats.lifeflow

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HospitalChatActivity : AppCompatActivity() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvMessages: RecyclerView
    private lateinit var etInput: EditText
    
    private var senderId: Int = -1 
    private var receiverId: Int = -1 
    private var messageList = mutableListOf<ChatMessage>()
    
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            fetchChatHistory()
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hospital_chat)

        // Prioritize SENDER_ID from Intent
        senderId = intent.getIntExtra("SENDER_ID", -1)

        // Fallback to SharedPreferences
        if (senderId == -1) {
            val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            senderId = sharedPrefs.getInt("user_id", -1)
        }

        receiverId = intent.getIntExtra("RECEIVER_ID", -1)
        val receiverName = intent.getStringExtra("RECEIVER_NAME") ?: "Hospital"
        
        Log.d("HospitalChatActivity", "Me: $senderId, Target: $receiverId")

        if (senderId == -1 || receiverId == -1) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tv_hospital_name).text = receiverName

        etInput = findViewById(R.id.et_message)
        rvMessages = findViewById(R.id.rv_messages)

        chatAdapter = ChatAdapter(senderId)
        rvMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvMessages.adapter = chatAdapter

        findViewById<AppCompatImageButton>(R.id.btn_send).setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
        
        fetchChatHistory()
    }

    private fun sendMessage(text: String) {
        val msg = ChatMessage(sender_id = senderId, receiver_id = receiverId, message = text)
        
        // Optimistic update
        val tempMsg = msg.copy(created_at = "Sending...")
        messageList.add(tempMsg)
        chatAdapter.setMessages(ArrayList(messageList))
        rvMessages.scrollToPosition(messageList.size - 1)
        
        etInput.text.clear()

        ApiClient.instance.sendMessage(msg).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    fetchChatHistory()
                } else {
                    Log.e("HospitalChatActivity", "Send failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@HospitalChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("HospitalChatActivity", "Send error: ${t.message}")
                Toast.makeText(this@HospitalChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun fetchChatHistory() {
        if (senderId == -1 || receiverId == -1) {
            Log.e("HospitalChatActivity", "Invalid IDs - senderId: $senderId, receiverId: $receiverId")
            return
        }
        
        Log.d("HospitalChatActivity", "Fetching messages for user1=$senderId, user2=$receiverId")
        
        ApiClient.instance.getChatHistory(senderId, receiverId).enqueue(object : Callback<List<ChatMessage>> {
            override fun onResponse(call: Call<List<ChatMessage>>, response: Response<List<ChatMessage>>) {
                Log.d("HospitalChatActivity", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val history = response.body() ?: emptyList()
                    Log.d("HospitalChatActivity", "Fetched ${history.size} messages")
                    
                    // Update if message count is different
                    if (history.size != messageList.size || (messageList.isEmpty() && history.isNotEmpty())) {
                        messageList = history.toMutableList()
                        chatAdapter.setMessages(messageList)
                        
                        rvMessages.post {
                            if (messageList.isNotEmpty()) {
                                rvMessages.scrollToPosition(messageList.size - 1)
                            }
                        }
                    }
                } else {
                    Log.e("HospitalChatActivity", "Response not successful: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<List<ChatMessage>>, t: Throwable) {
                Log.e("HospitalChatActivity", "API call failed: ${t.message}", t)
            }
        })
    }
}
