package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.ForeignSearchName
import com.akazlou.dynoman.domain.search.ResultData
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.AddQuerySaverService
import tornadofx.*
import java.nio.file.Path

class AddQuerySaverController : Controller() {
    private val service = AddQuerySaverService()

    fun save(table: String, name: String, search: Search, data: List<ResultData>): ForeignSearchName {
        return service.save(table, getBase(), name, search, data)
    }

    fun listNames(table: String): List<ForeignSearchName> {
        return service.listNames(table, getBase())
    }

    fun restore(table: String, name: ForeignSearchName): Search {
        return service.restore(table, getBase(), name)
    }

    private fun getBase(): Path {
        return Config.getSavedQueriesPath(Config.getProfile(app.config), app.configBasePath)
    }
}