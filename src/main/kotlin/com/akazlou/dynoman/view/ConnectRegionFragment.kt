package com.akazlou.dynoman.view

import com.amazonaws.regions.Regions
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*

class ConnectRegionFragment : Fragment("Connect Region") {
    private val region = SimpleStringProperty(Regions.US_WEST_2.getName())
    override val root = vbox {
        hbox {
            label("Region: ")
            combobox(region, Regions.values().map { it.getName() })
            alignment = Pos.CENTER
        }
        hbox {
            button("Connect") {
                setPrefSize(100.0, 40.0)
                action {
                    with(app.config) {
                        set("region" to region.value)
                        save()
                    }
                    close()
                }
            }
            alignment = Pos.CENTER
        }
    }
}
