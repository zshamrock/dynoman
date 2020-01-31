package com.akazlou.dynoman.service

import java.nio.file.Path

open class SaverService {
    protected fun resolve(base: Path, name: String, suffix: String): Path = base.resolve("$name$suffix")
}