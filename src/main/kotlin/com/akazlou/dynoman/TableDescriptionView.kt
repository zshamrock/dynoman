package com.akazlou.dynoman

import com.amazonaws.services.dynamodbv2.model.TableDescription
import tornadofx.Fragment
import tornadofx.text
import tornadofx.vbox

class TableDescriptionView : Fragment("Table Description") {
    val description: TableDescription by param()
    override val root = vbox {
        text("Name: ${description.tableName}")
        val provisionedThroughput = description.provisionedThroughput
        text("RCU: ${provisionedThroughput.readCapacityUnits}, WCU: ${provisionedThroughput.writeCapacityUnits}")
    }
}
