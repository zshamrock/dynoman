package com.akazlou.dynoman.domain

open class DynamoDBTable(val name: String)

class DynamoDBTableIndex(tableName: String, val indexName: String) : DynamoDBTable(tableName)