package com.akazlou.dynoman.view

import tornadofx.*

class MainView : View("DynamoDB Manager") {
    override val root = borderpane {
        prefWidth = 920.0
        prefHeight = 730.0
        left(TableListView::class)
        center(QueryView::class)
    }
}
