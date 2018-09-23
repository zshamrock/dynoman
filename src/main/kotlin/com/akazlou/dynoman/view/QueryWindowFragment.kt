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
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
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
    val operationType: OperationType by param()
    val operation: DynamoDBOperation by param()
    val description: TableDescription by param()
    private val operationTypes: List<OperationType> = listOf(OperationType.SCAN, OperationType.QUERY)
    private val operationTypeProperty = SimpleObjectProperty<OperationType>(operationType)
    private val operationTypeButtonTextProperty = SimpleStringProperty(operationType.toString())
    private val attributeDefinitions: Map<String, String>
    private val queryTypes: List<QueryType>
    private var queryTypeComboBox: ComboBox<QueryType> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private var sortKeyTextField: TextField by singleAssign()
    private val sortKeyBetweenHBox: HBox
    private val sortKeyOperationComboBox: ComboBox<Operator>
    // TODO: Create 5 between hbox-es for the filters and reuse them (as we do limit support only up to 5 filters)
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
    private val filterKeyBetweenValues = mutableListOf<Pair<SimpleStringProperty, SimpleStringProperty>>()
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
        sortKeyTextField = TextField()
        sortKeyTextField.bind(sortKey)
        sortKeyBetweenHBox = HBox(sortKeyFromTextField, andLabel, sortKeyToTextField)
        sortKeyBetweenHBox.alignment = Pos.CENTER

        sortKeyOperationComboBox = ComboBox<Operator>(SORT_KEY_AVAILABLE_OPERATORS.observable())
        sortKeyOperationComboBox.bind(sortKeyOperation)
        sortKeyOperationComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
        sortKeyOperationComboBox.valueProperty()
                .addListener(this.BetweenChangeListener(sortKeyTextField, sortKeyBetweenHBox))
    }

    override val root = vbox(5.0) {
        prefWidth = 750.0
        if (mode == Mode.MODAL) {
            prefHeight = 230.0
        }
        scrollpane {
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbox(5.0) {
                hbox(5.0, Pos.CENTER_LEFT) {
                    combobox(values = operationTypes, property = operationTypeProperty) {
                        prefWidth = 100.0
                        valueProperty().onChange {
                            operationTypeButtonTextProperty.value = it.toString()
                        }
                    }
                    // TODO: handle the change - clean the query area, and update the button text
                    queryTypeComboBox = combobox(values = queryTypes, property = queryType)
                    queryTypeComboBox.prefWidth = 585.0
                    queryTypeComboBox.valueProperty().onChange {
                        sortKeyOperation.value = Operator.EQ
                        queryGridPane.removeAllRows()
                        hashKey.value = ""
                        sortKey.value = ""
                        sortKeyFrom.value = ""
                        sortKeyTo.value = ""
                        filterKeys.clear()
                        filterKeyTypes.clear()
                        filterKeyOperations.clear()
                        filterKeyValues.clear()
                        filterKeyBetweenValues.clear()
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
                    text("Sort")
                    val asc = radiobutton("Ascending", sortGroup, "asc")
                    radiobutton("Descending", sortGroup, "desc")
                    sortGroup.selectToggle(asc)
                }
                separator()
                hbox(5.0) {
                    alignment = Pos.CENTER
                    button(operationTypeButtonTextProperty) {
                        // TODO: Extract the sizes into the constant, so allow ease of modification just in one place
                        //setPrefSize(100.0, 40.0)
                        prefWidth = 100.0
                        action {
                            println("Query:")
                            val skOp = sortKeyOperation.value
                            val skValues = when {
                                skOp.isBetween() ->
                                    if (sortKeyFrom.value == null || sortKeyTo.value == null) {
                                        emptyList()
                                    } else {
                                        listOf(parseValue(sortKeyFrom.value)!!, parseValue(sortKeyTo.value)!!)
                                    }
                                else ->
                                    if (sortKey.value.isNullOrEmpty()) {
                                        emptyList()
                                    } else {
                                        listOf(parseValue(sortKey.value)!!)
                                    }
                            }
                            println("Hash Key = ${hashKey.value}, Sort Key $skOp ${sortKey.value}")
                            println("Sort By ${sort.value}")
                            val qt = queryType.value
                            println(qt)
                            if (!hashKey.value.isNullOrBlank()) {
                                val conditions = filterKeys.mapIndexed { index, filterKey ->
                                    val filterKeyOperation = filterKeyOperations[index].value
                                    QueryCondition(
                                            filterKey.value,
                                            filterKeyTypes[index].value,
                                            filterKeyOperation,
                                            if (filterKeyOperation.isBetween()) {
                                                val betweenPair = filterKeyBetweenValues[index]
                                                listOf(betweenPair.first.value, betweenPair.second.value)
                                            } else {
                                                listOf(filterKeyValues[index]!!.value)
                                            })
                                }
                                val result = operation.query(
                                        description.tableName,
                                        if (qt.isIndex) qt.name else null,
                                        qt.hashKey.attributeName,
                                        attributeDefinitions[qt.hashKey.attributeName]!!,
                                        parseValue(hashKey.value)!!,
                                        qt.sortKey?.attributeName,
                                        attributeDefinitions[qt.sortKey?.attributeName],
                                        skOp,
                                        skValues,
                                        sort.value,
                                        conditions)
                                if (mode == Mode.MODAL) {
                                    val queryFilters = filterKeys.mapIndexed { index, property ->
                                        val filterKeyOperation = filterKeyOperations[index].value
                                        QueryFilter(property.value,
                                                filterKeyTypes[index].value,
                                                filterKeyOperation,
                                                if (filterKeyOperation.isBetween()) {
                                                    val betweenPair = filterKeyBetweenValues[index]
                                                    listOf(betweenPair.first.value, betweenPair.second.value)
                                                } else {
                                                    listOf(filterKeyValues[index]?.value)
                                                })
                                    }
                                    println("skValues: $skValues")
                                    println(sortKey)
                                    find(QueryView::class).setQueryResult(
                                            operation,
                                            description,
                                            OperationType.QUERY,
                                            description.tableName,
                                            qt,
                                            hashKey.value,
                                            skOp,
                                            skValues,
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
                    sortKeyOperationComboBox.attachTo(this)
                }
                if (isHash) {
                    textfield(hashKey) { }
                } else {
                    sortKeyTextField.attachTo(this)
                }
            }
        }
    }

    private fun addFilterRow(queryGridPane: GridPane, queryFilter: QueryFilter? = null) {
        println("grid properties: ${queryGridPane.properties}")
        queryGridPane.row {
            text(if (filterKeys.isEmpty()) "Filter" else "And")
            val filterKey = SimpleStringProperty(queryFilter?.name)
            filterKeys.add(filterKey)
            textfield(filterKey) { }
            val filterKeyType = SimpleObjectProperty<Type>(queryFilter?.type?:Type.STRING)
            filterKeyTypes.add(filterKeyType)
            combobox(values = FILTER_KEY_TYPES, property = filterKeyType)
            val filterKeyOperation = SimpleObjectProperty<Operator>(queryFilter?.operator?:Operator.EQ)
            filterKeyOperations.add(filterKeyOperation)
            val filterKeyOperationComboBox = combobox(
                    values = FILTER_KEY_AVAILABLE_OPERATORS, property = filterKeyOperation)
            filterKeyOperationComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
            val filterKeyValue = SimpleStringProperty()
            filterKeyValues.add(filterKeyValue)
            val filterKeyValueTextField = TextField()
            filterKeyValueTextField.bind(filterKeyValue)
            val filterKeyFromTextField = TextField()
            val filterKeyFrom = SimpleStringProperty()
            filterKeyFromTextField.textProperty().bindBidirectional(filterKeyFrom)
            val filterKeyToTextField = TextField()
            val filterKeyTo = SimpleStringProperty()
            filterKeyToTextField.textProperty().bindBidirectional(filterKeyTo)
            val andLabel = Label("And")
            andLabel.minWidth = 40.0
            andLabel.alignment = Pos.CENTER
            val filterKeyBetweenHBox = HBox(filterKeyFromTextField, andLabel, filterKeyToTextField)
            filterKeyBetweenHBox.alignment = Pos.CENTER
            filterKeyBetweenValues.add(Pair(filterKeyFrom, filterKeyTo))
            filterKeyOperationComboBox.valueProperty()
                    .addListener(this@QueryWindowFragment.BetweenChangeListener(
                            filterKeyValueTextField, filterKeyBetweenHBox))
            if (filterKeyOperation.value.isBetween()) {
                filterKeyFrom.value = queryFilter?.values?.get(0)
                filterKeyTo.value = queryFilter?.values?.get(1)
                filterKeyBetweenHBox.attachTo(this)
            } else {
                filterKeyValue.value = queryFilter?.values?.get(0)
                filterKeyValueTextField.attachTo(this)
            }
            button("x") {
                action {
                    val rowIndex = queryGridPane.removeRow(this)
                    val index = rowIndex - keysRowsCount
                    filterKeys.removeAt(index)
                    filterKeyTypes.removeAt(index)
                    filterKeyOperations.removeAt(index)
                    filterKeyValues.removeAt(index)
                    filterKeyBetweenValues.removeAt(index)
                }
            }
        }
    }

    fun init(operationType: OperationType,
             queryType: QueryType?,
             hashKey: String?,
             sortKeyOperation: Operator?,
             sortKeyValues: List<String>,
             sort: String?,
             queryFilters: List<QueryFilter>,
             tab: QueryTabFragment) {
        this.tab = tab
        if (operationType == OperationType.SCAN) {
            return
        }
        println("Sort key values: $sortKeyValues")
        queryTypeComboBox.value = queryType
        this.hashKey.value = hashKey
        this.sortKeyOperation.value = sortKeyOperation
        if (sortKeyOperation != null && sortKeyOperation.isBetween()) {
            this.sortKeyFrom.value = sortKeyValues.getOrNull(0)
            this.sortKeyTo.value = sortKeyValues.getOrNull(1)
        } else {
            this.sortKey.value = sortKeyValues.getOrNull(0)
        }
        this.sort.value = sort
        queryFilters.forEach { addFilterRow(queryGridPane, it) }
    }

    inner class BetweenChangeListener(private val textField: TextField,
                                      private val betweenHBox: HBox) : ChangeListener<Operator> {
        override fun changed(observable: ObservableValue<out Operator>, oldValue: Operator, newValue: Operator) {
            if (newValue.isBetween()) {
                val columnIndex = GridPane.getColumnIndex(textField)
                val rowIndex = GridPane.getRowIndex(textField)
                queryGridPane.add(betweenHBox, columnIndex, rowIndex)
                queryGridPane.children.remove(textField)
            }
            if (oldValue.isBetween()) {
                val columnIndex = GridPane.getColumnIndex(betweenHBox)
                val rowIndex = GridPane.getRowIndex(betweenHBox)
                queryGridPane.add(textField, columnIndex, rowIndex)
                queryGridPane.children.remove(betweenHBox)
            }
        }
    }
}

