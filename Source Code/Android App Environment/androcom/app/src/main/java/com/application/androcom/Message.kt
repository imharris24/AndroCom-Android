package com.application.androcom

import org.json.JSONObject

data class Message(val text: String, val isSent: Boolean, val timestamp: Long = System.currentTimeMillis()) {

    fun toJson(): String {
        val map: MutableMap<String, Any?> = HashMap()
        map["text"] = text
        map["isSent"] = isSent
        map["timestamp"] = timestamp
        return JSONObject(map).toString()
    }
}
