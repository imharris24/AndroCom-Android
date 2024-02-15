package com.application.androcom

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class IPAdapter(private val context : Activity,private val arrayList : ArrayList<UserIP>) :ArrayAdapter<UserIP>(context,
                R.layout.contact_item,arrayList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflator : LayoutInflater = LayoutInflater.from(context)
        val view :  View = inflator.inflate(R.layout.contact_item,null)

        val username : TextView =view.findViewById(R.id.username)
        val userIP : TextView =view.findViewById(R.id.IPadd)

        username.text =arrayList[position].name
        userIP.text=arrayList[position].IP

        return view
    }
}