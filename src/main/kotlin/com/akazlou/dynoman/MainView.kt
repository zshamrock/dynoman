package com.akazlou.dynoman

import tornadofx.*

class MainView : View("DynamoDB Manager") {
    override val root = borderpane {
        left(TableListView::class)
        center(QueryView::class)
    }
}
