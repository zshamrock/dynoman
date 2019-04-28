package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.SearchCriteria
import com.akazlou.dynoman.service.SessionSaverService
import tornadofx.*
import java.nio.file.Path

class SessionSaverController : Controller() {
    private val service = SessionSaverService()

    fun save(path: Path, searches: List<SearchCriteria>, config: ConfigProperties) {
        service.save(path, searches)
    }
}