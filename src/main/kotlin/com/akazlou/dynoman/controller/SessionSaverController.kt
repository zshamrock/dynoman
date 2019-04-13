package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.SessionSaverService
import tornadofx.*
import java.nio.file.Path

class SessionSaverController : Controller() {
    private val service = SessionSaverService()

    fun save(path: Path, search: Search, config: ConfigProperties) {
        val json = JsonBuilder()
        with(json) {
        }
        service.save(search)
        service.save(search)
    }
}