package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.ManagedEnvironment
import com.akazlou.dynoman.service.ManagedEnvironmentsService
import tornadofx.*
import java.nio.file.Path

class ManagedEnvironmentsController : Controller() {
    private val service = ManagedEnvironmentsService()

    fun getGlobals(): ManagedEnvironment {
        return service.getGlobals()
    }

    fun save(base: Path, environment: ManagedEnvironment) {
        service.save(base, environment)
    }
}