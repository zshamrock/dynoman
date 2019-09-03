package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.search.Search
import com.akazlou.dynoman.domain.search.SearchInput
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import tornadofx.*

class UserQueryInputFragment : Fragment("Specify values for search") {
    val inputs: List<SearchInput> by param()
    private val values: List<SimpleStringProperty>
    private var cancel = true

    companion object {
        private const val ATTRIBUTE_TYPE_COLUMN_WIDTH = 90.0
        private const val ATTRIBUTE_OPERATION_COLUMN_WIDTH = 90.0
        private const val ATTRIBUTE_VALUE_COLUMN_WIDTH = 180.0
        private const val AND_LABEL_MIN_WIDTH = 40.0
    }

    init {
        values = filterRefs(inputs).map { SimpleStringProperty("") }
    }

    override val root = form {
        prefWidth = 740.0
        prefHeight = 60 + inputs.size * 40.0
        fieldset {
            var index = 0
            inputs.forEach { input ->
                field(input.name) {
                    label(input.type.toString()) {
                        prefWidth = ATTRIBUTE_TYPE_COLUMN_WIDTH
                    }
                    label(input.operator.toString()) {
                        prefWidth = ATTRIBUTE_OPERATION_COLUMN_WIDTH
                    }
                    if (input.operator.isBetween()) {
                        val refs = input.refs
                        refs.forEachIndexed { i, ref ->
                            if (i == 1) {
                                label("And") {
                                    minWidth = AND_LABEL_MIN_WIDTH
                                    alignment = Pos.CENTER
                                }
                            }
                            if (Search.requiresUserInput(ref)) {
                                textfield(values[index++]) {
                                    prefWidth = ATTRIBUTE_VALUE_COLUMN_WIDTH
                                }
                            } else {
                                textfield(ref) {
                                    isDisable = true
                                    prefWidth = ATTRIBUTE_VALUE_COLUMN_WIDTH
                                }
                            }
                        }
                    } else {
                        textfield(values[index++])
                    }
                }
            }
        }
        buttonbar {
            button("Apply") {
                action {
                    cancel = false
                    close()
                }
            }
            button("Cancel") {
                action {
                    close()
                }
            }
        }
    }

    fun getMappings(): Map<String, String> {
        return filterRefs(inputs)
                .zip(values)
                .associate { pair -> pair.first to pair.second.value }
    }

    fun isCancel(): Boolean {
        return cancel
    }

    private fun filterRefs(inputs: List<SearchInput>) = inputs
            .flatMap { it.refs.filter { ref -> Search.requiresUserInput(ref) } }
}
