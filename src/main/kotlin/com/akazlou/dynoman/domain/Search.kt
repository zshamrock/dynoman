package com.akazlou.dynoman.domain

abstract class Search(private var operationType: OperationType,
                      table: String,
                      index: String?,
                      filters: List<QueryCondition>,
                      asc: Boolean)

class QuerySearch(table: String,
                  index: String?,
                  keys: List<QueryCondition>,
                  filters: List<QueryCondition>,
                  asc: Boolean) : Search(OperationType.QUERY, table, index, filters, asc)

class ScanSearch(table: String,
                 index: String?,
                 filters: List<QueryCondition>,
                 asc: Boolean) :
        Search(OperationType.SCAN, table, index, filters, asc)