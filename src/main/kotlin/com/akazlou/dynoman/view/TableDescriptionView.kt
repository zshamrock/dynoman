package com.akazlou.dynoman.view

import com.amazonaws.services.dynamodbv2.model.TableDescription
import tornadofx.*

class  TableDescriptionView : Fragment("Table Description") {
    val description: TableDescription by param()
    override val root = vbox(5.0) {
        text("Name: ${description.tableName}")
        val provisionedThroughput = description.provisionedThroughput
        text("RCU: ${provisionedThroughput.readCapacityUnits}, WCU: ${provisionedThroughput.writeCapacityUnits}")
    }
}
