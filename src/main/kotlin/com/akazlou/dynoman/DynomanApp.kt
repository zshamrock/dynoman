package com.akazlou.dynoman

import com.akazlou.dynoman.style.AppStyle
import com.akazlou.dynoman.view.Config
import com.akazlou.dynoman.view.MainView
import com.akazlou.dynoman.view.UpdateAnnouncementFragment
import okhttp3.OkHttpClient
import okhttp3.Request
import tornadofx.*

class DynomanApp : App(MainView::class, AppStyle::class) {
    private val client: OkHttpClient = OkHttpClient()

    init {
        val versionCall = client.newCall(Request.Builder()
                .url("https://raw.githubusercontent.com/zshamrock/dynoman/master/release/version")
                .build())
        runAsync {
            var version: String = Config.VERSION
            var announcement = ""
            try {
                versionCall.execute().use { response ->
                    version = response.body?.string() ?: Config.VERSION
                    if (version != Config.VERSION) {
                        val announcementCall = client.newCall(Request.Builder()
                                .url("https://raw.githubusercontent.com/zshamrock/dynoman/master/release/announcement")
                                .build())
                        announcementCall.execute().use { response ->
                            announcement = response.body?.string().orEmpty()
                        }
                    }
                }
            } catch (ex: Exception) {
                // ignore
            }
            Pair(version, announcement)
        } ui { (version, announcement) ->
            if (version != Config.VERSION) {
                find<UpdateAnnouncementFragment>(
                        params = mapOf(
                                UpdateAnnouncementFragment::version to version,
                                UpdateAnnouncementFragment::announcement to announcement)).openModal()
            }
        }
    }
}