package com.akazlou.dynoman.service

import com.akazlou.dynoman.DynamoDBTestContainerListener
import com.akazlou.dynoman.IT
import com.akazlou.dynoman.domain.ConnectionProperties
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.Projection
import com.amazonaws.services.dynamodbv2.model.ProjectionType
import io.kotlintest.TestCaseConfig
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class DynamoDBOperationITSpec : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(DynamoDBTestContainerListener)

    private val operation = DynamoDBOperation(ConnectionProperties(Regions.US_WEST_2, "", "", "default", true), false)

    override val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig(tags = setOf(IT))

    init {
        "connect to local dynamodb using profile and list tables" {
            val tables = operation.listTables()
            tables shouldBe listOf("Table1")
        }

        "connect to local dynamodb using profile and describe table" {
            val table = operation.getTable("Table1")
            val description = table.describe()
            table.tableName shouldBe "Table1"
            description.provisionedThroughput.readCapacityUnits shouldBe 10
            description.provisionedThroughput.writeCapacityUnits shouldBe 5
            description.attributeDefinitions.toSet() shouldBe setOf(
                    AttributeDefinition("Id1", "S"),
                    AttributeDefinition("Timestamp1", "N"),
                    AttributeDefinition("Id2", "S"))
            description.keySchema shouldBe listOf(
                    KeySchemaElement("Id1", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE))
            val indexes = description.globalSecondaryIndexes
            indexes.size shouldBe 1
            val index = indexes.first()
            index.indexName shouldBe "Table1Index"
            index.keySchema shouldBe listOf(
                    KeySchemaElement("Id2", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE))
            index.projection shouldBe Projection().withProjectionType(ProjectionType.ALL)
            index.provisionedThroughput.readCapacityUnits shouldBe 10
            index.provisionedThroughput.writeCapacityUnits shouldBe 5
        }
    }
}