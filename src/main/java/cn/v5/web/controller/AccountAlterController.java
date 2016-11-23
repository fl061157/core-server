package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.User;
import cn.v5.service.UserService;
import net.sf.oval.constraint.NotEmpty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by piguangtao on 15/5/14.
 * 账号切割使用，数据完成首，可以删除该类
 */
@Controller
@RequestMapping(value = "/api", produces = "application/json")
public class AccountAlterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAlterController.class);
    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};
    @Autowired
    private UserService userService;


    private static AtomicLong index = new AtomicLong(1);

//    @RequestMapping(value = "/user/account/alert", method = RequestMethod.POST)
//    @ResponseBody
//    public Map<String, Integer> updateUser(@NotEmpty @NotNull String userId) {
//
//        String[] userIds = userId.split(",");
//        for (String singleUserId : userIds) {
//            //获取用户信息
//            User user = userService.findById(singleUserId);
//            if (null == user) {
//                throw new ServerException(StatusCode.PARAMETER_ERROR, "user does not exit.");
//            }
//
//            String oldAccount = user.getAccount();
//
//            AccountIndex accountIndex;
//            try {
//                accountIndex = userService.createAccount(user.getId(), user.getCountrycode());
//            } catch (AchillesLightWeightTransactionException e) {
//                throw new ServerException(StatusCode.ACCOUNT_EXIST, "account already exist.");
//            } catch (Exception e1) {
//                throw new ServerException(StatusCode.ACCOUNT_MODIFIED_ERROR, "fails to modifiy user account");
//            }
//
//            try {
//                user.setAccount(accountIndex.getAccount());
//                //屏蔽免打扰
//                user.setHideTime("");
//                userService.modifyUser(user);
//                eventPublisher.send(EventPath.USER_CHANGE, user);
//            } catch (Exception e1) {
//                throw new ServerException(StatusCode.ACCOUNT_MODIFIED_ERROR, "fails to modifiy user account");
//            }
//
//            try {
//                if (StringUtils.isNotBlank(oldAccount) && !oldAccount.equals(accountIndex.getAccount())) {
//                    userService.removeAccountIndex(oldAccount);
//                }
//                userService.createAccountHistory(user.getId(), accountIndex.getAccount());
//            } catch (Exception e1) {
//                LOGGER.error("fails to operate db.", e1);
//                //忽略异常，用户信息已经更新
//            }
//
//        }
//        return SUCCESS_CODE;
//    }

    @RequestMapping(value = "/user/nickname/change/default", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> updateUserNicknameByDefaultName(@NotEmpty @NotNull String userId) {

        String[] userIds = userId.split(",");
        for (String singleUserId : userIds) {
            //获取用户信息
            User user = userService.findById(0, singleUserId);
            if (null == user) {
                throw new ServerException(StatusCode.PARAMETER_ERROR, "user does not exit.");
            }

            String nickName = user.getNickname();

            if (!needToChange(nickName)) {
                LOGGER.debug("userId:{},nickName:{} is ok.", singleUserId, nickName);
                continue;
            }

            user.setNickname(getDefaultNickName(user.getCountrycode()));

            try {
                userService.modifyUser(user);
                LOGGER.debug("success to modify user nickName. user:{}", user);
                userService.userChange(user);
            } catch (Exception e1) {
                throw new ServerException(StatusCode.ACCOUNT_MODIFIED_ERROR, "fails to modifiy user account");
            }

        }
        return SUCCESS_CODE;
    }


    protected boolean needToChange(String nickName) {
        boolean result = true;
        if (StringUtils.isNotBlank(nickName)) {
            if (!nickName.startsWith("U_") || nickName.length() != 8) {
                result = false;
            }
        }
        return result;
    }

    protected String getDefaultNickName(String countryCode) {
        String nickName;
        if ("0086".equals(countryCode) || "86".equals(countryCode) || "+86".equals(countryCode)) {
            nickName = String.format("%s%d", "@无名氏_", index.getAndIncrement());
        } else {
            nickName = String.format("%s%d", "@Anonymous_", index.getAndIncrement());
        }
        return nickName;
    }

}
