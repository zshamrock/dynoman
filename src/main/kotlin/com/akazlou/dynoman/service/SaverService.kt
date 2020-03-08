package com.akazlou.dynoman.service

import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

open class SaverService {
    protected fun resolve(base: Path, name: String, suffix: String): Path = base.resolve("$name$suffix")

    protected fun write(base: Path, name: String, suffix: String, writer: StringWriter) {
        Files.createDirectories(base)
        Files.write(resolve(base, name, suffix),
                listOf(writer.toString()),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)
    }

    protected fun remove(base: Path, name: String, suffix: String) {
        Files.deleteIfExists(resolve(base, name, suffix))
    }
}