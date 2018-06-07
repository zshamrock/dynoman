package com.akazlou.dynoman.view

import com.amazonaws.services.dynamodbv2.model.TableDescription
import tornadofx.*

class QueryWindowFragment : Fragment("Query...") {
    val description: TableDescription by param()
    private val tableAttributesString: String

    init {
        tableAttributesString = description.keySchema.joinToString { it.attributeName }
    }

    override val root = vbox {
        gridpane {
            row {
                label("Query")
                combobox(values = listOf("[Table] ${description.tableName}: $tableAttributesString")) {
                    selectionModel.select(0)
                }
            }
        }
        hbox {
            button("Query")
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }
}
