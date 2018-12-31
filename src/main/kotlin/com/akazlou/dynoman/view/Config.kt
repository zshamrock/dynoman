package com.akazlou.dynoman.view

import com.amazonaws.regions.Regions
import tornadofx.*

// XXX: Should use instead the instance of the class, and inject it accordingly to each of the view
// (or research on how enable it the global wide similar to the app.config)
object Config {
    const val REGION = "region"
    @JvmField
    val DEFAULT_REGION = Regions.US_WEST_2
    const val LOCAL = "local"

    fun getRegion(config: ConfigProperties): String {
        return config.string(REGION, DEFAULT_REGION.name)
    }

    fun isLocal(config: ConfigProperties): Boolean {
        return config.boolean(LOCAL, false)!!
    }
}