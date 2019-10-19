package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.WebBrowserLinkController
import com.akazlou.dynoman.domain.Support
import com.akazlou.dynoman.domain.Version
import javafx.geometry.Pos
import javafx.scene.control.Hyperlink
import tornadofx.*

class AboutFragment : Fragment("About") {
    private val webBrowserLinkController: WebBrowserLinkController by inject()

    override val root = vbox {
        prefWidth = 540.0
        prefHeight = 220.0
        alignment = Pos.CENTER
        text("Dynoman (aka DynamoDB Manager) ${Version.CURRENT}")
        hbox {
            alignment = Pos.CENTER
            text("Built with love and passion by")
            hyperlink("Aliaksandr Kazlou", op = setup(Support.GITHUB_PROFILE_LINK))
            text(".")
        }
        separator()
        hbox {
            alignment = Pos.CENTER
            text("You can help and support the project by becoming the patron on")
            hyperlink("Patreon", op = setup(Support.PATREON_LINK))
        }
        hbox {
            alignment = Pos.CENTER
            text("and star the project on")
            hyperlink("GitHub", op = setup(Support.GITHUB_PROJECT_LINK))
            text("!")
        }
        button("Close") {
            action {
                close()
            }
        }
    }

    private fun setup(link: String): Hyperlink.() -> Unit {
        return {
            action {
                webBrowserLinkController.open(link)
            }
        }
    }
}
