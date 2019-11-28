package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.ManagedEnvironment
import com.akazlou.dynoman.service.ManagedEnvironmentsService
import tornadofx.*

class ManagedEnvironmentsController : Controller() {
    private val service = ManagedEnvironmentsService()

    fun getGlobals(): ManagedEnvironment {
        return service.getGlobals()
    }
}