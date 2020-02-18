package com.akazlou.dynoman.domain

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

data class ManagedEnvironment(val name: String, val values: List<EnvironmentValue>) {
    companion object {
        fun isEnvVar(value: String): Boolean {
            return value.startsWith(ENV_PREFIX) && value.endsWith(ENV_SUFFIX)
        }

        fun startsWithPrefix(value: String): Boolean {
            return value.startsWith(ENV_PREFIX)
        }

        fun surround(value: String): String {
            return ENV_PREFIX + value + ENV_SUFFIX
        }

        const val GLOBALS = "Globals"
        val COMPARATOR: Comparator<String> = Comparator { o1, o2 ->
            if (o1 == GLOBALS) {
                -1
            } else {
                o1.compareTo(o2)
            }
        }
        private const val ENV_PREFIX = "{{"
        private const val ENV_SUFFIX = "}}"
    }

    fun get(name: String): String {
        val key = if (name.startsWith(ENV_PREFIX) && name.endsWith(ENV_SUFFIX)) {
            name.removeSurrounding(ENV_PREFIX, ENV_SUFFIX).trim()
        } else {
            name.trim()
        }
        return values.firstOrNull { it.name == key }?.value.orEmpty()
    }

    fun getCompletions(value: String): List<String> {
        val part = value.removePrefix(ENV_PREFIX).trim()
        return values.filter { it.name.contains(part, true) }.map { it.name }
    }
}

class EnvironmentValue(name: String, value: String) {
    companion object {
        private const val SEPARATOR = "="
        fun of(line: String): EnvironmentValue {
            val parts = line.split(SEPARATOR)
            return EnvironmentValue(parts[0], parts[1])
        }
    }

    val nameProperty = SimpleStringProperty(name)
    var name: String by nameProperty

    val valueProperty = SimpleStringProperty(value)
    var value: String by valueProperty
    override fun toString(): String {
        return "$name$SEPARATOR$value"
    }
}