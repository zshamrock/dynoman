package com.akazlou.dynoman.domain

open class DynamoDBTable(val tableName: String) {
    open val name: String
        get() {
            return tableName
        }
}

class DynamoDBTableIndex(tableName: String, val indexName: String) : DynamoDBTable(tableName) {
    override val name: String
        get() {
            return indexName
        }
}