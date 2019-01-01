package com.akazlou.dynoman.view

import com.amazonaws.regions.Regions
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*

class ConnectRegionFragment : Fragment("Connect Region") {
    private val region = SimpleStringProperty(Config.getRegion(app.config))
    private val profile = SimpleStringProperty(Config.getProfile(app.config))
    private val local = SimpleBooleanProperty(Config.isLocal(app.config))
    override val root = vbox(5.0) {
        hbox(10.0, Pos.CENTER_LEFT) {
            label("Profile:")
            textfield(profile)
        }
        hbox(10.0, Pos.CENTER_LEFT) {
            label("Region:")
            combobox(region, Regions.values().map { it.getName() })
            checkbox("local", local)
        }
        hbox(alignment = Pos.CENTER) {
            button("Connect") {
                //setPrefSize(100.0, 40.0)
                prefWidth = 100.0
                action {
                    with(app.config) {
                        set(Config.REGION to region.value)
                        set(Config.LOCAL to local.value)
                        set(Config.PROFILE to profile.value)
                        save()
                    }
                    close()
                }
            }
        }
    }
}
