package com.akazlou.dynoman

import javafx.geometry.Pos
import javafx.scene.control.TextArea
import tornadofx.*

class QueryView : View("Query") {
    private val controller: RunQueryController by inject()

    private var queryArea: TextArea by singleAssign()
    private var resultArea: TextArea by singleAssign()

    override val root = vbox {
        queryArea = textarea("SELECT * FROM T") {
            selectAll()
        }
        resultArea = textarea {
            isEditable = false
        }
        hbox {
            alignment = Pos.CENTER
            button("Run") {
                setPrefSize(100.0, 40.0)
                action {
                    val result = controller.run(getQuery())
                    setQueryResult(result)
                }
                shortcut("Ctrl+R")
            }
        }
    }

    private fun getQuery(): String {
        return queryArea.text
    }

    private fun setQueryResult(result: String) {
        resultArea.text = result
    }
}
