package cn.v5.web.controller.openplatform;

import cn.v5.bean.oauth2.TokenCasStore;
import cn.v5.code.StatusCode;
import cn.v5.code.SystemConstants;
import cn.v5.openplatform.entity.AppKeyInfo;
import cn.v5.service.OpManagerService;
import cn.v5.validation.Validate;
import cn.v5.web.controller.ServerException;
import com.google.common.base.Strings;
import net.sf.oval.constraint.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by haoWang on 2016/7/16.
 */
@Controller
@Validate
@RequestMapping(value = "/open/admin", produces = "application/json")
public class AppAdminController {
    private final Logger logger = LoggerFactory.getLogger(AppAdminController.class);

    @Autowired
    @Qualifier("clientDetailsService")
    private JdbcClientDetailsService clientDetailsService;

    @Autowired
    @Qualifier("tokenStore")
    private TokenCasStore tokenStore;

    @Autowired
    private OpManagerService opManagerService;

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    /**
     * add or update
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> add(@NotEmpty Integer clientId, String secret,
                                    String name, String desc, String logo, String webUrl) {
        String clientIdStr = clientId.toString();
        BaseClientDetails clientDetails = buildDefault(clientIdStr);
        clientDetails.setClientSecret(secret);
        clientDetailsService.addClientDetails(clientDetails);
        opManagerService.updateAppKeySecret(clientId, secret, AppKeyInfo.STATUS_STARTED, name, logo, webUrl, desc);
        tokenStore.invalidToken(clientIdStr);
        return SUCCESS_CODE;
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> delete(@NotEmpty Integer clientId) {
        String clientIdStr = clientId.toString();
        tokenStore.invalidToken(clientIdStr);
        try {
            clientDetailsService.removeClientDetails(clientIdStr);
        } catch (NoSuchClientException e) {
            //ignore this kind of exception
        }
        return SUCCESS_CODE;
    }

    /**
     * @param status 1 为待审核 2为已审核 -1为禁用
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Integer> update(@NotEmpty Integer clientId, String secret, String status) {
        String clientIdStr = clientId.toString();
        try {
            clientDetailsService.loadClientByClientId(clientIdStr);
        } catch (NoSuchClientException exception) {
            logger.error(exception.toString(), exception);
            throw new ServerException(StatusCode.OAUTH_CLIENT_NOT_EXISTS, "client id not exists");
        }
        BaseClientDetails clientDetails = buildDefault(clientIdStr);
        AppKeyInfo appKeyInfo = opManagerService.getAppKeyInfo(clientId);
        if (Strings.isNullOrEmpty(status)) {
            status = appKeyInfo.getStatus();
        }
        if (Strings.isNullOrEmpty(secret)) {
            secret = appKeyInfo.getAppSecret();
        }
        clientDetails.setClientSecret(secret);
        clientDetailsService.addClientDetails(clientDetails);
        opManagerService.updateAppKeySecret(clientId, secret, status);
        tokenStore.invalidToken(clientIdStr);
        return SUCCESS_CODE;
    }

    private BaseClientDetails buildDefault(String clientId) {
        Objects.nonNull(clientId);
        return new BaseClientDetails(clientId,
                SystemConstants.DEFAULT_RESOURCE_IDS, SystemConstants.DEFAULT_SCOPE,
                SystemConstants.DEFAULT_AUTHORIZED_GRANT_TYPES, SystemConstants.DEFAULT_AUTHORITIES);
    }
}
