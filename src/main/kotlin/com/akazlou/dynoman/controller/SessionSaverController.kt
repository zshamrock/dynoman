package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.search.SearchCriteria
import com.akazlou.dynoman.service.SessionSaverService
import tornadofx.*

class SessionSaverController : Controller() {
    private val service = SessionSaverService()

    fun save(criterias: List<SearchCriteria>, config: ConfigProperties) {
        service.save(criterias)
    }
}