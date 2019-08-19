package com.akazlou.dynoman.view

import com.akazlou.dynoman.domain.search.SearchInput
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class UserQueryInputFragment : Fragment("User Input") {
    val inputs: List<SearchInput> by param()
    private val values: List<SimpleStringProperty>
    private var cancel = true

    init {
        values = inputs.map { SimpleStringProperty("") }
    }

    override val root = form {
        prefWidth = 390.0
        prefHeight = 80 + inputs.size * 40.0
        fieldset("Search Values") {
            inputs.forEachIndexed { index, input ->
                field(input.name) {
                    label(input.type.toString()) {
                        prefWidth = 60.0
                    }
                    textfield(values[index])
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
        return inputs.zip(values).associate { pair -> pair.first.ref to pair.second.value }
    }

    fun isCancel(): Boolean {
        return cancel
    }
}
