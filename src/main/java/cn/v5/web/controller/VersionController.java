package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.AppVersion;
import cn.v5.entity.AppVersionKey;
import cn.v5.service.UserService;
import cn.v5.util.Constants;
import cn.v5.util.LoggerFactory;
import cn.v5.validation.Validate;
import com.google.common.collect.Maps;
import net.sf.oval.constraint.NotEmpty;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by piguangtao on 15/2/6.
 */
@Controller
@RequestMapping(value = "/api", produces = "application/json")
@Validate
public class VersionController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private static Map<String, Integer> SUCCESS_CODE = new HashMap<String, Integer>() {{
        put("error_code", StatusCode.SUCCESS);
    }};

    @Value("${version.admin.name}")
    private String versionAdminName;

    @Value("${version.admin.password}")
    private String versionAdminPassword;

    @Inject
    private UserService userService;

    /**
     * 获取指定版本的信息
     */
    @RequestMapping(value = "/client/latest_version", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> versionInfo(@NotNull Integer app_id, @NotNull Integer device_type, String cert) {
        if (device_type == Constants.ANDROID_DEVICE) {
            cert = Constants.ANDROID_CERT;
        } else {
            if (!Constants.IOS_ENTERPRISE_CERT.equals(cert) && !Constants.IOS_PRIVATE_CERT.equals(cert))
                throw new ServerException(StatusCode.PARAMETER_ERROR, "非法参数");
        }

        AppVersion appVersion = userService.getAppVersion(new AppVersionKey(app_id, device_type, cert));
        if (null == appVersion) {
            throw new ServerException(StatusCode.GET_VERSION_INFO_ERROR, "获取版本信息错误");
        }

        Map<String, Object> map = Maps.newHashMap();
        map.put("desc", appVersion.getDesc());
        if (device_type == Constants.ANDROID_DEVICE)
            map.put("download_url", StringUtils.split(appVersion.getDownloadUrl(), ","));
        else
            map.put("download_url", appVersion.getDownloadUrl());

        return map;
    }

    @RequestMapping(value = "/client/update_latest_version", method = RequestMethod.GET)
    public void updateLatestVersion(@NotEmpty String admin, @NotEmpty String passwd, @NotEmpty String verNo,
                                    @NotNull Integer app_id, @NotNull Integer device_type, String cert, String jsoncallback, HttpServletResponse response) {

        int code = 0;
        if (versionAdminName.equals(admin) && versionAdminPassword.equals(passwd)) {
            if (device_type == Constants.ANDROID_DEVICE)
                cert = Constants.ANDROID_CERT;

            AppVersion appVersion = userService.getAppVersion(new AppVersionKey(app_id, device_type, cert));
            String downloadUrl = appVersion.getDownloadUrl();
            if (device_type.equals(Constants.ANDROID_DEVICE)) {
                // android
                String[] urls = StringUtils.split(downloadUrl, ",");

                String localUrl = urls[1];
                localUrl = localUrl.substring(0, localUrl.indexOf("chatgame_")) + "chatgame_" + verNo + ".apk";

                appVersion.setDownloadUrl(urls[0] + "," + localUrl);
            } else {
                // ios
                appVersion.setDownloadUrl(downloadUrl.substring(0, downloadUrl.indexOf("chatgame_")) + "chatgame_" + verNo + ".plist");
            }

            userService.updateAppVersion(appVersion);
            code = 2000;
        }
        response.setContentType("text/javascript");
        String res = jsoncallback + "({\"code\":" + code + "})";
        try {
            response.getWriter().write(res);
        } catch (IOException e) {
        }
    }
}
