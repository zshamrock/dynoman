package com.akazlou.dynoman

import tornadofx.*

class RunQueryController : Controller() {
    fun run(query: String): String {
        return "Run $query at ${System.currentTimeMillis()}"
    }
}