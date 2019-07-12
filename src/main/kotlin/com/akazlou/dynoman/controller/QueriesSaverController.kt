package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.QueriesSaverService
import tornadofx.*
import java.nio.file.Path

class QueriesSaverController : Controller() {
    private val service = QueriesSaverService()

    // TODO: Pass the suffix/type of the save operation either session or query
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