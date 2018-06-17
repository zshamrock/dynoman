package com.akazlou.dynoman.view

import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.TableDescription
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.GridPane
import tornadofx.*

class QueryWindowFragment : Fragment("Query...") {
    companion object {
        @JvmField
        val SORT_KEY_AVAILABLE_OPERATORS: List<String> = listOf("=", ">", "<", ">=", "<=", "between")
    }

    val description: TableDescription by param()
    private val queryTypes: List<QueryType>
    private var queryTypeComboBox: ComboBox<QueryType> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private val rowObservables: List<SimpleBooleanProperty>

    init {
        val gsi = description.globalSecondaryIndexes.orEmpty()
        val indexQueryStrings = gsi.map { index ->
            QueryType(index.indexName, index.keySchema, true)
        }
        queryTypes = listOf(QueryType(description.tableName, description.keySchema, false), *indexQueryStrings.toTypedArray())
        rowObservables = generateSequence {
            SimpleBooleanProperty(false)
        }.take(queryTypes.size).toList()
        rowObservables[0].value = true
        println(rowObservables)
    }

    override val root = vbox(5.0) {
        hbox(5.0) {
            label("Query")
            queryTypeComboBox = combobox(values = queryTypes) {
                selectionModel.select(0)
            }
            queryTypeComboBox.valueProperty().onChange {
                val children = queryGridPane.children
                children.clear()
                var rowIndex = 0
                it?.keySchema?.forEach {
                    val isHash = it.keyType == KeyType.HASH.name
                    queryGridPane.addRow(rowIndex, label(if (isHash) "Partition Key" else "Sort Key"))
                    queryGridPane.addRow(rowIndex, text(it.attributeName))
                    if (isHash) {
                        queryGridPane.addRow(rowIndex, label("="))
                    } else {
                        queryGridPane.addRow(rowIndex, combobox(values = SORT_KEY_AVAILABLE_OPERATORS))
                    }
                    queryGridPane.addRow(rowIndex, textfield { })
                    rowIndex++
                }
            }
        }
        queryGridPane = gridpane {
            queryTypes[0].keySchema.forEach {
                row {
                    val isHash = it.keyType == KeyType.HASH.name
                    label(if (isHash) "Partition Key" else "Sort Key")
                    text(it.attributeName)
                    if (isHash) {
                        label("=")
                    } else {
                        combobox(values = SORT_KEY_AVAILABLE_OPERATORS)
                    }
                    textfield { }
                }
            }
        }
        button("Add filter") {
            setPrefSize(100.0, 40.0)
            action {

            }
        }
        separator()
        val sortGroup = ToggleGroup()
        hbox(5.0) {
            label("Sort")
            val asc = radiobutton("Ascending", sortGroup)
            radiobutton("Descending", sortGroup)
            sortGroup.selectToggle(asc)
        }
        separator()
        hbox(5.0) {
            alignment = Pos.CENTER
            button("Query") {
                // TODO: Extract the sizes into the constant, so allow ease of modification just in one place
                setPrefSize(100.0, 40.0)
            }
            button("Cancel") {
                setPrefSize(100.0, 40.0)
                action {
                    close()
                }
            }
        }
    }
}

private data class QueryType(val name: String, val keySchema: List<KeySchemaElement>, val isIndex: Boolean) {
    override fun toString(): String {
        return (if (isIndex) "[Index]" else "[Table]") + " $name: ${joinKeySchema(keySchema)}"
    }

    private fun joinKeySchema(keySchema: List<KeySchemaElement>): String {
        return keySchema.joinToString { it.attributeName }
    }
}