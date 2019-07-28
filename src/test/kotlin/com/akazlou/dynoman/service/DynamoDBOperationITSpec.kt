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

    private val operation = DynamoDBOperation(ConnectionProperties(Regions.US_WEST_2, "", "", "default", "", true), false)

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
                    AttributeDefinition("Id2", "S"),
                    AttributeDefinition("Num", "N"))
            description.keySchema shouldBe listOf(
                    KeySchemaElement("Id1", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE))
            val indexes = description.globalSecondaryIndexes
            indexes.size shouldBe 2

            val index1 = indexes[0]
            index1.indexName shouldBe "Table1Index2"
            index1.keySchema shouldBe listOf(
                    KeySchemaElement("Num", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE))
            index1.projection shouldBe Projection().withProjectionType(ProjectionType.ALL)
            index1.provisionedThroughput.readCapacityUnits shouldBe 10
            index1.provisionedThroughput.writeCapacityUnits shouldBe 5

            val index2 = indexes[1]
            index2.indexName shouldBe "Table1Index1"
            index2.keySchema shouldBe listOf(
                    KeySchemaElement("Id2", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE))
            index2.projection shouldBe Projection().withProjectionType(ProjectionType.ALL)
            index2.provisionedThroughput.readCapacityUnits shouldBe 10
            index2.provisionedThroughput.writeCapacityUnits shouldBe 5
        }
    }
}