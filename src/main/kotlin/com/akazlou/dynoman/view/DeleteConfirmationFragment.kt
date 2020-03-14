package com.akazlou.dynoman.view

import javafx.geometry.Pos
import tornadofx.*

class DeleteConfirmationFragment : Fragment("Delete Confirmation") {
    val type: Type by param()
    val name: String by param()
    private var confirmed: Boolean = false

    enum class Type(val title: String) {
        FOREIGN_QUERY("foreign query"),
        ENVIRONMENT("environment"),
        SESSION("saved session")
    }

    override val root = vbox(5.0, Pos.CENTER) {
        prefWidth = 720.0
        prefHeight = 80.0
        text("You are about to delete ${type.title} \"$name\". Are you sure you want to continue?")
        buttonbar {
            paddingRight = 5.0
            button("Delete") {
                action {
                    confirmed = true
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }

    fun isConfirmed(): Boolean {
        return confirmed
    }
}