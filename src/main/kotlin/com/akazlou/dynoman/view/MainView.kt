package com.akazlou.dynoman.view

import javafx.scene.input.KeyCombination
import tornadofx.*

class MainView : View("DynamoDB Manager") {
    override val root = borderpane {
        prefWidth = 970.0
        prefHeight = 730.0
        top {
            menubar {
                stylesheet {

                }
                menu("_File") {
                    item("E_xit", KeyCombination.keyCombination("Ctrl+X")).action {
                        System.exit(0)
                    }
                }
                menu("_Help") {
                    item("_About").action {
                        println("Dynoman ${Config.VERSION}")
                    }
                }
            }
        }
        left(TableListView::class)
        center(QueryView::class)
    }
}
