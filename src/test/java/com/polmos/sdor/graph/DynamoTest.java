package com.polmos.sdor.graph;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RobicToNieMaKomu on 2017-11-01.
 */
public class DynamoTest {

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("sqlite4java.library.path", "native-libs");
    }

    @Test
    public void createTableTest() {
        AmazonDynamoDB ddb = DynamoDBEmbedded.create().amazonDynamoDB();
        DynamoDB dynamoDB = new DynamoDB(ddb);
        try {
            String tableName = "Users";
            String hashKeyName = "github_name";
            createTable(ddb, tableName, hashKeyName);

            Item item = new Item()
                    .withPrimaryKey(hashKeyName, "a")
                    .withBinary("data", new Item()
                            .withStringSet("b", "c")
                            .toJSON()
                            .getBytes());
            System.out.println(item);

            Table table = dynamoDB.getTable(tableName);
            table.putItem(item);

            System.out.println(table.getItem(hashKeyName, "a"));


        } finally {
            ddb.shutdown();
        }
    }

    private static CreateTableResult createTable(AmazonDynamoDB ddb, String tableName, String hashKeyName) {
        List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        attributeDefinitions.add(new AttributeDefinition(hashKeyName, ScalarAttributeType.S));

        List<KeySchemaElement> ks = new ArrayList<>();
        ks.add(new KeySchemaElement(hashKeyName, KeyType.HASH));

        ProvisionedThroughput provisionedthroughput = new ProvisionedThroughput(1000L, 1000L);

        CreateTableRequest request =
                new CreateTableRequest()
                        .withTableName(tableName)
                        .withAttributeDefinitions(attributeDefinitions)
                        .withKeySchema(ks)
                        .withProvisionedThroughput(provisionedthroughput);

        return ddb.createTable(request);
    }

}
