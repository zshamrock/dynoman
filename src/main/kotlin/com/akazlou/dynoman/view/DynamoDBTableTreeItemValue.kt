package com.akazlou.dynoman.view

sealed class DynamoDBTableTreeItemValue(private val text: String) {
    open fun isTable(): Boolean {
        return false
    }

    open fun isIndex(): Boolean {
        return false
    }

    open fun getText(): String {
        return text
    }

    companion object {
        fun textValue(text: String): DynamoDBTableTreeItemValue {
            return DynamoDBTableTreeTextItem(text)
        }

        fun tableValue(tableName: String): DynamoDBTableTreeItemValue {
            return DynamoDBTableTreeTableItem(tableName)
        }

        fun indexValue(tableName: String, indexName: String): DynamoDBTableTreeItemValue {
            return DynamoDBTableTreeIndexItem(tableName, indexName)
        }
    }
}

class DynamoDBTableTreeTextItem(text: String) : DynamoDBTableTreeItemValue(text)

class DynamoDBTableTreeTableItem(val tableName: String) : DynamoDBTableTreeItemValue(tableName) {
    override fun isTable(): Boolean {
        return true
    }

    override fun getText(): String {
        return tableName
    }
}

class DynamoDBTableTreeIndexItem(val tableName: String, val indexName: String) : DynamoDBTableTreeItemValue(tableName) {
    override fun isIndex(): Boolean {
        return true
    }

    override fun getText(): String {
        return indexName
    }
}

