package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView

class BlockedIPS : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked_ips)

        val backIcon=findViewById<ImageView>(R.id.backButton)

//        val blockedIps = intent.getStringArrayListExtra("blocked_ips")
//        val blocked_User=intent.getStringArrayListExtra("Blocked_Username")
//        val blockedIpList: List<String> = blockedIps.orEmpty()
//        val blocked_UserList :List<String> =blocked_User.orEmpty()

        //list of blocked ips from database
        val dbHelper = ChatDatabaseHelper(this)
        val blockDataList = dbHelper.getAllBlockData()

        val blockedIpList: ArrayList<String> = ArrayList<String>()
        val blockedUserList: ArrayList<String> = ArrayList<String>()

        for (blockData in blockDataList) {
            blockedIpList.add(blockData.ip)
            blockedUserList.add(blockData.username)
            // display here
            Log.d("BlockData", "IP: $blockData.ip, Username: $blockData.username")
        }


        val blockedIpRecyclerView: RecyclerView = findViewById(R.id.blocked_ip_recycler_view)
        val adapter = BlockedIpAdapter(this, blockedIpList,blockedUserList)

        blockedIpRecyclerView.adapter = adapter

        // Set layout manager
        val layoutManager = LinearLayoutManager(this)
        blockedIpRecyclerView.layoutManager = layoutManager

        backIcon.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
            finish()
        }

    }
}
