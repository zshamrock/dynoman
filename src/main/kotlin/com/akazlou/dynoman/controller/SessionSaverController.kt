package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.SearchesSaverService
import tornadofx.*
import java.nio.file.Path

class SessionSaverController : Controller() {
    companion object {
        @JvmField
        val SAVER_TYPE = SearchesSaverService.Type.SESSION
    }

    private val service = SearchesSaverService()

    fun save(name: String, searches: List<Search>) {
        service.save(SAVER_TYPE, getBase(), name, searches)
    }

    fun listNames(): List<String> {
        return service.listNames(getBase())
    }

    fun restore(name: String): List<Search> {
        return service.restore(SAVER_TYPE, getBase(), name)
    }

    fun remove(name: String) {
        service.remove(SAVER_TYPE, getBase(), name)
    }

    private fun getBase(): Path {
        return Config.getSavedSessionsPath(Config.getProfile(app.config), app.configBasePath)
    }
}