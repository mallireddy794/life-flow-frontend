package com.simats.lifeflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatInboxActivity : BaseActivity() {

    private lateinit var rvInbox: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: InboxAdapter
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_inbox)

        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userId = sharedPrefs.getInt("user_id", -1)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        rvInbox = findViewById(R.id.rv_inbox)
        tvEmpty = findViewById(R.id.tv_empty_inbox)

        btnBack.setOnClickListener { finish() }

        adapter = InboxAdapter { inboxItem ->
            val otherUser = inboxItem.other_user
            if (otherUser != null && otherUser.id != null) {
                val intent = Intent(this, PatientChatActivity::class.java)
                intent.putExtra("SENDER_ID", userId)
                intent.putExtra("RECEIVER_ID", otherUser.id)
                intent.putExtra("RECEIVER_NAME", otherUser.name ?: "Unknown")
                intent.putExtra("CHAT_STATUS", "CONNECTED")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Cannot open chat: User info missing", Toast.LENGTH_SHORT).show()
            }
        }
        rvInbox.adapter = adapter

        fetchInbox()
    }

    private fun fetchInbox() {
        if (userId == -1) return

        ApiClient.instance.getChatInbox(userId).enqueue(object : Callback<List<InboxItem>> {
            override fun onResponse(call: Call<List<InboxItem>>, response: Response<List<InboxItem>>) {
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    if (items.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        rvInbox.visibility = View.GONE
                    } else {
                        tvEmpty.visibility = View.GONE
                        rvInbox.visibility = View.VISIBLE
                        adapter.setItems(items)
                    }
                }
            }

            override fun onFailure(call: Call<List<InboxItem>>, t: Throwable) {
                Toast.makeText(this@ChatInboxActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
