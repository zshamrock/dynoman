package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.QueryCondition
import com.akazlou.dynoman.domain.QueryResult
import com.akazlou.dynoman.domain.Type
import com.akazlou.dynoman.ext.removeAllRows
import com.akazlou.dynoman.ext.removeRow
import com.akazlou.dynoman.function.Functions
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
        val SORT_KEY_AVAILABLE_OPERATORS: List<Operator> = listOf(
                Operator.EQ,
                Operator.LT,
                Operator.LE,
                Operator.GT,
                Operator.GE,
                Operator.BETWEEN)

        @JvmField
        val FILTER_KEY_AVAILABLE_OPERATORS: List<Operator> = listOf(
                Operator.EQ,
                Operator.NE,
                Operator.LT,
                Operator.LE,
                Operator.GT,
                Operator.GE,
                Operator.BETWEEN,
                Operator.EXISTS,
                Operator.NOT_EXISTS,
                Operator.CONTAINS,
                Operator.NOT_CONTAINS,
                Operator.BEGINS_WITH)

        @JvmField
        val FILTER_KEY_TYPES: List<Type> = listOf(Type.STRING, Type.NUMBER)

        const val DEFAULT_SORT_ORDER = "asc"
    }

    enum class Mode {
        MODAL,
        INLINE
    }

    val mode: Mode by param()
    val operation: DynamoDBOperation by param()
    val description: TableDescription by param()
    private val queryTypes: List<QueryType>
    private var queryTypeComboBox: ComboBox<QueryType> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private val queryType = SimpleObjectProperty<QueryType>()
    private val hashKey = SimpleStringProperty()
    private val sortKey = SimpleStringProperty()
    private val sortKeyOperation = SimpleObjectProperty<Operator>(Operator.EQ)
    private val sort = SimpleStringProperty(DEFAULT_SORT_ORDER)
    private val filterKeys = mutableListOf<SimpleStringProperty>()
    private val filterKeyTypes = mutableListOf<SimpleObjectProperty<Type>>()
    private val filterKeyOperations = mutableListOf<SimpleObjectProperty<Operator>>()
    private val filterKeyValues = mutableListOf<SimpleStringProperty?>()
    private var keysRowsCount = 0
    private val functions = Functions.getAvailableFunctions()
    private var tab: QueryTabFragment? = null

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
                queryGridPane.removeAllRows()
                hashKey.value = ""
                sortKey.value = ""
                sortKeyOperation.value = Operator.EQ
                addRow(queryGridPane, it!!.keySchema)
            }
        }
        queryGridPane = gridpane {}
        addRow(queryGridPane, queryTypes[0].keySchema)
        button("Add filter") {
            setPrefSize(100.0, 40.0)
            action {
                addFilterRow(queryGridPane)
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
                        val conditions = filterKeys.mapIndexed { index, filterKey ->
                            QueryCondition(
                                    filterKey.value,
                                    filterKeyTypes[index].value,
                                    filterKeyOperations[index].value,
                                    // TODO: Actually have to correctly handle when the value is missing
                                    filterKeyValues[index]!!.value)
                        }
                        val result = operation.query(
                                description.tableName,
                                if (qt.isIndex) qt.name else null,
                                qt.hashKey.attributeName,
                                attributeDefinitions[qt.hashKey.attributeName]!!,
                                parseValue(hashKey.value)!!,
                                qt.sortKey?.attributeName,
                                attributeDefinitions[qt.sortKey?.attributeName],
                                sortKeyOperation.value,
                                parseValue(sortKey.value),
                                sort.value,
                                conditions)
                        if (mode == Mode.MODAL) {
                            find(QueryView::class).setQueryResult(
                                    operation,
                                    description,
                                    OperationType.QUERY,
                                    description.tableName,
                                    queryType.value,
                                    hashKey.value,
                                    sortKeyOperation.value,
                                    sortKey.value,
                                    sort.value,
                                    result)
                        } else {
                            tab?.setQueryResult(QueryResult(OperationType.QUERY, description.tableName, result))
                        }
                    }
                    if (mode == Mode.MODAL) {
                        close()
                    }
                }
            }
            if (mode == Mode.MODAL) {
                button("Cancel") {
                    setPrefSize(100.0, 40.0)
                    action {
                        close()
                    }
                }
            }
        }
    }

    private fun parseValue(value: String?): String? {
        if (value == null) {
            return null
        }
        functions.forEach { function ->
            if (value.startsWith(function.name())) {
                return function.parse(value).toString()
            }
        }
        return value
    }

    private fun addRow(queryGridPane: GridPane, keySchema: List<KeySchemaElement>) {
        keysRowsCount = 0
        keySchema.forEach {
            queryGridPane.row {
                keysRowsCount++
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

    private fun addFilterRow(queryGridPane: GridPane) {
        println("grid properties: ${queryGridPane.properties}")
        queryGridPane.row {
            label(if (filterKeys.isEmpty()) "Filter" else "And")
            val filterKey = SimpleStringProperty()
            filterKeys.add(filterKey)
            textfield(filterKey) { }
            val filterKeyType = SimpleObjectProperty<Type>(Type.STRING)
            filterKeyTypes.add(filterKeyType)
            combobox(values = FILTER_KEY_TYPES, property = filterKeyType)
            val filterKeyOperation = SimpleObjectProperty<Operator>(Operator.EQ)
            filterKeyOperations.add(filterKeyOperation)
            combobox(values = FILTER_KEY_AVAILABLE_OPERATORS, property = filterKeyOperation)
            val filterKeyValue = SimpleStringProperty()
            filterKeyValues.add(filterKeyValue)
            textfield(filterKeyValue) { }
            button("x") {
                action {
                    val rowIndex = queryGridPane.removeRow(this)
                    val index = rowIndex - keysRowsCount
                    filterKeys.removeAt(index)
                    filterKeyTypes.removeAt(index)
                    filterKeyOperations.removeAt(index)
                    filterKeyValues.removeAt(index)
                }
            }
        }
    }

    fun init(operationType: OperationType,
             queryType: QueryType?,
             hashKey: String?,
             sortKeyOperation: Operator?,
             sortKey: String?,
             sort: String?,
             tab: QueryTabFragment) {
        this.tab = tab
        if (operationType == OperationType.SCAN) {
            return
        }
        queryTypeComboBox.value = queryType
        this.hashKey.value = hashKey
        this.sortKeyOperation.value = sortKeyOperation
        this.sortKey.value = sortKey
        this.sort.value = sort
    }
}

