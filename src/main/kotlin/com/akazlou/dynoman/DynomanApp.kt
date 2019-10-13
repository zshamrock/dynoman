package com.akazlou.dynoman

import com.akazlou.dynoman.controller.UpdateCheckController
import com.akazlou.dynoman.style.AppStyle
import com.akazlou.dynoman.view.MainView
import com.akazlou.dynoman.view.UpdateAnnouncementFragment
import tornadofx.*

class DynomanApp : App(MainView::class, AppStyle::class) {
    private val controller: UpdateCheckController by inject()

    init {
        runAsync {
            controller.getUpdate(false)
        } ui { update ->
            if (update.shouldAnnounce()) {
                find<UpdateAnnouncementFragment>(
                        params = mapOf(
                                UpdateAnnouncementFragment::update to update)).openModal()
            }
        }
    }
}