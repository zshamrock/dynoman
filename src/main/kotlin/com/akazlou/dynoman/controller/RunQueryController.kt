package com.akazlou.dynoman.controller

import tornadofx.Controller

class RunQueryController : Controller() {
    fun run(query: String): String {
        return "Run $query at ${System.currentTimeMillis()}"
    }
}