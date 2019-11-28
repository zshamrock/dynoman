package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment

class ManagedEnvironmentsService {
    fun getGlobals(): ManagedEnvironment {
        return ManagedEnvironment("Globals",
                System.getProperty("globals", "").split(",").map { it.split("=") }.map { EnvironmentValue(it[0], it[1]) })
    }
}