package com.akazlou.dynoman.domain

// TODO: Environment separator and the complete logic of the environment detection and stripping should be moved
// to the application settings
/**
 * Extracts environment prefix (if any) from the specified source - table or index using the provided separator.
 *
 * Ex.: dev.TableA will detect and extract "dev" environment and "TableA" as the environment-less table name, while
 * TableA will detect empty/no environment, and use TableA as the environment-less table name.
 */
data class Environment(val source: String, private val separator: String = DEFAULT_ENVIRONMENT_SEPARATOR) {
    companion object {
        const val DEFAULT_ENVIRONMENT_SEPARATOR = "."
        private const val NO_ENVIRONMENT = ""
    }

    val name: String
    val value: String

    init {
        val parts = source.split(separator, limit = 2)
        if (parts.size == 2) {
            name = parts[0]
            value = parts[1]
        } else {
            name = NO_ENVIRONMENT
            value = source
        }
    }

    fun isEmpty(): Boolean {
        return name == NO_ENVIRONMENT
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun prefix(value: String): String {
        if (isEmpty()) {
            return value
        }
        return name + separator + value
    }
}