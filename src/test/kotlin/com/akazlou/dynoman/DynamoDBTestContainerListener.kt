package com.akazlou.dynoman

import com.akazlou.dynoman.domain.ConnectionProperties
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
import com.amazonaws.services.dynamodbv2.model.CreateGlobalSecondaryIndexAction
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
import com.amazonaws.services.dynamodbv2.model.KeyType
import com.amazonaws.services.dynamodbv2.model.Projection
import com.amazonaws.services.dynamodbv2.model.ProjectionType
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import org.testcontainers.containers.GenericContainer
import java.time.Duration

object DynamoDBTestContainerListener : TestListener {
    private const val DYNAMODB_HOST_PORT = 8000
    private const val DYNAMODB_CONTAINER_PORT = 8000
    private const val DYNAMODB_IMAGE = "amazon/dynamodb-local:1.11.119"

    private val container: KGenericContainer

    init {
        container = KGenericContainer(DYNAMODB_IMAGE).withStartupTimeout(Duration.ofSeconds(5))
        container.portBindings = listOf("$DYNAMODB_HOST_PORT:$DYNAMODB_CONTAINER_PORT")
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        container.start()
        val dynamoDB = ConnectionProperties(Regions.US_WEST_2, "", "", "default", "", true).buildDynamoDBClient()
        setupData(dynamoDB)
    }

    private fun setupData(dynamoDB: DynamoDB) {
        val table = dynamoDB.createTable(
                "Table1",
                listOf(KeySchemaElement("Id1", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE)),
                listOf(
                        AttributeDefinition("Id1", "S"),
                        AttributeDefinition("Timestamp1", "N")),
                ProvisionedThroughput(10, 5))
        val index1 = table.createGSI(CreateGlobalSecondaryIndexAction()
                .withIndexName("Table1Index")
                .withKeySchema(
                        listOf(KeySchemaElement("Id2", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE)))
                .withProjection(Projection().withProjectionType(ProjectionType.ALL))
                .withProvisionedThroughput(ProvisionedThroughput(10, 5)),
                AttributeDefinition("Id2", "S"),
                AttributeDefinition("Timestamp1", "N"))
        index1.waitForActive()
        val index2 = table.createGSI(CreateGlobalSecondaryIndexAction()
                .withIndexName("Table2Index")
                .withKeySchema(
                        listOf(KeySchemaElement("Num", KeyType.HASH), KeySchemaElement("Timestamp1", KeyType.RANGE)))
                .withProjection(Projection().withProjectionType(ProjectionType.ALL))
                .withProvisionedThroughput(ProvisionedThroughput(10, 5)),
                AttributeDefinition("Num", "N"),
                AttributeDefinition("Timestamp1", "N"))
        index2.waitForActive()

        var num = 1
        for (i in 1..200) {
            if (i >= 150 && num == 1) {
                num = 2
            }
            table.putItem(Item()
                    .withPrimaryKey("Id1", "Id1-$i", "Timestamp1", i)
                    .withString("Id2", "Id2-$i")
                    .withNumber("Timestamp2", i)
                    .withNumber("Num", num))
        }
    }

    override fun afterSpec(description: Description, spec: Spec) {
        container.stop()
    }
}

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)