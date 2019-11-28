package com.akazlou.dynoman.domain

data class ManagedEnvironment(val name: String, val values: List<EnvironmentValue>)

data class EnvironmentValue(val name: String, val value: String)