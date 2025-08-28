package com.lovigin.android.allbanks.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object TelegramApi {
    suspend fun sendMessage(
        token: String,
        chatId: String,
        message: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (token.isBlank() || chatId.isBlank()) return@withContext false

        val url = URL("https://api.telegram.org/bot$token/sendMessage")
        val json = """{"chat_id":"$chatId","text":${message.toJsonString()}}"""

        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
        }

        try {
            BufferedOutputStream(conn.outputStream).use { out ->
                out.write(json.toByteArray(Charsets.UTF_8))
                out.flush()
            }

            val code = conn.responseCode
            if (code in 200..299) {
                // можно прочитать тело если нужно
                true
            } else {
                // Прочитаем ошибку для лога
                val err = conn.errorStream
                if (err != null) {
                    BufferedReader(InputStreamReader(err)).use { br ->
                        br.readLine() // проглотить
                    }
                }
                false
            }
        } catch (_: Exception) {
            false
        } finally {
            conn.disconnect()
        }
    }

    // простая экранизация для JSON строки
    private fun String.toJsonString(): String {
        val sb = StringBuilder(length + 16)
        sb.append('"')
        for (ch in this) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", ch.code))
                    } else sb.append(ch)
                }
            }
        }
        sb.append('"')
        return sb.toString()
    }
}