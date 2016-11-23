package cn.v5.web.controller;

import cn.v5.bean.ivt.InvitePageBean;
import cn.v5.entity.User;
import cn.v5.service.MessageSourceService;
import cn.v5.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * 用户分享相关
 * Created by haoWang on 2016/4/22.
 */
@Controller
@RequestMapping(value = "/ivt")
public class InviteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InviteController.class);

    @Inject
    private UserService userService;

    @Value("${base.host}")
    private String baseHost;

//    @Inject
//    private MessageSource messageSource;

    @Autowired
    private MessageSourceService messageSourceService;


    @RequestMapping(value = "/{accountId}", method = RequestMethod.GET)
    public String card(@PathVariable String accountId, Model model) {
        User user = userService.getUserByAccoundId(accountId);
        if (user == null) {
            return "notfound";
        }
        InvitePageBean invitePageBean = buildInvitePageBean(user.getLocale(), user.getAppId());
        if (user.getAccount() == null) user.setAccount("unknown");
        model.addAttribute("user", user);
        model.addAttribute("ivt", invitePageBean);
        return "ivt";
    }

    private InvitePageBean buildInvitePageBean(Locale locale, Integer appID) {
        String title = messageSourceService.getMessageSource(appID).getMessage("ivt.title", new Object[]{}, locale);
        String account = messageSourceService.getMessageSource(appID).getMessage("ivt.account", new Object[]{}, locale);
        String invite = messageSourceService.getMessageSource(appID).getMessage("ivt.invite", new Object[]{}, locale);
        String isDownload = messageSourceService.getMessageSource(appID).getMessage("ivt.isDownload", new Object[]{}, locale);
        String download = messageSourceService.getMessageSource(appID).getMessage("ivt.download", new Object[]{}, locale);
        InvitePageBean invitePageBean = new InvitePageBean();
        invitePageBean.setAccount(account);
        invitePageBean.setTitle(title);
        invitePageBean.setInvite(invite);
        invitePageBean.setIsDownload(isDownload);
        invitePageBean.setDownload(download);
        return invitePageBean;
    }
}
