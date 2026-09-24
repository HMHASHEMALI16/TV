package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class ChannelRepository(private val context: Context) {

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val cacheFile = File(context.filesDir, "channels_cache.json")

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Default remote API endpoint mentioned in specifications
    private val remoteApiUrl = "http://10.6.6.2/channels.json"

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            // 1. Try loading from bundled assets/channels.json
            val bundled = loadFromAssets()
            if (bundled.isNotEmpty()) {
                _channels.value = bundled
            } else {
                // Fallback to hardcoded list of all 48 channels
                _channels.value = getHardcodedChannels()
            }

            // 4. Try updating in background from remote server if reachable
            fetchFromRemote()
        }
    }

    suspend fun refreshRemote() {
        withContext(Dispatchers.IO) {
            fetchFromRemote()
        }
    }

    private fun fetchFromRemote() {
        try {
            val request = Request.Builder()
                .url(remoteApiUrl)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val parsed = parseChannels(body)
                        if (parsed.isNotEmpty()) {
                            _channels.value = parsed
                            saveToCache(body)
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Server unavailable or not connected to local IPTV network;
            // elderly user should not be bothered with network errors.
        }
    }

    private fun loadFromCache(): List<Channel> {
        return try {
            if (cacheFile.exists()) {
                val json = cacheFile.readText()
                parseChannels(json)
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveToCache(json: String) {
        try {
            cacheFile.writeText(json)
        } catch (_: Exception) {
            // Ignore cache write errors
        }
    }

    private fun loadFromAssets(): List<Channel> {
        return try {
            val json = context.assets.open("channels.json").bufferedReader().use { it.readText() }
            parseChannels(json)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseChannels(json: String): List<Channel> {
        val result = mutableListOf<Channel>()
        try {
            val root = JSONObject(json)
            val array = root.optJSONArray("channels") ?: return emptyList()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val id = item.optString("id", "")
                val name = item.optString("name", "")
                var streamUrl = item.optString("streamUrl", "")
                if (streamUrl.isBlank() && id.isNotEmpty()) {
                    streamUrl = "http://10.6.6.2/play.php?id=$id"
                }
                if (id.isNotEmpty() && name.isNotEmpty()) {
                    result.add(Channel(id = id, name = name, streamUrl = streamUrl))
                }
            }
        } catch (_: Exception) {
            // Ignore parsing error
        }
        return result
    }

    companion object {
        fun getHardcodedChannels(): List<Channel> {
            return listOf(
                Channel("channel_31", "71", "http://10.6.6.2/play.php?id=channel_31"),
                Channel("channel_32", "Ananda TV", "http://10.6.6.2/play.php?id=channel_32"),
                Channel("channel_33", "Asian TV", "http://10.6.6.2/play.php?id=channel_33"),
                Channel("channel_34", "ATN Bangla", "http://10.6.6.2/play.php?id=channel_34"),
                Channel("channel_35", "ATN News", "http://10.6.6.2/play.php?id=channel_35"),
                Channel("channel_36", "Bangla TV", "http://10.6.6.2/play.php?id=channel_36"),
                Channel("channel_37", "Bangla Vision", "http://10.6.6.2/play.php?id=channel_37"),
                Channel("channel_38", "Bijoy TV", "http://10.6.6.2/play.php?id=channel_38"),
                Channel("channel_39", "Boishakhi TV", "http://10.6.6.2/play.php?id=channel_39"),
                Channel("channel_40", "BTV", "http://10.6.6.2/play.php?id=channel_40"),
                Channel("channel_41", "BTV News", "http://10.6.6.2/play.php?id=channel_41"),
                Channel("channel_42", "Channel 24", "http://10.6.6.2/play.php?id=channel_42"),
                Channel("channel_43", "Channel 9", "http://10.6.6.2/play.php?id=channel_43"),
                Channel("channel_44", "Channel i", "http://10.6.6.2/play.php?id=channel_44"),
                Channel("channel_45", "DBC News", "http://10.6.6.2/play.php?id=channel_45"),
                Channel("channel_46", "Deepto TV", "http://10.6.6.2/play.php?id=channel_46"),
                Channel("channel_47", "Desh TV", "http://10.6.6.2/play.php?id=channel_47"),
                Channel("channel_48", "Duronto TV", "http://10.6.6.2/play.php?id=channel_48"),
                Channel("channel_49", "Ekhon TV", "http://10.6.6.2/play.php?id=channel_49"),
                Channel("channel_50", "ETV", "http://10.6.6.2/play.php?id=channel_50"),
                Channel("channel_51", "Global TV", "http://10.6.6.2/play.php?id=channel_51"),
                Channel("channel_52", "GTV", "http://10.6.6.2/play.php?id=channel_52"),
                Channel("channel_53", "Independent TV", "http://10.6.6.2/play.php?id=channel_53"),
                Channel("channel_54", "Jamuna TV", "http://10.6.6.2/play.php?id=channel_54"),
                Channel("channel_55", "Maasranga HD", "http://10.6.6.2/play.php?id=channel_55"),
                Channel("channel_56", "Mohona TV", "http://10.6.6.2/play.php?id=channel_56"),
                Channel("channel_57", "Movie Bangla TV", "http://10.6.6.2/play.php?id=channel_57"),
                Channel("channel_58", "My TV", "http://10.6.6.2/play.php?id=channel_58"),
                Channel("channel_59", "Nagorik TV", "http://10.6.6.2/play.php?id=channel_59"),
                Channel("channel_60", "News24", "http://10.6.6.2/play.php?id=channel_60"),
                Channel("channel_61", "Nexus TV", "http://10.6.6.2/play.php?id=channel_61"),
                Channel("channel_62", "NTV", "http://10.6.6.2/play.php?id=channel_62"),
                Channel("channel_63", "RTV", "http://10.6.6.2/play.php?id=channel_63"),
                Channel("channel_64", "SATV", "http://10.6.6.2/play.php?id=channel_64"),
                Channel("channel_65", "Somoy TV", "http://10.6.6.2/play.php?id=channel_65"),
                Channel("channel_66", "Akash Aat", "http://10.6.6.2/play.php?id=channel_66"),
                Channel("channel_67", "Colors Bangla Cinema", "http://10.6.6.2/play.php?id=channel_67"),
                Channel("channel_68", "Colors Bangla HD", "http://10.6.6.2/play.php?id=channel_68"),
                Channel("channel_69", "Enterr 10", "http://10.6.6.2/play.php?id=channel_69"),
                Channel("channel_70", "Jalsha Movies HD", "http://10.6.6.2/play.php?id=channel_70"),
                Channel("channel_71", "Rongeen TV", "http://10.6.6.2/play.php?id=channel_71"),
                Channel("channel_72", "Ruposhi Bangla", "http://10.6.6.2/play.php?id=channel_72"),
                Channel("channel_73", "Sony Aath", "http://10.6.6.2/play.php?id=channel_73"),
                Channel("channel_74", "Star Jalsha HD", "http://10.6.6.2/play.php?id=channel_74"),
                Channel("channel_75", "Sun Bangla HD", "http://10.6.6.2/play.php?id=channel_75"),
                Channel("channel_76", "Zee 24 Ghanta", "http://10.6.6.2/play.php?id=channel_76"),
                Channel("channel_77", "Zee Bangla Cinema", "http://10.6.6.2/play.php?id=channel_77"),
                Channel("channel_78", "Zee Bangla HD", "http://10.6.6.2/play.php?id=channel_78")
            )
        }
    }
}
