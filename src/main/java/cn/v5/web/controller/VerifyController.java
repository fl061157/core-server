package cn.v5.web.controller;

import cn.v5.cache.CacheService;
import cn.v5.util.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping(value = "/v")
public class VerifyController {
    private static Logger logger = LoggerFactory.getLogger(VerifyController.class);

    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @RequestMapping(value = "/{code}", method = RequestMethod.GET)
    public String verify(@PathVariable String code, Model model) {
        String mobileCode = cacheService.get(code);
        try {

            String[] values = mobileCode.split(",");
            model.addAttribute("mobile", values[0]);
            model.addAttribute("code", values[1]);
            model.addAttribute("verified", true);
            cacheService.del(code);
        } catch (Exception e) {
            logger.error("verify code " + code + " error", e);
            model.addAttribute("verified", false);
        }
        return "verify";
    }
}
