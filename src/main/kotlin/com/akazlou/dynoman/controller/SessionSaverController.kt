package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.SearchCriteria
import com.akazlou.dynoman.service.SessionSaverService
import tornadofx.*
import java.nio.file.Path

class SessionSaverController : Controller() {
    private val service = SessionSaverService()

    fun save(path: Path, name: String, searches: List<SearchCriteria>, config: ConfigProperties) {
        service.save(path, name, searches)
    }

    fun listNames(path: Path): List<String> {
        return service.listNames(path)
    }

    fun restore(path: Path): List<SearchCriteria> {
        return service.restore(path)
    }
}