package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.ManagedEnvironment
import com.akazlou.dynoman.service.ManagedEnvironmentsService
import tornadofx.*
import java.nio.file.Path

class ManagedEnvironmentsController : Controller() {
    private val service = ManagedEnvironmentsService()

    fun save(environment: ManagedEnvironment) {
        service.save(getBase(), environment)
    }

    fun get(name: String): ManagedEnvironment {
        return service.get(getBase(), name)
    }

    fun list(): List<String> {
        return service.list(getBase())
    }

    fun remove(name: String) {
        service.remove(getBase(), name)
    }

    private fun getBase(): Path {
        return Config.getSavedEnvironmentsPath(Config.getProfile(app.config), app.configBasePath)
    }
}