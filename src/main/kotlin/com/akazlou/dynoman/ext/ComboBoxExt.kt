package com.akazlou.dynoman.ext

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin
import javafx.scene.AccessibleAttribute
import javafx.scene.control.ComboBox

fun <T> ComboBox<T>.applyAccessibilityNpeWorkaround(): ComboBox<T> {
    skin = CustomComboBoxListViewSkin(this)
    return this
}

private class CustomComboBoxListViewSkin<T>(comboBox: ComboBox<T>) : ComboBoxListViewSkin<T>(comboBox) {
    override fun queryAccessibleAttribute(attribute: AccessibleAttribute, vararg parameters: Any?): Any? {
        return when (attribute) {
            AccessibleAttribute.SELECTION_START -> if (skinnable.isEditable) {
                editor?.selection?.start
            } else {
                null
            }

            AccessibleAttribute.SELECTION_END -> if (skinnable.isEditable) {
                editor?.selection?.end
            } else {
                null
            }

            else -> super.queryAccessibleAttribute(attribute, *parameters)
        }
    }
}
