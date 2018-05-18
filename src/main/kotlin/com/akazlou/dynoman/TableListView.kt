package com.akazlou.dynoman

import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import tornadofx.View
import tornadofx.vbox

class TableListView : View() {
    override val root = vbox {
        add(TreeView(TreeItem("X")))
    }
}
