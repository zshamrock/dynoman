package com.akazlou.dynoman.service

import com.akazlou.dynoman.domain.EnvironmentValue
import com.akazlou.dynoman.domain.ManagedEnvironment
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.TimeUnit

class ManagedEnvironmentsService : SaverService() {
    private val envs: LoadingCache<Pair<Path, String>, ManagedEnvironment> = Caffeine.newBuilder()
            .maximumSize(10)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build { restore(it.first, it.second) }

    fun save(base: Path, environment: ManagedEnvironment) {
        val writer = StringWriter()
        environment.values.forEach {
            writer.appendln(it.toString())
        }
        write(base, environment.name, ".env", writer)
        envs.refresh(Pair(base, environment.name))
    }

    fun restore(base: Path, name: String): ManagedEnvironment {
        val path = resolve(base, name, ".env")
        if (Files.notExists(path)) {
            return ManagedEnvironment(name, emptyList())
        }
        val lines = Files.readAllLines(path, StandardCharsets.UTF_8)
        return ManagedEnvironment(name, lines.filter { it.isNotBlank() }.map { EnvironmentValue.of(it) })
    }

    fun get(base: Path, name: String): ManagedEnvironment {
        return envs.get(Pair(base, name))!!
    }
}