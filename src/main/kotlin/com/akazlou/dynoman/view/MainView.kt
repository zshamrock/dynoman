package com.akazlou.dynoman.view

import javafx.scene.input.KeyCombination
import tornadofx.*

class MainView : View("DynamoDB Manager") {
    override val root = borderpane {
        prefWidth = 970.0
        prefHeight = 730.0
        top {
            menubar {
                menu("File") {
                    item("Exit", KeyCombination.keyCombination("Ctrl+X")).action {
                        System.exit(0)
                    }
                }
                menu("Help") {
                    item("About").action {
                        println("Dynoman ${Config.VERSION}")
                    }
                }
            }
        }
        left(TableListView::class)
        center(QueryView::class)
    }
}
