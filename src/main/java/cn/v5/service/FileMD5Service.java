package cn.v5.service;

import cn.v5.dynamodb.DynamoDBService;
import cn.v5.entity.FileMD5;
import cn.v5.util.LoggerFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileMD5Service {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileMD5Service.class);

	@Autowired
	private DynamoDBService dynamoDBService;

	public void saveFileMD5(FileMD5 fileMD5) {
		try {
			Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
			map.put("md5", new AttributeValue().withS(fileMD5.getMd5()));
			map.put("url", new AttributeValue().withS(fileMD5.getUrl()));
			map.put("timestamp", new AttributeValue().withN(Long.toString(System.currentTimeMillis())));
			
			PutItemRequest itemRequest = new PutItemRequest().withTableName("FileMD5").withItem(map);
			dynamoDBService.getAmazonDynamoDB().putItem(itemRequest);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to save file md5. {}", e.getMessage());
		}
	}

	public String getUrl(String md5) {
		try {
			FileMD5 fileMD5 = new FileMD5();
			fileMD5.setMd5(md5);
			DynamoDBQueryExpression<FileMD5> expression = new DynamoDBQueryExpression<FileMD5>().withHashKeyValues(fileMD5);

			List<FileMD5> list = dynamoDBService.getDynamoDBMapper().query(FileMD5.class, expression);
			if (list.size() > 0) {
				return list.get(0).getUrl();
			} else {
				return null;
			}
		} catch (RuntimeException e) {
			LOGGER.error("Failed to get url by md5. {}", e.getMessage());
			return null;
		}
	}
	
	public static void main(String[] args) throws IOException {
		AWSCredentials credentials = new PropertiesCredentials(Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("AwsDynamodbCredentials.properties"));
		AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(credentials);
		dynamoDB.setRegion(Region.getRegion(Regions.CN_NORTH_1));
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
		FileMD5 fileMD5 = new FileMD5();
		fileMD5.setMd5("12345678765DC0AC916ED2A812345678");
		
		
		Map<String, AttributeValue> map = new HashMap<String, AttributeValue>();
		map.put("md5", new AttributeValue().withS("12345678765DC0AC916ED2A812345678"));
		map.put("url", new AttributeValue().withS("http://test"));
		map.put("timestamp", new AttributeValue().withN(Long.toString(System.currentTimeMillis())));
		PutItemRequest itemRequest = new PutItemRequest().withTableName("FileMD5").withItem(map);
		dynamoDB.putItem(itemRequest);
		
		DynamoDBQueryExpression<FileMD5> expression = new DynamoDBQueryExpression<FileMD5>().withHashKeyValues(fileMD5);
		List<FileMD5> list = mapper.query(FileMD5.class, expression);
		System.out.println(list.get(0).getUrl());
	}
}
