package com.akazlou.dynoman.domain

import javafx.beans.property.SimpleStringProperty

data class ManagedEnvironment(val name: String, val values: List<EnvironmentValue>) {
    companion object {
        const val GLOBALS = "Globals"
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