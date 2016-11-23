package cn.v5.json;

import cn.v5.code.SystemConstants;
import cn.v5.util.LoggerFactory;
import cn.v5.util.ReqMetricUtil;
import cn.v5.util.StringUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.JsonProcessingException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;


public class CustomJacksonMessageConverter extends MappingJackson2HttpMessageConverter {
    private static final Logger log = LoggerFactory.getLogger(CustomJacksonMessageConverter.class);
    @Autowired
    private ObjectMapper mapper;

    private ObjectMapper debugMapper = new ObjectMapper();

    @Autowired
    private ReqMetricUtil reqMetricUtil;

    @PostConstruct
    public void init() {
        super.setObjectMapper(mapper);
        initObjectMapper();
    }

    private void initObjectMapper() {
        debugMapper.configure(SerializationFeature.INDENT_OUTPUT, false);
//        this.objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        //this.objectMapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        this.objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

        this.objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {

        try {
            byte[] buffer = mapper.writeValueAsBytes(object);
            if (buffer != null) {
                outputMessage.getHeaders().setContentLength(buffer.length);
            }
            outputMessage.getHeaders().setCacheControl("no-cache");
            outputMessage.getHeaders().setExpires(-1);

            if (null != buffer) {
                outputMessage.getBody().write(buffer);
            }

            if (log.isInfoEnabled()) {
                List<String> traceId = outputMessage.getHeaders().get(SystemConstants.REQUEST_TRACE_ID);
                StringBuilder sbuilder = new StringBuilder();
                if (null != traceId && traceId.size() > 0) {
                    sbuilder.append(" traceId:").append(traceId.get(0)).append(" ");
                }
                sbuilder.append(debugMapper.writeValueAsString(object));
                log.info(StringUtil.hideMsgContent(sbuilder.toString()));
            }

            reqMetricUtil.end(System.currentTimeMillis(), 200);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }
}
