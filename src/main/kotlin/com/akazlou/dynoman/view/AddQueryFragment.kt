package com.akazlou.dynoman.view

import com.akazlou.dynoman.controller.MainController
import com.akazlou.dynoman.domain.SearchSource
import javafx.scene.control.SelectionMode
import tornadofx.*

class AddQueryFragment : Fragment("Add Query") {
    private val controller: MainController by inject()

    override val root = form {
        fieldset("New Query") {
            field("Target:") {
                // TODO: Probably change to the tree view instead
                listview<SearchSource> {
                    val properties = Config.getConnectionProperties(app.config)
                    val tables = controller.listTables(properties)
                    val descriptions = tables.map { controller.describeTable(it, properties) }
                    descriptions.map { description ->
                        val gsi = description.globalSecondaryIndexes.orEmpty()
                        val indexesSearchSources = gsi.map { index ->
                            SearchSource(index.indexName, index.keySchema, true)
                        }.sorted()
                        val tableSearchSource = SearchSource(description.tableName, description.keySchema, false)
                        items.add(tableSearchSource)
                        indexesSearchSources.forEach { items.add(it) }
                    }
                    selectionModel.selectionMode = SelectionMode.MULTIPLE
                }
            }
        }
    }
}
