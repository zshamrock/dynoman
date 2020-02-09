package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.Config
import com.akazlou.dynoman.domain.ManagedEnvironment
import com.akazlou.dynoman.service.ManagedEnvironmentsService
import tornadofx.*
import java.nio.file.Path

class ManagedEnvironmentsController : Controller() {
    private val service = ManagedEnvironmentsService()

    fun save(base: Path, environment: ManagedEnvironment) {
        service.save(base, environment)
    }

    fun restore(base: Path, name: String): ManagedEnvironment {
        return service.restore(base, name)
    }

    // TODO: Refactor method above using app directly from the Controller
    fun get(name: String): ManagedEnvironment {
        return service.get(Config.getSavedEnvironmentsPath(Config.getProfile(app.config), app.configBasePath), name)
    }
}