package com.simats.lifeflow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NearbyDonorListAdapter(
    private val donorList: List<NearbyDonor>,
    private val onChatClick: (NearbyDonor) -> Unit
) : RecyclerView.Adapter<NearbyDonorListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_donor_name)
        val tvInfo: TextView = view.findViewById(R.id.tv_donor_info)
        val btnChat: Button = view.findViewById(R.id.btn_chat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donor_list_simple, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val donor = donorList[position]
        holder.tvName.text = donor.name ?: "Unknown Donor"
        
        val distStr = if (donor.distanceKm != null) String.format("%.1f km", donor.distanceKm) else ""
        holder.tvInfo.text = "Blood Group: ${donor.bloodGroup ?: "?"} · $distStr"

        holder.btnChat.setOnClickListener { onChatClick(donor) }
        holder.itemView.setOnClickListener { onChatClick(donor) }
    }

    override fun getItemCount() = donorList.size
}
