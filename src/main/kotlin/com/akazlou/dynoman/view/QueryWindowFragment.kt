package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.TableDescription
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.GridPane
import tornadofx.*

class QueryWindowFragment : Fragment("Query...") {
    companion object {
        @JvmField
        val SORT_KEY_AVAILABLE_OPERATORS: List<String> = listOf("=", "<", "<=", ">", ">=", "Between")

        @JvmField
        val FILTER_KEY_AVAILABLE_OPERATORS: List<String> = listOf("=", "!=", "<=", "<", ">=", ">", "Between", "Exists",
                "Not exists", "Contains", "Not contains", "Begins with")
    }

    val operation: DynamoDBOperation by param()
    val description: TableDescription by param()
    private val queryTypes: List<QueryType>
    private var queryTypeComboBox: ComboBox<QueryType> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private val queryType = SimpleObjectProperty<QueryType>()
    private val hashKey = SimpleStringProperty()
    private val sortKey = SimpleStringProperty()
    private val sortKeyOperation = SimpleStringProperty("=")
    private val sort = SimpleStringProperty("asc")

    init {
        val gsi = description.globalSecondaryIndexes.orEmpty()
        val indexQueryStrings = gsi.map { index ->
            QueryType(index.indexName, index.keySchema, true)
        }
        queryTypes = listOf(QueryType(description.tableName, description.keySchema, false), *indexQueryStrings.toTypedArray())
        queryType.value = queryTypes[0]
    }

    override val root = vbox(5.0) {
        hbox(5.0) {
            label("Query")
            queryTypeComboBox = combobox(values = queryTypes, property = queryType)
            queryTypeComboBox.valueProperty().onChange {
                val children = queryGridPane.children
                children.clear()
                hashKey.value = ""
                sortKey.value = ""
                sortKeyOperation.value = "="
                addRow(queryGridPane, it!!.keySchema)
            }
        }
        queryGridPane = gridpane {}
        addRow(queryGridPane, queryTypes[0].keySchema)
        button("Add filter") {
            setPrefSize(100.0, 40.0)
            action {

            }
        }
        separator()
        val sortGroup = ToggleGroup()
        sortGroup.bind(sort)
        hbox(5.0) {
            label("Sort")
            val asc = radiobutton("Ascending", sortGroup, "asc")
            radiobutton("Descending", sortGroup, "desc")
            sortGroup.selectToggle(asc)
        }
        separator()
        hbox(5.0) {
            alignment = Pos.CENTER
            button("Query") {
                // TODO: Extract the sizes into the constant, so allow ease of modification just in one place
                setPrefSize(100.0, 40.0)
                action {
                    println("Query:")
                    println("Hash Key = ${hashKey.value}, Sort Key ${sortKeyOperation.value} ${sortKey.value}")
                    println("Sort By ${sort.value}")
                    val qt = queryType.value
                    println(qt)
                    if (!hashKey.value.isNullOrBlank()) {
                        val attributeDefinitions = description.attributeDefinitions.associateBy({ it.attributeName }, { it.attributeType })
                        val result = operation.query(
                                description.tableName,
                                if (qt.isIndex) qt.name else null,
                                qt.hashKey.attributeName,
                                attributeDefinitions[qt.hashKey.attributeName]!!,
                                hashKey.value,
                                qt.sortKey?.attributeName,
                                attributeDefinitions[qt.sortKey?.attributeName],
                                sortKeyOperation.value,
                                sortKey.value,
                                sort.value)
                        find(QueryView::class).setQueryResult(
                                OperationType.QUERY,
                                description.tableName,
                                result)
                    }
                    close()
                }
            }
            button("Cancel") {
                setPrefSize(100.0, 40.0)
                action {
                    close()
                }
            }
        }
    }

    private fun addRow(queryGridPane: GridPane, keySchema: List<KeySchemaElement>) {
        keySchema.forEach {
            queryGridPane.row {
                val isHash = it.keyType == KeyType.HASH.name
                label(if (isHash) "Partition Key" else "Sort Key")
                text(it.attributeName)
                if (isHash) {
                    label("=")
                } else {
                    combobox(values = SORT_KEY_AVAILABLE_OPERATORS, property = sortKeyOperation)
                }
                textfield(if (isHash) hashKey else sortKey) { }
            }
        }
    }
}

private data class QueryType(val name: String, val keySchema: List<KeySchemaElement>, val isIndex: Boolean) {
    val hashKey: KeySchemaElement = keySchema[0]
    val sortKey: KeySchemaElement? = keySchema.getOrNull(1)

    override fun toString(): String {
        return (if (isIndex) "[Index]" else "[Table]") + " $name: ${joinKeySchema(keySchema)}"
    }

    private fun joinKeySchema(keySchema: List<KeySchemaElement>): String {
        return keySchema.joinToString { it.attributeName }
    }
}