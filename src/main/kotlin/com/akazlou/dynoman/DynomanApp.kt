package com.akazlou.dynoman

import com.amazonaws.regions.Regions
import tornadofx.App

class DynomanApp: App(MainView::class) {
    private val operation: DynamoDBOperation = DynamoDBOperation("https://localhost:9000", Regions.US_EAST_2.name)
}