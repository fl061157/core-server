package cn.v5.web.controller;

import cn.v5.code.StatusCode;
import cn.v5.entity.BeautyRegionConfig;
import cn.v5.service.BeautyService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fangliang on 20/10/15.
 */

@Controller
@RequestMapping(value = "/api/beauty", produces = "application/json")
public class BeautyController {

    @Autowired
    private BeautyService beautyService;

    @RequestMapping(value = "/region/{countrycode}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map findBeautyRegionConfig(@PathVariable(value = "countrycode") String countrycode) {

        if (StringUtils.isBlank(countrycode)) {
            throw new ServerException(StatusCode.PARAMETER_ERROR, "countrycode can't be empty.");
        }

        BeautyRegionConfig regionConfig = beautyService.findBeautyRegion(countrycode);

        if (regionConfig == null) {
            throw new ServerException(StatusCode.OBJECT_NOT_FOUND, String.format("countrycode:%s config not exists", countrycode));
        }

        Map m = new HashMap<>();

        m.put("error_code", StatusCode.SUCCESS);
        m.put("data", regionConfig);

        return m;
    }


}
