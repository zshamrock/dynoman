package com.akazlou.dynoman.domain

import javafx.beans.property.SimpleStringProperty

data class ManagedEnvironment(val name: String, val values: List<EnvironmentValue>) {
    companion object {
        const val GLOBALS = "Globals"
        const val ENV_PREFIX = "{{"
        const val ENV_SUFFIX = "}}"
    }

    fun get(name: String): String? {
        val key = if (name.startsWith(ENV_PREFIX) && name.endsWith(ENV_SUFFIX)) {
            name.removeSurrounding(ENV_PREFIX, ENV_SUFFIX)
        } else {
            name
        }
        return values.firstOrNull { it.name == key }?.value
    }
}

data class EnvironmentValue(val name: String, val value: String) {
    companion object {
        private const val SEPARATOR = "="
        fun of(line: String): EnvironmentValue {
            val parts = line.split(SEPARATOR)
            return EnvironmentValue(parts[0], parts[1])
        }
    }

    val nameProperty = SimpleStringProperty(name)
    val valueProperty = SimpleStringProperty(value)
    override fun toString(): String {
        return "$name$SEPARATOR$value"
    }
}