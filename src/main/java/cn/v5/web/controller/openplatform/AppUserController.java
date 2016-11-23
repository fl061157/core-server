package cn.v5.web.controller.openplatform;

import cn.v5.bean.openplatform.UserVo;
import cn.v5.code.StatusCode;
import cn.v5.entity.User;
import cn.v5.service.OpManagerService;
import cn.v5.util.RequestUtils;
import cn.v5.util.UserUtils;
import cn.v5.validation.Validate;
import cn.v5.web.controller.ServerException;
import net.sf.oval.constraint.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Created by piguangtao on 15/7/8.
 */
@Controller
@RequestMapping(value = "/open/api", produces = "application/json")
@Validate
public class AppUserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppUserController.class);

    private static final Map SUCCESS = new HashMap<>();

    static {
        SUCCESS.put("error_code", 2000);
    }

    @Autowired
    private OpManagerService opManagerService;

    @Autowired
    private RequestUtils requestUtils;

//    @Autowired
//    private EventPublisher eventPublisher;

    //登陆1:如果app_user_id存在则更新session和nickname，否则创建新用户

    /**
     * @param app_user_id 调用方提供的userId
     */
    @RequestMapping(value = "/user/auth", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAuthInfo(HttpServletRequest request, @NotNull @NotEmpty String app_user_id, String app_user_nick_name, String avatar) {
        int appKey = requestUtils.getAppIdFromToken(request);
        Map<String, Object> result = new HashMap();

        User user = opManagerService.generateUser(request, appKey, app_user_id, app_user_nick_name, avatar);

        if (null == user) {
            throw new ServerException(StatusCode.INNER_ERROR, "fails to generate user");
        }

        UserVo userVo = new UserVo();
        userVo.withId(app_user_id).withSessionId(user.getSessionId());

        result.put("error_code", 2000);
        result.put("user", userVo);
        return result;
    }

    /**
     */
    @RequestMapping(value = "/session/auth", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAuthInfoByUserId(HttpServletRequest request, @NotNull @NotEmpty String app_user_id) {
        Map<String, Object> result = new HashMap();
        int appKey = requestUtils.getAppIdFromToken(request);
        String user_id = UserUtils.genInternalUserId(app_user_id, String.valueOf(appKey));
        User user = opManagerService.getNewSession(appKey, user_id);

        if (null == user) {
            throw new ServerException(StatusCode.INNER_ERROR, "fails to generate user");
        }

        UserVo userVo = new UserVo();
        userVo.withId(app_user_id).withSessionId(user.getSessionId());

        result.put("error_code", 2000);
        result.put("user", userVo);
        return result;
    }

    @RequestMapping(value = "/create_if_absent", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getIfPresent(HttpServletRequest request, @NotNull @NotEmpty String app_user_id, String app_user_nick_name, String avatar) {
        Map<String, Object> result = new HashMap();
        int appKey = requestUtils.getAppIdFromToken(request);
        User user = opManagerService.getIfPresent(request, appKey, app_user_id, app_user_nick_name, avatar);

        if (null == user) {
            throw new ServerException(StatusCode.INNER_ERROR, "fails to generate user");
        }

        UserVo userVo = new UserVo();
        userVo.withId(app_user_id).withSessionId(user.getSessionId());

        result.put("error_code", 2000);
        result.put("user", userVo);
        return result;
    }


    /**
     * @param
     */
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateUserInfo(HttpServletRequest request, String app_user_id, String app_user_nick_name, String avatar) {
        int appKey = requestUtils.getAppIdFromToken(request);

        User currentUser = opManagerService.getById(UserUtils.genInternalUserId(app_user_id, appKey));

        opManagerService.updateUserInfo(currentUser, app_user_nick_name, avatar);

        return SUCCESS;
    }

}
