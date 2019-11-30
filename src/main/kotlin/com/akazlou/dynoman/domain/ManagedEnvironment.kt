package com.akazlou.dynoman.domain

import javafx.beans.property.SimpleStringProperty

data class ManagedEnvironment(val name: String, val values: List<EnvironmentValue>)

data class EnvironmentValue(val name: String, val value: String) {
    val nameProperty = SimpleStringProperty(name)
    val valueProperty = SimpleStringProperty(value)
}