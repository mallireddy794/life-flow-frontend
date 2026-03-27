package com.simats.lifeflow

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class NearbyRequestAdapter(private var requests: List<NearbyRequest>) :
    RecyclerView.Adapter<NearbyRequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBloodGroup: TextView = view.findViewById(R.id.tv_blood_group)
        val tvHospital: TextView = view.findViewById(R.id.tv_hospital_name)
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvUrgency: TextView = view.findViewById(R.id.tv_urgency)
        val tvUnits: TextView = view.findViewById(R.id.tv_units)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.tvBloodGroup.text = request.bloodGroup
        holder.tvHospital.text = request.hospitalName
        holder.tvLocation.text = request.location
        holder.tvUrgency.text = request.urgency
        holder.tvUnits.text = "${request.units} Units Required"

        if (request.urgency.lowercase() == "urgent") {
            holder.tvUrgency.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.bright_red))
            holder.tvUrgency.setBackgroundResource(R.drawable.bg_status_urgent)
        } else {
            holder.tvUrgency.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text_gray))
            holder.tvUrgency.setBackgroundResource(R.drawable.bg_status_pending)
        }

        // Handle Click to open Eligibility Check
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EligibilityCheckActivity::class.java)
            intent.putExtra("FROM_ACCEPT", true)
            intent.putExtra("RECEIVER_ID", request.patientId)
            intent.putExtra("REQUEST_ID", request.id)
            intent.putExtra("RECEIVER_NAME", request.hospitalName)
            intent.putExtra("BLOOD_GROUP", request.bloodGroup)
            intent.putExtra("UNITS", request.units)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = requests.size

    fun updateList(newList: List<NearbyRequest>) {
        requests = newList
        notifyDataSetChanged()
    }
}
