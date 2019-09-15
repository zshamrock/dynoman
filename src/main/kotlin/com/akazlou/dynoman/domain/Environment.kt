package com.akazlou.dynoman.domain

// TODO: Environment separator and the complete logic of the environment detection and stripping should be moved
// to the application settings
data class Environment(val tableOrIndex: String) {
    companion object {
        private const val ENVIRONMENT_SEPARATOR = "."
        private const val NO_ENVIRONMENT = ""
    }

    val name: String
    val envlessTableOrIndex: String

    init {
        val parts = tableOrIndex.split(ENVIRONMENT_SEPARATOR, limit = 2)
        if (parts.size == 2) {
            name = parts[0]
            envlessTableOrIndex = parts[1]
        } else {
            name = NO_ENVIRONMENT
            envlessTableOrIndex = tableOrIndex
        }
    }

    fun isEmpty(): Boolean {
        return name == NO_ENVIRONMENT
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }
}