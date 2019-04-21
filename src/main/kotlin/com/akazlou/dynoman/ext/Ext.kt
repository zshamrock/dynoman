package com.akazlou.dynoman.ext

import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.control.TabPane

fun <T : Node> TabPane.tab(index: Int, text: String, content: T, op: T.() -> Unit = {}): Tab {
    val tab = Tab(text, content)
    tabs.add(index, tab)
    op(content)
    return tab
}