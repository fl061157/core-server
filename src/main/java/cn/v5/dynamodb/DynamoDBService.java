package cn.v5.dynamodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * 
 * @author luyanliang
 *
 */
@Service
public class DynamoDBService implements InitializingBean{

	private static Logger LOGGER = LoggerFactory.getLogger(DynamoDBService.class);

	private AmazonDynamoDB dynamoDB;

	private DynamoDBMapper mapper;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			AWSCredentials credentials = new PropertiesCredentials(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream("AwsDynamodbCredentials.properties"));
			dynamoDB = new AmazonDynamoDBClient(credentials);
			dynamoDB.setRegion(Region.getRegion(Regions.CN_NORTH_1));
			mapper = new DynamoDBMapper(dynamoDB);
		} catch (Exception e) {
			LOGGER.error("Failed to create AmazonDynamoDB. {}", e.getMessage());
		}
	}

	public AmazonDynamoDB getAmazonDynamoDB() {
		return dynamoDB;
	}

	public DynamoDBMapper getDynamoDBMapper() {
		return mapper;
	}
}
