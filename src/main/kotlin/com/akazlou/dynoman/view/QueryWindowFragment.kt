package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.OperationType
import com.akazlou.dynoman.domain.Operator
import com.akazlou.dynoman.domain.QueryCondition
import com.akazlou.dynoman.domain.QueryFilter
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
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
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

        const val KEY_TYPE_COLUMN_WIDTH = 90.0
        const val ATTRIBUTE_NAME_COLUMN_WIDTH = 140.0
        const val ATTRIBUTE_TYPE_COLUMN_WIDTH = 100.0
        const val ATTRIBUTE_OPERATION_COLUMN_WIDTH = 140.0
        const val ATTRIBUTE_VALUE_COLUMN_WIDTH = 200.0
    }

    enum class Mode {
        MODAL,
        INLINE
    }

    val mode: Mode by param()
    val operation: DynamoDBOperation by param()
    val description: TableDescription by param()
    private val attributeDefinitions: Map<String, String>
    private val queryTypes: List<QueryType>
    private var queryTypeComboBox: ComboBox<QueryType> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private lateinit var sortKeyTextField: TextField
    private val sortKeyBetweenHBox: HBox
    private val queryType = SimpleObjectProperty<QueryType>()
    private val hashKey = SimpleStringProperty()
    private val sortKey = SimpleStringProperty()
    private val sortKeyFrom = SimpleStringProperty()
    private val sortKeyTo = SimpleStringProperty()
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
        }.sorted()
        queryTypes = listOf(QueryType(description.tableName, description.keySchema, false), *indexQueryStrings.toTypedArray())
        queryType.value = queryTypes[0]
        attributeDefinitions = description.attributeDefinitions.associateBy({ it.attributeName }, { it.attributeType })

        val sortKeyFromTextField = TextField()
        sortKeyFromTextField.textProperty().bindBidirectional(sortKeyFrom)
        val sortKeyToTextField = TextField()
        sortKeyToTextField.textProperty().bindBidirectional(sortKeyTo)
        val andLabel = Label("And")
        andLabel.minWidth = 40.0
        andLabel.alignment = Pos.CENTER
        sortKeyBetweenHBox = HBox(sortKeyFromTextField, andLabel, sortKeyToTextField)
        sortKeyBetweenHBox.alignment = Pos.CENTER
    }

    override val root = vbox(5.0) {
        prefWidth = 750.0
        scrollpane {
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbox(5.0) {
                hbox(5.0) {
                    label("Query")
                    queryTypeComboBox = combobox(values = queryTypes, property = queryType)
                    queryTypeComboBox.valueProperty().onChange {
                        queryGridPane.removeAllRows()
                        hashKey.value = ""
                        sortKey.value = ""
                        sortKeyOperation.value = Operator.EQ
                        addKeySchemaRows(queryGridPane, it!!.keySchema)
                    }
                }
                queryGridPane = gridpane {}
                queryGridPane.hgap = 5.0
                queryGridPane.vgap = 5.0
                with(queryGridPane.columnConstraints) {
                    add(ColumnConstraints(KEY_TYPE_COLUMN_WIDTH))
                    add(ColumnConstraints(ATTRIBUTE_NAME_COLUMN_WIDTH))
                    add(ColumnConstraints(ATTRIBUTE_TYPE_COLUMN_WIDTH))
                    add(ColumnConstraints(ATTRIBUTE_OPERATION_COLUMN_WIDTH))
                    add(ColumnConstraints(ATTRIBUTE_VALUE_COLUMN_WIDTH))
                }
                addKeySchemaRows(queryGridPane, queryTypes[0].keySchema)
                button("Add filter") {
                    //setPrefSize(100.0, 40.0)
                    prefWidth = 100.0
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
                        //setPrefSize(100.0, 40.0)
                        prefWidth = 100.0
                        action {
                            println("Query:")
                            println("Hash Key = ${hashKey.value}, Sort Key ${sortKeyOperation.value} ${sortKey.value}")
                            println("Sort By ${sort.value}")
                            val qt = queryType.value
                            println(qt)
                            if (!hashKey.value.isNullOrBlank()) {
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
                                    val queryFilters = filterKeys.mapIndexed { index, property ->
                                        QueryFilter(property.value,
                                                filterKeyTypes[index].value,
                                                filterKeyOperations[index].value,
                                                filterKeyValues[index]?.value)
                                    }
                                    find(QueryView::class).setQueryResult(
                                            operation,
                                            description,
                                            OperationType.QUERY,
                                            description.tableName,
                                            qt,
                                            hashKey.value,
                                            sortKeyOperation.value,
                                            sortKey.value,
                                            sort.value,
                                            queryFilters,
                                            result)
                                } else {
                                    tab?.setQueryResult(
                                            QueryResult(
                                                    OperationType.QUERY,
                                                    description,
                                                    result))
                                }
                            }
                            if (mode == Mode.MODAL) {
                                close()
                            }
                        }
                    }
                    if (mode == Mode.MODAL) {
                        button("Cancel") {
                            //setPrefSize(pref100.0, 40.0)
                            prefWidth = 100.0
                            action {
                                close()
                            }
                        }
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

    private fun addKeySchemaRows(queryGridPane: GridPane, keySchema: List<KeySchemaElement>) {
        keysRowsCount = 0
        keySchema.forEach {
            queryGridPane.row {
                keysRowsCount++
                val isHash = it.keyType == KeyType.HASH.name
                text(if (isHash) "Partition Key" else "Sort Key")
                label(it.attributeName)
                label(when (attributeDefinitions[it.attributeName]) {
                    "S" -> "String"
                    "N" -> "Number"
                    "B" -> "Binary"
                    else -> "?"
                })
                if (isHash) {
                    label("=")
                } else {
                    val sortKeyOperationComboBox = combobox(
                            values = SORT_KEY_AVAILABLE_OPERATORS, property = sortKeyOperation)
                    sortKeyOperationComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
                    sortKeyOperationComboBox.valueProperty().addListener { _, oldValue, newValue ->
                        if (newValue == Operator.BETWEEN) {
                            val columnIndex = GridPane.getColumnIndex(sortKeyTextField)
                            val rowIndex = GridPane.getRowIndex(sortKeyTextField)
                            queryGridPane.add(sortKeyBetweenHBox, columnIndex, rowIndex)
                            queryGridPane.children.remove(sortKeyTextField)
                        }
                        if (oldValue == Operator.BETWEEN) {
                            // TODO: has to restore back the sortKeyTextField
                        }
                    }
                }
                val keyTextField = textfield(if (isHash) hashKey else sortKey) { }
                if (!isHash) {
                    sortKeyTextField = keyTextField
                }
            }
        }
    }

    private fun addFilterRow(queryGridPane: GridPane, queryFilter: QueryFilter? = null) {
        println("grid properties: ${queryGridPane.properties}")
        queryGridPane.row {
            text(if (filterKeys.isEmpty()) "Filter" else "And")
            val filterKey = SimpleStringProperty()
            filterKeys.add(filterKey)
            if (queryFilter != null) {
                filterKey.value = queryFilter.name
            }
            textfield(filterKey) { }
            val filterKeyType = SimpleObjectProperty<Type>(Type.STRING)
            filterKeyTypes.add(filterKeyType)
            if (queryFilter != null) {
                filterKeyType.value = queryFilter.type
            }
            combobox(values = FILTER_KEY_TYPES, property = filterKeyType)
            val filterKeyOperation = SimpleObjectProperty<Operator>(Operator.EQ)
            filterKeyOperations.add(filterKeyOperation)
            if (queryFilter != null) {
                filterKeyOperation.value = queryFilter.operator
            }
            val filterKeyOperationComboBox = combobox(
                    values = FILTER_KEY_AVAILABLE_OPERATORS, property = filterKeyOperation)
            filterKeyOperationComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
            val filterKeyValue = SimpleStringProperty()
            filterKeyValues.add(filterKeyValue)
            if (queryFilter != null) {
                filterKeyValue.value = queryFilter.value
            }
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
             queryFilters: List<QueryFilter>,
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
        queryFilters.forEach { addFilterRow(queryGridPane, it) }
    }
}

