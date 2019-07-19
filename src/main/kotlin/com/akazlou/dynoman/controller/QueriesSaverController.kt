package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.QueriesSaverService
import tornadofx.*
import java.nio.file.Path

class QueriesSaverController : Controller() {
    private val service = QueriesSaverService()

    fun save(type: QueriesSaverService.Type, base: Path, name: String, searches: List<Search>) {
        service.save(type, base, name, searches)
    }

    fun listNames(path: Path, refresh: Boolean = false): List<String> {
        return service.listNames(path)
    }

    fun restore(type: QueriesSaverService.Type, base: Path, name: String): List<Search> {
        return service.restore(type, base, name)
    }
}