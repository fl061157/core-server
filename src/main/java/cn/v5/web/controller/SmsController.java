package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.CurrentUser;
import cn.v5.entity.User;
import cn.v5.service.SmsService;
import cn.v5.service.UserCallSmSendService;
import cn.v5.service.UserService;
import cn.v5.util.LoggerFactory;
import cn.v5.util.RequestUtils;
import cn.v5.validation.Validate;
import net.sf.oval.constraint.NotEmpty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 15/9/2.
 */
@Controller
@RequestMapping(value = "/api", produces = "application/json")
@Validate
public class SmsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmsController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", 2000);
    }};

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserService userService;


    @Autowired
    private UserCallSmSendService callSmSendService;


    @RequestMapping(value = "sms/send", method = RequestMethod.POST)
    @ResponseBody
    public Map sendSm(HttpServletRequest request, @NotNull @NotEmpty String mobile, @NotEmpty @NotNull String countrycode, String msg, @NotNull @NotEmpty String type, String from_id) {
        User currentUser = CurrentUser.user();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[send sm]user:{},mobile:{},countrycode:{},msg:{},type:{},fromId:{}", currentUser, mobile, countrycode, msg, type, from_id);
        }

        Integer appId = RequestUtils.getAppId(request);
        //根据手机号码查找对方
        //A 呼叫B时，B在voip模式下，且B没有push权限，需要给B发送一条短信
        User toUser = userService.findUserByMobile(mobile, countrycode, appId);

        User fromUser = null;
        if (StringUtils.isNotBlank(from_id)) {
            fromUser = userService.findById(from_id);
        }

        switch (type.toLowerCase()) {
            case "0":
                //直接发送短信
                if (StringUtils.isBlank(msg)) {
                    throw new ServerException(StatusCode.PARAMETER_ERROR, "msg should not be emputy in case of type 0");
                }

                smsService.sendSmsExcludeAuth(mobile, countrycode, msg);

                break;
            case "ios_no_push_right": {
                if (StringUtils.isBlank(from_id)) {
                    LOGGER.warn("[send sm] type:ios_no_push_right from_id should not be emputy.");
                    throw new ServerException(StatusCode.PARAMETER_ERROR, "type:ios_no_push_right from_id should not be emputy.");
                }
                userService.sendUserCallSm(toUser, fromUser, "call", null);
                break;
            }

            case "ios_call_unaccept": {
                if (StringUtils.isBlank(from_id)) {
                    LOGGER.warn("[send sm] type:ios_no_push_right from_id should not be emputy.");
                    throw new ServerException(StatusCode.PARAMETER_ERROR, "type:ios_no_push_right from_id should not be emputy.");
                }
                userService.sendUserCallSm(toUser, fromUser, "missed", Long.valueOf(2 * 60));
                break;
            }
            default:
                LOGGER.warn("[send sm]type:{} not supported", type);
                break;
        }


        return SUCCESS_CODE;
    }
}
