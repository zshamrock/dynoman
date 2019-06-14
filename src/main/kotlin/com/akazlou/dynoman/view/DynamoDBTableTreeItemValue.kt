package com.akazlou.dynoman.view

sealed class DynamoDBTableTreeItemValue(val text: String) {

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

class DynamoDBTableTreeTableItem(val tableName: String) : DynamoDBTableTreeItemValue(tableName)

class DynamoDBTableTreeIndexItem(val tableName: String, val indexName: String) : DynamoDBTableTreeItemValue(tableName)

