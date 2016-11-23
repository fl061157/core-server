package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.service.ConfigurationService;
import cn.v5.service.SystemCmdService;
import cn.v5.validation.Validate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/api", produces = "application/json")
@Validate
public class SystemController {
    private static Logger logger = cn.v5.util.LoggerFactory.getLogger(SystemController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", 2000);
    }};

    @Inject
    private ConfigurationService configurationService;

    @RequestMapping(value = "/system/settings", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> settings(String key) {
        return configurationService.findById(key);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SystemCmdService systemCmdService;


    @RequestMapping(value = "/system/settings", method = RequestMethod.POST)
    @ResponseBody
    public void settings(String key, String values) {
        Map<String, String> attrs = new HashMap<>();
        String[] items = values.split(",");
        for (String item : items) {
            String[] namePair = item.split(":");
            String name = namePair[0];
            String value = namePair[1];
            attrs.put(name, value);
        }
        logger.info("key = {}, attrs = {}", key, attrs);
        configurationService.create(key, attrs);
    }

    @RequestMapping(value = "/system/cmd", method = RequestMethod.POST)
    @ResponseBody
    public Map systemCmd(HttpServletRequest request) {
        User user = CurrentUser.user();

        //兼容IOS问题
        //POST/api//system/cmd ==== thread id : http-bio-8080-exec-88|POSTHeader:{host = api.chatgame.me,x-real-ip = 10.0.30.161,remote-host = 10.0.30.161,x-forwarded-for = 1.46.3.240, 10.0.30.161,
        // connection = close,content-length = 56,accept = */*,accept-encoding = gzip, deflate,accept-language = th;q=1, en;q=0.9,api-version = 1.0,client-session = 3df1e16ec75b48c6bc6ec9f61bd62a0b,
        // client-version = chatgame-2.1.5,content-type = application/x-www-form-urlencoded; charset=utf-8,region-code = 0066,user-agent = CG2.1.5_iOS_8.40_Apple_iPhone6,2_iOS_th-TH_7.0,x-forwarded-port = 443,
        // x-forwarded-proto = https,}
        // ==== Parameter:{info = [{"source":"13"}],other_app_friend = [type],} ====

        //两处错误，1 是content-type 填写错误，2 是other_app_friend=type 参数名称和参数值填写反了

        //如果是application/x-www-form-urlencoded，容器已经把body解析成了各个参数
        String contentType = request.getHeader("content-type");
        if (StringUtils.isNotBlank(contentType) && contentType.toLowerCase().contains("x-www-form-urlencoded")) {
            String info = request.getParameter("info");
            boolean otherAppFriend = "type".equalsIgnoreCase(request.getParameter("other_app_friend"));
            if (StringUtils.isNotBlank(info) && otherAppFriend) {
                systemCmdService.handleSystemCmd(user, "other_app_friend", info);
            }
            logger.debug("[system cmd] info:{} . is otherAppFriend:{}", info, otherAppFriend);
        } else {
            try {
                String body = IOUtils.toString(request.getInputStream(), "utf-8");
                logger.debug("[system cmd] body:{}", body);
                JsonNode jsonNode = objectMapper.readTree(body);
                String type = jsonNode.get("type").textValue();
                String info = null;
                JsonNode content = jsonNode.get("info");
                if (null != content) {
                    info = content.toString();
                }
                systemCmdService.handleSystemCmd(user, type, info);
            } catch (IOException e) {
                logger.error("fails to get request body.", e);
                throw new ServerException(StatusCode.PARAMETER_ERROR, "fails to get request body");
            }
        }
        return SUCCESS_CODE;
    }
}
