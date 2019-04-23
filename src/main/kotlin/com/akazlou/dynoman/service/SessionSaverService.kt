package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.search.SearchCriteria
import tornadofx.*

class SessionSaverService {
    fun save(searches: List<SearchCriteria>) {
        val json = JsonBuilder()
        searches.forEach {
            with(json) {
            }
        }
    }

    fun restore(): List<SearchCriteria> {
        TODO("Implement")
    }
}