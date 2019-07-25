package com.akazlou.dynoman.controller

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

    fun save(base: Path, name: String, searches: List<Search>) {
        service.save(SAVER_TYPE, base, name, searches)
    }

    fun listNames(path: Path): List<String> {
        return service.listNames(path)
    }

    fun restore(base: Path, name: String): List<Search> {
        return service.restore(SAVER_TYPE, base, name)
    }
}