package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.UpdateAnnouncement
import com.akazlou.dynoman.domain.Version
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Files
import java.nio.file.Paths

class UpdateCheckService {
    companion object {
        private const val BASE_RELEASE_URL = "https://raw.githubusercontent.com/zshamrock/dynoman/master/release"
        private const val PROPERTY_PREFIX = "dynoman"
        private const val RELEASE_DIR = "release"

        @JvmField
        val DEFAULT_ANNOUNCEMENT = UpdateAnnouncement(version = Version.CURRENT)
    }

    private val debug = getSystemBooleanProperty("$PROPERTY_PREFIX.debug", true)
    private val noUpdate = getSystemBooleanProperty("$PROPERTY_PREFIX.noupdate")

    private enum class Type(val filename: String) {
        VERSION("version"),
        ANNOUNCEMENT("announcement"),
        CHANGELOG("changelog"),
        URL("url")
    }

    private val client: OkHttpClient = OkHttpClient()

    fun getUpdate(): UpdateAnnouncement {
        if (noUpdate) {
            return DEFAULT_ANNOUNCEMENT
        }
        var version: String = Version.CURRENT
        var announcement = ""
        var changelog = ""
        var url = ""
        try {
            version = get(Type.VERSION, Version.CURRENT)
            if (version != Version.CURRENT) {
                // TODO: Might combine this into the single REST call and return combined output, could be JSON
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
        if (debug) {
            return Files.readAllLines(Paths.get(System.getProperty("user.dir"), RELEASE_DIR, type.filename))
                    .joinToString(separator = System.lineSeparator())
        }
        val call = client.newCall(Request.Builder()
                .url("${BASE_RELEASE_URL}/${type.filename}")
                .build())
        return call.execute().use { response ->
            response.body?.string() ?: def
        }
    }

    private fun getSystemBooleanProperty(name: String, presenceAsTrue: Boolean = false): Boolean {
        return System.getProperty(name).orEmpty().ifEmpty {
            if (presenceAsTrue) {
                "true"
            } else {
                "false"
            }
        }.toBoolean()
    }
}