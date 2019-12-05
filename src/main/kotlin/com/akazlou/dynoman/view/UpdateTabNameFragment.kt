package com.akazlou.dynoman.view

import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class UpdateTabNameFragment : Fragment("Update Tab Name") {
    companion object {
        private const val UNDEFINED = ""
    }

    val name: String by param(UNDEFINED)
    private val tabNameProperty = SimpleStringProperty(name)

    override val root = form {
        fieldset {
            field("Name:") {
                textfield(tabNameProperty)
            }
        }
        buttonbar {
            button("Update") {
                enableWhen { tabNameProperty.isNotBlank() }
                action {
                    close()
                }
            }
            button("Cancel") {
                action {
                    tabNameProperty.set(UNDEFINED)
                    close()
                }
            }
        }
    }

    fun getTabName(): String {
        return tabNameProperty.value.orEmpty()
    }
}
