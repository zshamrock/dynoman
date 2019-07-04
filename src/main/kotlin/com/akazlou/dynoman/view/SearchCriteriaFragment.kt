package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.SearchSource
import com.akazlou.dynoman.domain.search.Condition
import com.akazlou.dynoman.domain.search.Operator
import com.akazlou.dynoman.domain.search.Order
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.SearchType
import com.akazlou.dynoman.domain.search.Type
import com.akazlou.dynoman.function.Functions
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
import javafx.scene.control.TextField
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import tornadofx.*

class SearchCriteriaFragment : Fragment("Search") {
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

        const val KEY_TYPE_COLUMN_WIDTH = 90.0
        const val ATTRIBUTE_NAME_COLUMN_WIDTH = 140.0
        const val ATTRIBUTE_TYPE_COLUMN_WIDTH = 100.0
        const val ATTRIBUTE_OPERATION_COLUMN_WIDTH = 140.0
        const val ATTRIBUTE_VALUE_COLUMN_WIDTH = 200.0

        const val FILTER_PRIMARY_LABEL = "Filter"
        const val FILTER_SECONDARY_LABEL = "And"

        // TODO: Replace all references for the params (in all classes) by using constant keys
        const val SEARCH_TYPE_PARAM = "searchType"
    }

    val description: TableDescription by param()
    private val searchTypes: List<SearchType> = listOf(SearchType.SCAN, SearchType.QUERY)
    private val searchTypeProperty = SimpleObjectProperty(params["searchType"] as SearchType)
    private val attributeDefinitionTypes: Map<String, Type>
    private val searchSources: List<SearchSource>
    private var searchSourceComboBox: ComboBox<SearchSource> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private val functions = Functions.getAvailableFunctions()
    private var sortKeyTextField: TextField by singleAssign()
    private val sortKeyBetweenHBox: HBox
    private val sortKeyOperatorComboBox: ComboBox<Operator>
    // TODO: Create 5 between hbox-es for the filters and reuse them (as we do limit support only up to 5 filters)
    private val searchSourceProperty = SimpleObjectProperty<SearchSource>()
    private val hashKeyValueProperty = SimpleStringProperty("")
    private val sortKeyProperty = SimpleStringProperty("")
    private val sortKeyFromProperty = SimpleStringProperty("")
    private val sortKeyToProperty = SimpleStringProperty("")
    private val sortKeyOperatorProperty = SimpleObjectProperty<Operator>(Operator.EQ)
    private val orderProperty = SimpleObjectProperty<Order>(Order.ASC)
    private val filterKeys = mutableListOf<SimpleStringProperty>()
    private val filterLabels = mutableListOf<SimpleStringProperty>()
    private val filterKeyTypes = mutableListOf<SimpleObjectProperty<Type>>()
    private val filterKeyOperators = mutableListOf<SimpleObjectProperty<Operator>>()
    private val filterKeyValues = mutableListOf<SimpleStringProperty?>()
    private val filterKeyBetweenValues = mutableListOf<Pair<SimpleStringProperty, SimpleStringProperty>>()
    private var keysRowsCount = 0
    @Suppress("UNCHECKED_CAST")
    private val attributes: List<String> = (params["attributes"] as? List<String>?).orEmpty()

    init {
        val gsi = description.globalSecondaryIndexes.orEmpty()
        val indexQueryStrings = gsi.map { index ->
            SearchSource(index.indexName, index.keySchema, true)
        }.sorted()
        searchSources = listOf(SearchSource(description.tableName, description.keySchema, false), *indexQueryStrings.toTypedArray())
        searchSourceProperty.value = searchSources[0]
        attributeDefinitionTypes = description.attributeDefinitions.associateBy({ it.attributeName }, { Type.fromString(it.attributeType) })

        val sortKeyFromTextField = TextField()
        sortKeyFromTextField.textProperty().bindBidirectional(sortKeyFromProperty)
        val sortKeyToTextField = TextField()
        sortKeyToTextField.textProperty().bindBidirectional(sortKeyToProperty)
        val andLabel = Label("And")
        andLabel.minWidth = 40.0
        andLabel.alignment = Pos.CENTER
        sortKeyTextField = TextField()
        sortKeyTextField.bind(sortKeyProperty)
        sortKeyBetweenHBox = HBox(sortKeyFromTextField, andLabel, sortKeyToTextField)
        sortKeyBetweenHBox.alignment = Pos.CENTER

        sortKeyOperatorComboBox = ComboBox<Operator>(SORT_KEY_AVAILABLE_OPERATORS.observable())
        sortKeyOperatorComboBox.bind(sortKeyOperatorProperty)
        sortKeyOperatorComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
        sortKeyOperatorComboBox.valueProperty()
                .addListener(this.OperatorChangeListener(
                        sortKeyOperatorComboBox,
                        sortKeyTextField,
                        sortKeyProperty,
                        sortKeyBetweenHBox,
                        sortKeyFromProperty,
                        sortKeyToProperty))
    }

    override val root = vbox(5.0) {
        hbox(5.0, Pos.CENTER_LEFT) {
            combobox(values = searchTypes, property = searchTypeProperty) {
                prefWidth = 100.0
                valueProperty().onChange { searchType ->
                    when (searchType) {
                        SearchType.QUERY -> {
                            val filters = cleanQueryGridPane()
                            addKeySchemaRows(searchType, queryGridPane, searchSourceProperty.value.keySchema)
                            filters.forEach { filter ->
                                addFilterRow(queryGridPane, filter)
                            }
                        }
                        SearchType.SCAN -> {
                            val filters = cleanQueryGridPane()
                            filters.forEach { filter ->
                                addFilterRow(queryGridPane, filter)
                            }
                        }
                    }
                }
            }
            searchSourceComboBox = combobox(values = searchSources, property = searchSourceProperty)
            searchSourceComboBox.prefWidth = 585.0
            searchSourceComboBox.valueProperty().onChange {
                cleanQueryGridPane()
                addKeySchemaRows(searchTypeProperty.value, queryGridPane, it!!.keySchema)
            }
        }
        queryGridPane = gridpane {}
        queryGridPane.hgap = 5.0
        queryGridPane.vgap = 5.0
        with(queryGridPane.columnConstraints) {
            add(javafx.scene.layout.ColumnConstraints(KEY_TYPE_COLUMN_WIDTH))
            add(javafx.scene.layout.ColumnConstraints(ATTRIBUTE_NAME_COLUMN_WIDTH))
            add(javafx.scene.layout.ColumnConstraints(ATTRIBUTE_TYPE_COLUMN_WIDTH))
            add(javafx.scene.layout.ColumnConstraints(ATTRIBUTE_OPERATION_COLUMN_WIDTH))
            add(javafx.scene.layout.ColumnConstraints(ATTRIBUTE_VALUE_COLUMN_WIDTH))
        }
        addKeySchemaRows(searchTypeProperty.value, queryGridPane, searchSources[0].keySchema)
        button("Add filter") {
            action {
                addFilterRow(queryGridPane)
            }
        }
        // TODO: Sorting indeed doesn't apply to the SCAN or if it is then we can keep it
        separator()
        val orderGroup = ToggleGroup()
        orderGroup.bind(orderProperty)
        hbox(5.0) {
            text("Sort")
            val asc = radiobutton("Ascending", orderGroup, Order.ASC)
            radiobutton("Descending", orderGroup, Order.DESC)
            orderGroup.selectToggle(asc)
        }
    }

    private fun cleanQueryGridPane(): List<Condition> {
        sortKeyOperatorProperty.value = Operator.EQ
        queryGridPane.removeAllRows()
        keysRowsCount = 0
        hashKeyValueProperty.value = ""
        sortKeyProperty.value = ""
        sortKeyFromProperty.value = ""
        sortKeyToProperty.value = ""
        // Collect current filters into the QueryFilter-s before clean them up
        val filters = mutableListOf<Condition>()
        filterKeys.forEachIndexed { index, filterKey ->
            val filterKeyOperator = filterKeyOperators[index].value
            filters.add(Condition(
                    filterKey.value.orEmpty(),
                    filterKeyTypes[index].value,
                    filterKeyOperator,
                    when {
                        filterKeyOperator.isNoArg() -> emptyList()
                        filterKeyOperator.isBetween() -> filterKeyBetweenValues[index].toList().map { it.value.orEmpty() }
                        else -> listOf(filterKeyValues[index]?.value.orEmpty())
                    }))
        }
        filterLabels.clear()
        filterKeys.clear()
        filterKeyTypes.clear()
        filterKeyOperators.clear()
        filterKeyValues.clear()
        filterKeyBetweenValues.clear()
        return filters
    }

    private fun parseValue(value: String?): String {
        if (value == null) {
            return ""
        }
        functions.forEach { function ->
            if (value.startsWith(function.name())) {
                return function.parse(value).toString()
            }
        }
        return value
    }

    private fun addKeySchemaRows(searchType: SearchType, queryGridPane: GridPane, keySchema: List<KeySchemaElement>) {
        keysRowsCount = 0
        if (searchType == SearchType.SCAN) {
            return
        }
        keySchema.forEach {
            queryGridPane.row {
                keysRowsCount++
                val isHash = it.keyType == KeyType.HASH.name
                text(if (isHash) "Partition Key" else "Sort Key")
                label(it.attributeName)
                label(when (attributeDefinitionTypes[it.attributeName]) {
                    Type.STRING -> "String"
                    Type.NUMBER -> "Number"
                    Type.BINARY -> "Binary"
                    else -> "?"
                })
                if (isHash) {
                    label("=")
                } else {
                    sortKeyOperatorComboBox.attachTo(this)
                }
                if (isHash) {
                    if (attributes.isEmpty()) {
                        textfield(hashKeyValueProperty) { }
                    } else {
                        combobox(values = attributes, property = hashKeyValueProperty) {
                            prefWidth = ATTRIBUTE_VALUE_COLUMN_WIDTH
                            isEditable = true
                        }
                    }
                } else {
                    sortKeyTextField.attachTo(this)
                }
            }
        }
    }

    private fun addFilterRow(queryGridPane: GridPane, condition: Condition? = null) {
        println("grid properties: ${queryGridPane.properties}")
        queryGridPane.row {
            val filterLabel = SimpleStringProperty(
                    if (filterKeys.isEmpty()) FILTER_PRIMARY_LABEL else FILTER_SECONDARY_LABEL)
            filterLabels.add(filterLabel)
            text(filterLabel)
            val filterKey = SimpleStringProperty(condition?.name)
            filterKeys.add(filterKey)
            textfield(filterKey) { }
            val filterKeyType = SimpleObjectProperty<Type>(condition?.type ?: Type.STRING)
            filterKeyTypes.add(filterKeyType)
            combobox(values = FILTER_KEY_TYPES, property = filterKeyType)
            val filterKeyOperation = SimpleObjectProperty<Operator>(condition?.operator ?: Operator.EQ)
            filterKeyOperators.add(filterKeyOperation)
            val filterKeyOperationComboBox = combobox(
                    values = FILTER_KEY_AVAILABLE_OPERATORS, property = filterKeyOperation)
            filterKeyOperationComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
            val filterKeyValue = SimpleStringProperty("")
            filterKeyValues.add(filterKeyValue)
            val filterKeyValueTextField = TextField()
            filterKeyValueTextField.bind(filterKeyValue)
            val filterKeyFromTextField = TextField()
            val filterKeyFrom = SimpleStringProperty("")
            filterKeyFromTextField.textProperty().bindBidirectional(filterKeyFrom)
            val filterKeyToTextField = TextField()
            val filterKeyTo = SimpleStringProperty("")
            filterKeyToTextField.textProperty().bindBidirectional(filterKeyTo)
            val andLabel = Label("And")
            andLabel.minWidth = 40.0
            andLabel.alignment = Pos.CENTER
            val filterKeyBetweenHBox = HBox(filterKeyFromTextField, andLabel, filterKeyToTextField)
            filterKeyBetweenHBox.alignment = Pos.CENTER
            filterKeyBetweenValues.add(Pair(filterKeyFrom, filterKeyTo))
            filterKeyOperationComboBox.valueProperty()
                    .addListener(this@SearchCriteriaFragment.OperatorChangeListener(
                            filterKeyOperationComboBox,
                            filterKeyValueTextField,
                            filterKeyValue,
                            filterKeyBetweenHBox,
                            filterKeyFrom,
                            filterKeyTo))
            if (filterKeyOperation.value.isBetween()) {
                filterKeyFrom.value = condition?.values?.get(0)
                filterKeyTo.value = condition?.values?.get(1)
                filterKeyBetweenHBox.attachTo(this)
            } else if (!filterKeyOperation.value.isNoArg()) {
                filterKeyValue.value = condition?.values?.get(0)
                filterKeyValueTextField.attachTo(this)
            }
            button("x") {
                addClass("button-x")
                action {
                    val rowIndex = queryGridPane.removeRow(this)
                    val index = rowIndex - keysRowsCount
                    println("Remove index $index")
                    filterLabels.removeAt(index)
                    filterKeys.removeAt(index)
                    filterKeyTypes.removeAt(index)
                    filterKeyOperators.removeAt(index)
                    filterKeyValues.removeAt(index)
                    filterKeyBetweenValues.removeAt(index)
                    if (index == 0 && filterLabels.isNotEmpty()) {
                        filterLabels[0].value = FILTER_PRIMARY_LABEL
                    }
                }
            }
        }
    }

    fun init(search: Search) {
        when (search) {
            is ScanSearch -> {
                search.filters.forEach { addFilterRow(queryGridPane, it) }
            }
            is QuerySearch -> {
                println("Sort key values: $search.sortKeyValues")
                val hashKeySchemaElement = KeySchemaElement(search.getHashKeyName(), KeyType.HASH)
                val rangeKeySchemaElement = if (search.getRangeKeyName() == null) {
                    null
                } else {
                    KeySchemaElement(search.getRangeKeyName(), KeyType.RANGE)
                }
                searchSourceComboBox.value = SearchSource(
                        search.index ?: search.table,
                        if (rangeKeySchemaElement == null) {
                            listOf(hashKeySchemaElement)
                        } else {
                            listOf(hashKeySchemaElement, rangeKeySchemaElement)
                        },
                        search.index != null)
                this.hashKeyValueProperty.value = search.getHashKeyValue()
                val rangeKeyOperator = search.getRangeKeyOperator()
                this.sortKeyOperatorProperty.value = rangeKeyOperator
                if (rangeKeyOperator.isBetween()) {
                    this.sortKeyFromProperty.value = search.getRangeKeyValues()[0]
                    this.sortKeyToProperty.value = search.getRangeKeyValues()[1]
                } else {
                    this.sortKeyProperty.value = search.getRangeKeyValues().getOrNull(0).orEmpty()
                }
                this.orderProperty.value = search.order
                search.filters.forEach { addFilterRow(queryGridPane, it) }
            }
        }
    }

    // TODO: Compare getSearch() and getSearch2() functions
    fun getSearch(): Search {
        val searchSource = searchSourceProperty.value
        val tableName = description.tableName
        val index = if (searchSource.isIndex) {
            searchSource.name
        } else {
            null
        }
        return when (searchTypeProperty.value!!) {
            SearchType.SCAN -> {
                ScanSearch(tableName, index, buildSearchFilters())
            }
            SearchType.QUERY -> {
                val hashKeyName = searchSource.hashKey.attributeName
                val sortKey = searchSource.sortKey
                val sortQueryCondition = if (sortKey == null) {
                    null
                } else {
                    val sortKeyName = sortKey.attributeName
                    val sortKeyOperator = sortKeyOperatorProperty.value
                    val sortKeyValues = if (sortKeyOperator.isBetween()) {
                        listOf(sortKeyFromProperty.value, sortKeyToProperty.value)
                    } else {
                        listOf(sortKeyProperty.value)
                    }
                    Condition(
                            sortKeyName,
                            attributeDefinitionTypes.getValue(sortKeyName),
                            sortKeyOperator,
                            sortKeyValues)
                }
                QuerySearch(
                        tableName,
                        index,
                        Condition.hashKey(
                                hashKeyName,
                                attributeDefinitionTypes.getValue(hashKeyName),
                                hashKeyValueProperty.value),
                        sortQueryCondition,
                        buildSearchFilters(),
                        orderProperty.value)
            }
        }
    }

    fun getSearch2(): Search? {
        val searchType = searchTypeProperty.value
        println("Search: ${searchTypeProperty.value}")
        val skOp = sortKeyOperatorProperty.value
        val skValues = when {
            skOp.isBetween() ->
                if (sortKeyFromProperty.value == null || sortKeyToProperty.value == null) {
                    emptyList()
                } else {
                    listOf(parseValue(sortKeyFromProperty.value), parseValue(sortKeyToProperty.value))
                }
            else ->
                if (sortKeyProperty.value.isNullOrEmpty()) {
                    emptyList()
                } else {
                    listOf(parseValue(sortKeyProperty.value))
                }
        }
        println("Hash Key = ${hashKeyValueProperty.value}, Sort Key $skOp ${sortKeyProperty.value}")
        println("Sort By ${orderProperty.value}")
        val qt = searchSourceProperty.value
        println(qt)
        if (!hashKeyValueProperty.value.isNullOrBlank() || searchType.isScan()) {
            val conditions = filterKeys.mapIndexed { index, filterKey ->
                val filterKeyOperation = filterKeyOperators[index].value
                Condition(
                        filterKey.value,
                        filterKeyTypes[index].value,
                        filterKeyOperation,
                        if (filterKeyOperation.isBetween()) {
                            val betweenPair = filterKeyBetweenValues[index]
                            listOf(parseValue(betweenPair.first.value),
                                    parseValue(betweenPair.second.value))
                        } else {
                            listOf(parseValue(filterKeyValues[index]!!.value))
                        })
            }

            if (searchType.isScan()) {
                val search = ScanSearch(
                        description.tableName,
                        if (qt.isIndex) qt.name else null,
                        conditions)
                return search
            } else {
                val hashKey = Condition(
                        qt.hashKey.attributeName,
                        attributeDefinitionTypes[qt.hashKey.attributeName]!!,
                        Operator.EQ,
                        listOf(parseValue(hashKeyValueProperty.value)))
                val rangeKey = if (qt.sortKey == null) {
                    null
                } else {
                    Condition(
                            qt.sortKey.attributeName!!,
                            attributeDefinitionTypes.getValue(qt.sortKey.attributeName),
                            skOp,
                            skValues)
                }
                val search = QuerySearch(
                        description.tableName,
                        if (qt.isIndex) qt.name else null,
                        hashKey,
                        rangeKey,
                        conditions,
                        orderProperty.value
                )
                return search
            }
        }
        return null
    }

    private fun buildSearchFilters(): List<Condition> {
        // TODO: Test various scenarios when the query filter row is incomplete in multiple ways
        // TODO: Duplicate of the duplicate results in the strange behaviour, looks like it gets the value from the original tab
        return filterKeys.mapIndexed { index, key ->
            val operator = filterKeyOperators[index].value
            Condition(key.value,
                    filterKeyTypes[index].value,
                    operator,
                    if (operator.isBetween()) {
                        val values = filterKeyBetweenValues[index]
                        listOf(values.first.value, values.second.value)
                    } else {
                        listOf(filterKeyValues[index]!!.value)
                    })
        }
    }

    inner class OperatorChangeListener(private val operators: ComboBox<Operator>,
                                       private val textField: TextField,
                                       private val textFieldValue: SimpleStringProperty,
                                       private val betweenHBox: HBox,
                                       private val textFieldFrom: SimpleStringProperty,
                                       private val textFieldTo: SimpleStringProperty) : ChangeListener<Operator> {
        override fun changed(observable: ObservableValue<out Operator>, oldValue: Operator, newValue: Operator) {
            when (newValue) {
                Operator.BETWEEN -> {
                    val columnIndex = GridPane.getColumnIndex(textField)
                    val rowIndex = GridPane.getRowIndex(textField)
                    queryGridPane.add(betweenHBox, columnIndex, rowIndex)
                    queryGridPane.children.remove(textField)
                    textFieldValue.value = ""
                }
                Operator.EXISTS, Operator.NOT_EXISTS -> {
                    if (oldValue.isNoArg()) {
                        // The textfield then has been already removed
                        return
                    }
                    queryGridPane.children.remove(textField)
                    textFieldValue.value = ""
                }
                else -> {
                    // noop
                }
            }
            when (oldValue) {
                Operator.BETWEEN -> {
                    val columnIndex = GridPane.getColumnIndex(betweenHBox)
                    val rowIndex = GridPane.getRowIndex(betweenHBox)
                    queryGridPane.add(textField, columnIndex, rowIndex)
                    queryGridPane.children.remove(betweenHBox)
                    textFieldFrom.value = ""
                    textFieldTo.value = ""
                }
                Operator.EXISTS, Operator.NOT_EXISTS -> {
                    if (newValue.isNoArg()) {
                        return
                    }
                    val columnIndex = GridPane.getColumnIndex(operators)
                    val rowIndex = GridPane.getRowIndex(operators)
                    queryGridPane.add(textField, columnIndex + 1, rowIndex)
                }
                else -> {
                    // noop
                }
            }
        }
    }
}
