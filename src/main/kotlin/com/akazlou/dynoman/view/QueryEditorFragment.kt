package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.Type
import com.akazlou.dynoman.ext.removeAllRows
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ComboBox
import javafx.scene.layout.GridPane
import tornadofx.*

class QueryEditorFragment : Fragment("Query Editor") {
    private val queryTypes: List<QueryType> = emptyList()
    private var queryTypeComboBox: ComboBox<QueryType> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private val queryType = SimpleObjectProperty<QueryType>()
    private val hashKey = SimpleStringProperty()
    private val sortKey = SimpleStringProperty()
    private val sortKeyOperation = SimpleObjectProperty<Operator>(Operator.EQ)
    private val sort = SimpleStringProperty("asc")
    private val filterKeys = mutableListOf<SimpleStringProperty>()
    private val filterKeyTypes = mutableListOf<SimpleObjectProperty<Type>>()
    private val filterKeyOperations = mutableListOf<SimpleObjectProperty<Operator>>()
    private val filterKeyValues = mutableListOf<SimpleStringProperty?>()
    private var keysRowsCount = 0

    override val root = vbox(5.0) {
        hbox(5.0) {
            label("Query")
            queryTypeComboBox = combobox(values = queryTypes, property = queryType)
            queryTypeComboBox.valueProperty().onChange {
                queryGridPane.removeAllRows()
                hashKey.value = ""
                sortKey.value = ""
                sortKeyOperation.value = Operator.EQ
//                addRow(queryGridPane, it!!.keySchema)
            }
        }
        queryGridPane = gridpane {}
//        addRow(queryGridPane, queryTypes[0].keySchema)
        button("Add filter") {
            setPrefSize(100.0, 40.0)
            action {
                //                addFilterRow(queryGridPane)
            }
        }
        separator()

    }
}
