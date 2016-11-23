package cn.v5.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by piguangtao on 15/12/3.
 */
public class CoreServerReqUtilTest {
    private ObjectMapper objectMapper = null;

    @Before
    public void before() {
        this.objectMapper = new ObjectMapper();
//        this.objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        this.objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

        this.objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }

    @Test
    public void testString() throws IOException {
        String urlMap = "[{\"country_code\": \"-1\",\"core_server\": [\"http://10.100.2.121\",\"http://10.100.2.139\"]}]";
        List<CoreServerReqUtil.CoreServerMap> result = objectMapper.readValue(urlMap, new TypeReference<List<CoreServerReqUtil.CoreServerMap>>() {
        });

        System.out.println(result.toArray().toString());

    }
}