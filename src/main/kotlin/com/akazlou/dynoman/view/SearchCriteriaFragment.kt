package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.ManagedEnvironmentsController
import com.akazlou.dynoman.domain.ManagedEnvironment
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
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.Node
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
        val FILTER_KEY_TYPES: List<Type> = listOf(Type.STRING, Type.BINARY, Type.NUMBER, Type.BOOLEAN, Type.NULL)

        const val KEY_TYPE_COLUMN_WIDTH = 90.0
        const val ATTRIBUTE_NAME_COLUMN_WIDTH = 140.0
        const val ATTRIBUTE_TYPE_COLUMN_WIDTH = 100.0
        const val ATTRIBUTE_OPERATION_COLUMN_WIDTH = 140.0
        const val ATTRIBUTE_VALUE_COLUMN_WIDTH = 200.0
        const val AND_LABEL_MIN_WIDTH = 40.0

        const val FILTER_PRIMARY_LABEL = "Filter"
        const val FILTER_SECONDARY_LABEL = "And"

        // TODO: Replace all references for the params (in all classes) by using constant keys
        const val SEARCH_TYPE_PARAM = "searchType"
    }

    private enum class Mode {
        NORMAL,
        FOREIGN
    }

    val description: TableDescription by param()
    private val managedEnvironmentsController: ManagedEnvironmentsController by inject()
    private val searchTypes: List<SearchType> = listOf(SearchType.SCAN, SearchType.QUERY)
    private val searchTypeProperty = SimpleObjectProperty(params["searchType"] as SearchType)
    private val attributeDefinitionTypes: Map<String, Type>
    private val searchSources: List<SearchSource>
    private var searchSourceComboBox: ComboBox<SearchSource> by singleAssign()
    private var queryGridPane: GridPane by singleAssign()
    private val functions = Functions.getAvailableFunctions()
    private var sortKeyTextField: TextField by singleAssign()
    private var sortKeyComboBox: ComboBox<String> by singleAssign()
    private val sortKeyBetweenHBox: HBox
    private val sortKeyOperatorComboBox: ComboBox<Operator>
    // TODO: Create 5 between hbox-es for the filters and reuse them (as we do limit support only up to 5 filters)
    private val searchSourceProperty = SimpleObjectProperty<SearchSource>()
    private val hashKeyValueProperty = SimpleStringProperty("")
    private val sortKeyOperators = mutableListOf<Operator>().asObservable()
    private val sortKeyProperty = SimpleStringProperty("")
    private val sortKeyFromProperty = SimpleStringProperty("")
    private val sortKeyToProperty = SimpleStringProperty("")
    private val sortKeyOperatorProperty = SimpleObjectProperty<Operator>(Operator.EQ)
    private val orderProperty = SimpleObjectProperty<Order>(Order.ASC)
    private val filterKeyProperties = mutableListOf<SimpleStringProperty>()
    private val filterLabelProperties = mutableListOf<SimpleStringProperty>()
    private val filterKeyTypeProperties = mutableListOf<SimpleObjectProperty<Type>>()
    private val filterKeyOperatorProperties = mutableListOf<SimpleObjectProperty<Operator>>()
    private val filterKeyValueProperties = mutableListOf<SimpleStringProperty?>()
    private val filterKeyBetweenValueProperties = mutableListOf<Pair<SimpleStringProperty, SimpleStringProperty>>()
    private var keysRowsCount = 0
    @Suppress("UNCHECKED_CAST")
    private val attributes: ObservableList<String> = (params["attributes"] as? List<String>?).orEmpty().asObservable()
    private val mode = if (attributes.isEmpty()) Mode.NORMAL else Mode.FOREIGN

    init {
        val gsi = description.globalSecondaryIndexes.orEmpty()
        val indexQueryStrings = gsi.map { index ->
            SearchSource(index.indexName, index.keySchema, true)
        }.sorted()
        searchSources = listOf(SearchSource(description.tableName, description.keySchema, false), *indexQueryStrings.toTypedArray())
        searchSourceProperty.value = searchSources[0]
        attributeDefinitionTypes = description.attributeDefinitions.associateBy({ it.attributeName }, { Type.fromString(it.attributeType) })

        val andLabel = Label("And").apply {
            minWidth = AND_LABEL_MIN_WIDTH
            alignment = Pos.CENTER
        }
        sortKeyBetweenHBox = when (mode) {
            Mode.NORMAL -> {
                val sortKeyFromTextField = TextField()
                sortKeyFromTextField.textProperty().bindBidirectional(sortKeyFromProperty)
                val sortKeyToTextField = TextField()
                sortKeyToTextField.textProperty().bindBidirectional(sortKeyToProperty)
                HBox(sortKeyFromTextField, andLabel, sortKeyToTextField)
            }
            Mode.FOREIGN -> {
                val sortKeyFromComboBox = createAttributesComboBox(sortKeyFromProperty)
                val sortKeyToComboBox = createAttributesComboBox(sortKeyToProperty)
                HBox(sortKeyFromComboBox, andLabel, sortKeyToComboBox)
            }
        }
        sortKeyBetweenHBox.alignment = Pos.CENTER
        sortKeyTextField = TextField()
        sortKeyTextField.bind(sortKeyProperty)
        sortKeyComboBox = createAttributesComboBox(sortKeyProperty)

        sortKeyOperatorComboBox = ComboBox<Operator>(sortKeyOperators)
        sortKeyOperatorComboBox.bind(sortKeyOperatorProperty)
        sortKeyOperatorComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
        sortKeyOperatorComboBox.valueProperty()
                .addListener(this.OperatorChangeListener(
                        sortKeyOperatorComboBox,
                        if (mode == Mode.NORMAL) {
                            sortKeyTextField
                        } else {
                            sortKeyComboBox
                        },
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
        filterKeyProperties.forEachIndexed { index, filterKey ->
            val filterKeyOperator = filterKeyOperatorProperties[index].value
            filters.add(Condition(
                    filterKey.value.orEmpty(),
                    filterKeyTypeProperties[index].value,
                    filterKeyOperator,
                    when {
                        filterKeyOperator.isNoArg() -> emptyList()
                        filterKeyOperator.isBetween() -> filterKeyBetweenValueProperties[index].toList().map { it.value.orEmpty() }
                        else -> listOf(filterKeyValueProperties[index]?.value.orEmpty())
                    }))
        }
        filterLabelProperties.clear()
        filterKeyProperties.clear()
        filterKeyTypeProperties.clear()
        filterKeyOperatorProperties.clear()
        filterKeyValueProperties.clear()
        filterKeyBetweenValueProperties.clear()
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
        if (value.startsWith(ManagedEnvironment.ENV_PREFIX) && value.endsWith(ManagedEnvironment.ENV_SUFFIX)) {
            return managedEnvironmentsController.get(ManagedEnvironment.GLOBALS).get(value).orEmpty()
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
                val attributeType = attributeDefinitionTypes[it.attributeName] as Type
                label(attributeType.toString())
                if (isHash) {
                    label("=")
                } else {
                    sortKeyOperators.setAll(attributeType.sortOperators)
                    sortKeyOperatorComboBox.attachTo(this)
                }
                if (isHash) {
                    if (mode == Mode.NORMAL) {
                        textfield(hashKeyValueProperty) { }
                    } else {
                        createAttributesComboBox(hashKeyValueProperty).attachTo(this)
                    }
                } else {
                    if (mode == Mode.NORMAL) {
                        sortKeyTextField.attachTo(this)
                    } else {
                        sortKeyComboBox.attachTo(this)
                    }
                }
            }
        }
    }

    private fun addFilterRow(queryGridPane: GridPane, condition: Condition? = null) {
        println("grid properties: ${queryGridPane.properties}")
        queryGridPane.row {
            val filterLabel = SimpleStringProperty(
                    if (filterKeyProperties.isEmpty()) FILTER_PRIMARY_LABEL else FILTER_SECONDARY_LABEL)
            filterLabelProperties.add(filterLabel)
            text(filterLabel)
            val filterKey = SimpleStringProperty(condition?.name)
            filterKeyProperties.add(filterKey)
            textfield(filterKey) { }
            val type = condition?.type ?: Type.STRING
            val filterKeyType = SimpleObjectProperty<Type>(type)
            filterKeyTypeProperties.add(filterKeyType)
            combobox(values = FILTER_KEY_TYPES, property = filterKeyType)
            val filterKeyOperation = SimpleObjectProperty<Operator>(condition?.operator ?: Operator.EQ)
            filterKeyOperatorProperties.add(filterKeyOperation)
            val filterKeyOperators = mutableListOf<Operator>().asObservable()
            filterKeyOperators.setAll(type.filterOperators)
            val filterKeyOperationComboBox = combobox(
                    values = filterKeyOperators, property = filterKeyOperation)
            filterKeyOperationComboBox.prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
            filterKeyType.addListener { _, _, newValue ->
                filterKeyOperators.setAll(newValue.filterOperators)
                filterKeyOperation.value = newValue.filterOperators.first()
            }
            val filterKeyValue = SimpleStringProperty("")
            val filterKeyValueTextField = TextField()
            filterKeyValueTextField.bind(filterKeyValue)
            val filterKeyValueComboBox = createAttributesComboBox(filterKeyValue)
            filterKeyValueProperties.add(filterKeyValue)
            val filterKeyFrom = SimpleStringProperty("")
            val filterKeyTo = SimpleStringProperty("")
            val andLabel = Label("And")
            andLabel.minWidth = 40.0
            andLabel.alignment = Pos.CENTER
            val filterKeyBetweenHBox = when (mode) {
                Mode.NORMAL -> {
                    val filterKeyFromTextField = TextField()
                    filterKeyFromTextField.textProperty().bindBidirectional(filterKeyFrom)
                    val filterKeyToTextField = TextField()
                    filterKeyToTextField.textProperty().bindBidirectional(filterKeyTo)
                    HBox(filterKeyFromTextField, andLabel, filterKeyToTextField)
                }
                Mode.FOREIGN -> {
                    val filterKeyFromComboBox = createAttributesComboBox(filterKeyFrom)
                    val filterKeyToComboBox = createAttributesComboBox(filterKeyTo)
                    HBox(filterKeyFromComboBox, andLabel, filterKeyToComboBox)
                }
            }
            filterKeyBetweenHBox.alignment = Pos.CENTER
            filterKeyBetweenValueProperties.add(Pair(filterKeyFrom, filterKeyTo))
            filterKeyOperationComboBox.valueProperty()
                    .addListener(this@SearchCriteriaFragment.OperatorChangeListener(
                            filterKeyOperationComboBox,
                            if (mode == Mode.NORMAL) {
                                filterKeyValueTextField
                            } else {
                                filterKeyValueComboBox
                            },
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
                if (mode == Mode.NORMAL) {
                    filterKeyValueTextField.attachTo(this)
                } else {
                    filterKeyValueComboBox.attachTo(this)
                }
            }
            button("x") {
                addClass("button-x")
                action {
                    val rowIndex = queryGridPane.removeRow(this)
                    val index = rowIndex - keysRowsCount
                    println("Remove index $index")
                    filterLabelProperties.removeAt(index)
                    filterKeyProperties.removeAt(index)
                    filterKeyTypeProperties.removeAt(index)
                    filterKeyOperatorProperties.removeAt(index)
                    filterKeyValueProperties.removeAt(index)
                    filterKeyBetweenValueProperties.removeAt(index)
                    if (index == 0 && filterLabelProperties.isNotEmpty()) {
                        filterLabelProperties[0].value = FILTER_PRIMARY_LABEL
                    }
                }
            }
        }
    }

    private fun createAttributesComboBox(property: SimpleStringProperty): ComboBox<String> {
        return ComboBox(attributes).apply {
            isEditable = true
            prefWidth = ATTRIBUTE_VALUE_COLUMN_WIDTH
            bind(property)
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

    fun getSearch(name: String = ""): Search {
        val searchSource = searchSourceProperty.value
        val tableName = description.tableName
        val indexName = if (searchSource.isIndex) {
            searchSource.name
        } else {
            null
        }
        return when (searchTypeProperty.value!!) {
            SearchType.SCAN -> {
                ScanSearch(tableName, indexName, buildSearchConditions(), name)
            }
            SearchType.QUERY -> {
                val hashKeyName = searchSource.hashKey.attributeName
                val sortKey = searchSource.sortKey
                val sortQueryCondition = if (sortKey == null) {
                    null
                } else {
                    val sortKeyName = sortKey.attributeName
                    val sortKeyOperator = sortKeyOperatorProperty.value
                    val sortKeyValues = when {
                        sortKeyOperator.isBetween() ->
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
                    Condition(
                            sortKeyName,
                            attributeDefinitionTypes.getValue(sortKeyName),
                            sortKeyOperator,
                            sortKeyValues)
                }
                QuerySearch(
                        tableName,
                        indexName,
                        Condition.hashKey(
                                hashKeyName,
                                attributeDefinitionTypes.getValue(hashKeyName),
                                parseValue(hashKeyValueProperty.value)),
                        sortQueryCondition,
                        buildSearchConditions(),
                        orderProperty.value,
                        name)
            }
        }
    }

    private fun buildSearchConditions(): List<Condition> {
        return filterKeyProperties.mapIndexed { index, filterKey ->
            val filterKeyOperation = filterKeyOperatorProperties[index].value
            Condition(
                    filterKey.value,
                    filterKeyTypeProperties[index].value,
                    filterKeyOperation,
                    if (filterKeyOperation.isBetween()) {
                        val betweenPair = filterKeyBetweenValueProperties[index]
                        listOf(parseValue(betweenPair.first.value),
                                parseValue(betweenPair.second.value))
                    } else {
                        listOf(parseValue(filterKeyValueProperties[index]!!.value))
                    })
        }
    }

    inner class OperatorChangeListener(private val operators: ComboBox<Operator>,
                                       private val field: Node,
                                       private val fieldValue: SimpleStringProperty,
                                       private val betweenHBox: HBox,
                                       private val textFieldFrom: SimpleStringProperty,
                                       private val textFieldTo: SimpleStringProperty) : ChangeListener<Operator> {
        override fun changed(observable: ObservableValue<out Operator>, oldValue: Operator, newValue: Operator) {
            when (newValue) {
                Operator.BETWEEN -> {
                    val (columnIndex, rowIndex) = if (oldValue.isNoArg()) {
                        getNoArgColumnRowIndexes()
                    } else {
                        Pair(GridPane.getColumnIndex(field), GridPane.getRowIndex(field))
                    }
                    queryGridPane.add(betweenHBox, columnIndex, rowIndex)
                    if (!oldValue.isNoArg()) {
                        queryGridPane.children.remove(field)
                        fieldValue.value = ""
                    }
                }
                Operator.EXISTS, Operator.NOT_EXISTS -> {
                    if (oldValue.isNoArg()) {
                        // The textfield then has been already removed
                        return
                    }
                    if (oldValue.isBetween()) {
                        queryGridPane.children.remove(betweenHBox)
                        textFieldFrom.value = ""
                        textFieldTo.value = ""
                    } else {
                        queryGridPane.children.remove(field)
                        fieldValue.value = ""
                    }
                }
                else -> {
                    if (oldValue.isBetween()) {
                        val columnIndex = GridPane.getColumnIndex(betweenHBox)
                        val rowIndex = GridPane.getRowIndex(betweenHBox)
                        queryGridPane.add(field, columnIndex, rowIndex)
                        queryGridPane.children.remove(betweenHBox)
                        textFieldFrom.value = ""
                        textFieldTo.value = ""
                    } else if (oldValue.isNoArg()) {
                        val (columnIndex, rowIndex) = getNoArgColumnRowIndexes()
                        queryGridPane.add(field, columnIndex, rowIndex)
                    }
                }
            }
        }

        private fun getNoArgColumnRowIndexes(): Pair<Int, Int> {
            val columnIndex = GridPane.getColumnIndex(operators) + 1
            val rowIndex = GridPane.getRowIndex(operators)
            return Pair(columnIndex, rowIndex)
        }
    }
}
