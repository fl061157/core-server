package cn.v5.json;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import org.springframework.beans.factory.FactoryBean;

public class FastxmlObjectMapperFactoryBean implements FactoryBean<ObjectMapper> {
    private ObjectMapper objectMapper = null;

    public FastxmlObjectMapperFactoryBean() {
            this.objectMapper = new ObjectMapper();
            this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.objectMapper.setPropertyNamingStrategy(
                    PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

            //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);

            this.objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }
    @Override
    public ObjectMapper getObject() throws Exception {
        return objectMapper;
    }

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
