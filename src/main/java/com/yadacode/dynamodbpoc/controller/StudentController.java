package com.yadacode.dynamodbpoc.controller;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.yadacode.dynamodbpoc.model.student.StudentDTO;
import com.yadacode.dynamodbpoc.service.StudentService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
public class StudentController {
	
    static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    static DynamoDB dynamoDB = new DynamoDB(client);

    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static String productCatalogTableName = "ProductCatalogs2";
    static String forumTableName = "Forum";
    static String threadTableName = "Thread";
    static String replyTableName = "Reply";
	
	
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/")
    public StudentDTO createNewStudent(@RequestBody StudentDTO dto){
        return studentService.createNewStudent(dto);
 
    }
    
    @GetMapping("/gi")
    public Item getItem(){
    	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    	DynamoDB dynamoDB = new DynamoDB(client);

    	Table table = dynamoDB.getTable("question");

    	Item item = table.getItem("Id", 210);
    	
    	return item;
 
    }
    
    @GetMapping("/a")
    public Item getItemA(){
    	AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    	DynamoDB dynamoDB = new DynamoDB(client);

    	Table table = dynamoDB.getTable("question");

    	Item item = table.getItem("question_id", 210);
    	
    	return item;
 
    }
    @GetMapping("/ct")
    public String createTable(){
    createTable(productCatalogTableName, 10L, 5L, "Id", "N");
    return "success";
    }
    
    private static void createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
            String partitionKeyName, String partitionKeyType) {

            createTable(tableName, readCapacityUnits, writeCapacityUnits, partitionKeyName, partitionKeyType, null, null);
        }
    private static void createTable(String tableName, long readCapacityUnits, long writeCapacityUnits,
            String partitionKeyName, String partitionKeyType, String sortKeyName, String sortKeyType) {

            try {

                ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
                keySchema.add(new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH)); // Partition
                                                                                                                     // key

                ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
                attributeDefinitions
                    .add(new AttributeDefinition().withAttributeName(partitionKeyName).withAttributeType(partitionKeyType));

                if (sortKeyName != null) {
                    keySchema.add(new KeySchemaElement().withAttributeName(sortKeyName).withKeyType(KeyType.RANGE)); // Sort
                                                                                                                     // key
                    attributeDefinitions
                        .add(new AttributeDefinition().withAttributeName(sortKeyName).withAttributeType(sortKeyType));
                }

                CreateTableRequest request = new CreateTableRequest().withTableName(tableName).withKeySchema(keySchema)
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(readCapacityUnits)
                        .withWriteCapacityUnits(writeCapacityUnits));

                // If this is the Reply table, define a local secondary index
                if (replyTableName.equals(tableName)) {

                    attributeDefinitions
                        .add(new AttributeDefinition().withAttributeName("PostedBy").withAttributeType("S"));

                    ArrayList<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<LocalSecondaryIndex>();
                    localSecondaryIndexes.add(new LocalSecondaryIndex().withIndexName("PostedBy-Index")
                        .withKeySchema(new KeySchemaElement().withAttributeName(partitionKeyName).withKeyType(KeyType.HASH), // Partition
                                                                                                                             // key
                            new KeySchemaElement().withAttributeName("PostedBy").withKeyType(KeyType.RANGE)) // Sort
                                                                                                             // key
                        .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY)));

                    request.setLocalSecondaryIndexes(localSecondaryIndexes);
                }

                request.setAttributeDefinitions(attributeDefinitions);

                System.out.println("Issuing CreateTable request for " + tableName);
                Table table = dynamoDB.createTable(request);
                System.out.println("Waiting for " + tableName + " to be created...this may take a while...");
                table.waitForActive();

            }
            catch (Exception e) {
                System.err.println("CreateTable request failed for " + tableName);
                System.err.println(e.getMessage());
            }
        }
}
 
