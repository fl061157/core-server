package cn.v5.web.controller.openplatform;

import cn.v5.code.StatusCode;
import cn.v5.service.OpManagerService;
import cn.v5.util.RequestUtils;
import cn.v5.validation.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by haoWang on 2016/6/23.
 */
@Controller
@Validate
@RequestMapping(value = "/", produces = "application/json")
public class AppController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);
    private static final String PARAM_SPLIT = ",";
    @Autowired
    private RequestUtils requestUtils;

    @Autowired
    private OpManagerService managerService;

    @RequestMapping(value = "open/api/app_id", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Integer> getAppKey(HttpServletRequest request) {
        Integer appId = requestUtils.getAppIdFromToken(request);
        Map<String, Integer> ret = new HashMap<>();
        ret.put("app_id", appId);
        return ret;
    }

    @RequestMapping(value = "api/apps", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getApp(String ids) {
        Map<String, Object> ret = new HashMap<>();

        List<Integer> appIds = Stream.of(ids.split(PARAM_SPLIT)).map(Integer::parseInt).collect(Collectors.toList());
        ret.put("result", managerService.getApps(appIds));
        ret.put("error_code", StatusCode.SUCCESS);
        return ret;
    }
}
