package com.application.androcom

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class BlockedIpAdapter(private val context: Context, private val blockedIps: MutableList<String>,private val blocked_User:MutableList<String>) :
    RecyclerView.Adapter<BlockedIpAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ipAddressTextView: TextView = itemView.findViewById(R.id.ip_address)
        val blockedUserTextView:TextView=itemView.findViewById(R.id.blockedUser)
        val unblockButton: ImageButton = itemView.findViewById(R.id.unblock_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_blocked_ip, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ipAddress = blockedIps[position]
        val BlockedUser =blocked_User[position]
        holder.blockedUserTextView.text=BlockedUser
        holder.ipAddressTextView.text = ipAddress

        holder.unblockButton.setOnClickListener {
            ChatDatabaseHelper(context).removeIPFromBlock(ipAddress)
            Log.d("BlockedIpAdapter","Removed ${ipAddress} From Blocked List")
            // Remove the item from the data source
            blockedIps.removeAt(position)
            blocked_User.removeAt(position)

            // Notify the adapter about the item removal
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    override fun getItemCount(): Int {
        return blockedIps.size
    }
}
