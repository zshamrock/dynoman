package com.akazlou.dynoman.controller

import tornadofx.*
import java.awt.Desktop
import java.net.URL

class WebBrowserLinkController : Controller() {
    private val hostServicesSupported: Boolean

    init {
        hostServicesSupported = try {
            // If running in JDK where full JavaFX is supported use host services to open the URI in the
            // browser.
            // The reason on why java.awt.Desktop is not used in all cases, as it has been reported in
            // various sources, that on some system java.awt.Desktop doesn't work properly.
            Class.forName("com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory")
            true
        } catch (ex: Exception) {
            // Amazon Corretto JDK 8 (which is used for in Docker image) does provide embedded support
            // for JavaFX, although not every package is available there as per
            // https://github.com/corretto/corretto-8/issues/26, in particular HostServicesFactory is
            // not available, so fallback on use java.awt.Desktop to open the URL in the browser.
            false
        }
    }

    fun open(url: String) {
        if (hostServicesSupported) {
            hostServices.showDocument(url)
        } else {
            Desktop.getDesktop().browse(URL(url).toURI())
        }
    }
}