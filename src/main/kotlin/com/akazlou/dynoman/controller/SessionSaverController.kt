package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.SessionSaverService
import tornadofx.*
import java.nio.file.Path

class SessionSaverController : Controller() {
    private val service = SessionSaverService()

    fun save(base: Path, name: String, searches: List<Search>, config: ConfigProperties) {
        service.save(base, name, searches)
    }

    fun listNames(path: Path): List<String> {
        return service.listNames(path)
    }

    fun restore(base: Path, name: String): List<Search> {
        return service.restore(base, name)
    }
}