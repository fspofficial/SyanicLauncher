package com.movtery.pojavzh.utils.http

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class CallUtils(
    private val listener: CallbackListener,
    private val url: String,
    private val token: String?
) {
    fun start() {
        val client = OkHttpClient()
        val url = Request.Builder().url(this.url)
        if (token != null) {
            url.addHeader("Token", token)
        }
        val request = url.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure(call, e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                listener.onResponse(call, response)
            }
        })
    }

    interface CallbackListener {
        fun onFailure(call: Call?, e: IOException?)
        @Throws(IOException::class)
        fun onResponse(call: Call?, response: Response?)
    }
}