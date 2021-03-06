package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.UpdateCheckController
import com.akazlou.dynoman.controller.WebBrowserLinkController
import com.akazlou.dynoman.domain.Support
import javafx.scene.input.KeyCombination
import tornadofx.*
import kotlin.system.exitProcess

class MainView : View("DynamoDB Manager") {
    private val controller: UpdateCheckController by inject()
    private val webBrowserLinkController: WebBrowserLinkController by inject()

    override val root = borderpane {
        prefWidth = 970.0
        prefHeight = 730.0
        top {
            menubar {
                stylesheet {

                }
                menu("_File") {
                    item("E_xit", KeyCombination.keyCombination("Ctrl+X")).action {
                        exitProcess(0)
                    }
                }
                menu("_Help") {
                    item("Check for _Updates...").action {
                        val update = controller.getUpdate(true)
                        find<UpdateAnnouncementFragment>(
                                params = mapOf(
                                        UpdateAnnouncementFragment::update to update,
                                        UpdateAnnouncementFragment::confirm to true)).openModal()
                    }
                    item("_About").action {
                        find<AboutFragment>().openModal()
                    }
                }
                menu("_Support") {
                    item("Become _Patron").action {
                        webBrowserLinkController.open(Support.PATREON_LINK)
                    }
                    item("Star on _GitHub").action {
                        webBrowserLinkController.open(Support.GITHUB_PROJECT_LINK)
                    }
                }
            }
        }
        left(TableListView::class)
        center(QueryView::class)
    }
}
