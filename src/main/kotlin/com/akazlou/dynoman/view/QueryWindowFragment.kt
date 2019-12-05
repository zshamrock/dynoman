package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.search.QueryResult
import com.akazlou.dynoman.domain.search.QuerySearch
import com.akazlou.dynoman.domain.search.ScanSearch
import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.service.DynamoDBOperation
import com.amazonaws.services.dynamodbv2.model.TableDescription
import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import tornadofx.*

class QueryWindowFragment : Fragment("Query...") {
    enum class Mode {
        MODAL,
        INLINE
    }

    val mode: Mode by param()
    val operation: DynamoDBOperation by param()
    val description: TableDescription by param()
    private var tab: QueryTabFragment? = null
    private val searchCriteriaFragment: SearchCriteriaFragment = find(params)

    override val root = vbox(5.0) {
        prefWidth = 750.0
        if (mode == Mode.MODAL) {
            prefHeight = 230.0
        }
        scrollpane {
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbox(5.0) {
                add(searchCriteriaFragment.root)
                separator()
                hbox(5.0) {
                    alignment = Pos.CENTER
                    button("Search") {
                        action {
                            // TODO: Is it possible instead to open the tab, and show the loader/spinner there,
                            //  instead of closing this modal window and display nothing
                            val search = searchCriteriaFragment.getSearch()
                            if (search != null) {
                                runAsyncWithProgress {
                                    val result = if (search.type.isScan()) {
                                        operation.scan(search as ScanSearch)
                                    } else {
                                        operation.query(search as QuerySearch)
                                    }
                                    Pair(search, result)
                                } ui { (search, result) ->
                                    if (mode == Mode.MODAL) {
                                        find(QueryView::class).setQueryResult(
                                                operation,
                                                description,
                                                search,
                                                result)
                                    } else {
                                        tab?.setQueryResult(
                                                QueryResult(
                                                        search.type,
                                                        description,
                                                        result))
                                    }
                                }
                            }
                            if (mode == Mode.MODAL) {
                                close()
                            }
                        }
                    }
                    if (mode == Mode.MODAL) {
                        button("Cancel") {
                            action {
                                close()
                            }
                        }
                    }
                }
            }
        }
    }

    fun init(search: Search,
             tab: QueryTabFragment) {
        this.tab = tab
        searchCriteriaFragment.init(search)
    }

    fun getSearch(name: String = ""): Search {
        return searchCriteriaFragment.getSearch(name)
    }
}


