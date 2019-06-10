package com.akazlou.dynoman.view

data class DynamoDBTableTreeItemValue(val text: String, val type: Type) {
    enum class Type {
        TEXT,
        TABLE,
        INDEX
    }

    companion object {
        fun textValue(text: String): DynamoDBTableTreeItemValue {
            return DynamoDBTableTreeItemValue(text, Type.TEXT)
        }

        fun tableValue(name: String): DynamoDBTableTreeItemValue {
            return DynamoDBTableTreeItemValue(name, Type.TABLE)
        }

        fun indexValue(name: String): DynamoDBTableTreeItemValue {
            return DynamoDBTableTreeItemValue(name, Type.INDEX)
        }
    }
}

