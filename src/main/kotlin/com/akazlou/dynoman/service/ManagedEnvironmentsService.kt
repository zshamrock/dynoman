package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import java.nio.file.Path

class ManagedEnvironmentsService {
    fun getGlobals(): ManagedEnvironment {
        return ManagedEnvironment("Globals",
                System.getProperty("globals", "").split(",").map { it.split("=") }.map { EnvironmentValue(it[0], it[1]) })
    }

    fun save(base: Path, environment: ManagedEnvironment) {
    }
}