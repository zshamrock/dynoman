package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.UpdateAnnouncement
import com.akazlou.dynoman.domain.Version
import com.akazlou.dynoman.view.Config
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

class UpdateCheckService {
    companion object {
        private const val BASE_RELEASE_URL = "https://raw.githubusercontent.com/zshamrock/dynoman/master/release"
    }

    private enum class Type {
        VERSION,
        ANNOUNCEMENT,
        CHANGELOG,
        URL
    }

    private val client: OkHttpClient = OkHttpClient()

    fun getAnnouncement(): UpdateAnnouncement {
        var version: String = Config.VERSION
        var announcement = ""
        var changelog = ""
        var url = ""
        try {
            version = get(Type.VERSION, Version.CURRENT)
            if (version != Config.VERSION) {
                announcement = get(Type.ANNOUNCEMENT)
                changelog = get(Type.CHANGELOG)
                url = get(Type.URL)
            }
        } catch (ex: Exception) {
            // ignore
        }
        return UpdateAnnouncement(version, announcement, changelog, url)
    }

    private fun get(type: Type, def: String = ""): String {
        val call = client.newCall(Request.Builder()
                .url("${BASE_RELEASE_URL}/${type.name.toLowerCase(Locale.ROOT)}")
                .build())
        return call.execute().use { response ->
            response.body?.string() ?: def
        }
    }
}