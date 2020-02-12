package com.akazlou.dynoman.domain

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

data class ManagedEnvironment(val name: String, val values: List<EnvironmentValue>) {
    companion object {
        fun isEnvVar(value: String): Boolean {
            return value.startsWith(ENV_PREFIX) && value.endsWith(ENV_SUFFIX)
        }

        const val GLOBALS = "Globals"
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