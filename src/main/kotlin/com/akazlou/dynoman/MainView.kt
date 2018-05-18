package com.akazlou.dynoman

import tornadofx.View
import tornadofx.borderpane

class MainView : View("DynamoDB Manager") {
    override val root = borderpane {
        left(TableListView::class)
    }
}
