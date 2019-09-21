package com.akazlou.dynoman.controller

import com.akazlou.dynoman.domain.UpdateAnnouncement
import com.akazlou.dynoman.service.UpdateCheckService
import tornadofx.*

class UpdateCheckController : Controller() {
    private val service = UpdateCheckService()

    fun getUpdate(): UpdateAnnouncement {
        return service.getUpdate()
    }
}