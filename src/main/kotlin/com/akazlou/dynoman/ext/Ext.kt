package com.akazlou.dynoman.ext

import javafx.scene.Node
import javafx.scene.layout.GridPane

// This is taken from TornadoFX internal, which requires to decrement this property correctly, in order further
// GridPane.row and the corresponding deletes to work properly. Other words it will allow to keep
// GridPane.getRowIndex(node) consistent with the view.
private const val GridPaneRowIdKey = "TornadoFX.GridPaneRowId"

/**
 * Removes the corresponding row to which this {@code node} belongs to. Returns the row index of the removed row.
 *
 * <p>It also adjusts {@code "TornadoFX.GridPaneRowId"} which is used by the {@link GridPane#row} internally.
 */
fun GridPane.removeRow(node: Node): Int {
    if (properties.containsKey(GridPaneRowIdKey)) {
        properties[GridPaneRowIdKey] = properties[GridPaneRowIdKey] as Int - 1
    }
    val rowIndex = GridPane.getRowIndex(node) ?: 0
    val nodesToDelete = mutableListOf<Node>()
    children.forEach { child ->
        val childRowIndex = GridPane.getRowIndex(child) ?: 0
        if (childRowIndex == rowIndex) {
            nodesToDelete.add(child)
        } else if (childRowIndex > rowIndex) {
            GridPane.setRowIndex(child, childRowIndex - 1)
        }
    }
    children.removeAll(nodesToDelete)
    return rowIndex
}