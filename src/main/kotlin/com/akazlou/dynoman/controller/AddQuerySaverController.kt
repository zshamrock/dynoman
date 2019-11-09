package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.AddQuerySaverService
import tornadofx.*
import java.nio.file.Path

class AddQuerySaverController : Controller() {
    private val service = AddQuerySaverService()

    fun save(table: String, base: Path, name: String, search: Search): ForeignSearchName {
        return service.save(table, base, name, search)
    }

    fun listNames(table: String, path: Path): List<ForeignSearchName> {
        return service.listNames(table, path)
    }

    fun restore(table: String, base: Path, name: ForeignSearchName): Search {
        return service.restore(table, base, name)
    }
}