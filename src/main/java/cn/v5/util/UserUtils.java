package cn.v5.util;


import cn.v5.bean.group.GroupUser;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Group;
import cn.v5.entity.User;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.common.collect.Lists;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: sunhao
 * Date: 13-9-16
 * Time: 下午1:56
 */
@Service
public class UserUtils {
    private static Logger LOGGER = LoggerFactory.getLogger(UserUtils.class);

    private static final String USERID_APPID_SPBLIT = "_";
    private static final String THIRDID_APPID_SPLIT = "@\\{";

    public static String getAppUserId(String userId, String appId) {
        return getAppUserId(userId, Short.valueOf(appId));
    }

    public static String getUserId(String appUserId) {
        Assert.notNull(appUserId);
        return appUserId.split(USERID_APPID_SPBLIT)[0];
    }

    public static String getAppUserId(String userId, short appId) {
        return userId + USERID_APPID_SPBLIT + appId;
    }

    public static String getAppId(String appUserId) {
        Assert.notNull(appUserId);
        Assert.state(appUserId.contains(USERID_APPID_SPBLIT), "appUserId:" + appUserId + " not contain appId");
        return appUserId.split(USERID_APPID_SPBLIT)[1];
    }

    public static String formatLanguage(String language) {
        String result = "en";
        if (null != language) {
            language = language.toLowerCase();
            if (language.contains("zh")) {
                result = "zh";
            }
        }
        return result;
    }

    public static boolean isUserHideMessage(String hideTime, String timeZone) {
        boolean result = false;
        if (null != hideTime && !"".equals(hideTime)) {
            if (null != timeZone && !"".equals(timeZone)) {
                try {
                    String prefix = timeZone.substring(0, 1);
                    String offsetStr = timeZone.substring(1);
                    String[] offsetArray = offsetStr.split(":");
                    int hourOffset = Integer.parseInt(offsetArray[0]);
                    int minoffset = 0;
                    if (offsetArray.length == 2) {
                        minoffset = Integer.parseInt(offsetArray[1]);
                    }
                    if ("-".equals(prefix)) {
                        hourOffset = 0 - hourOffset;
                        minoffset = minoffset != 0 ? 0 - minoffset : minoffset;
                    }
                    result = isHideTimeValid(hideTime, hourOffset, minoffset);
                } catch (Exception e) {
                    //解析出错时，默认按照无免打扰
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    private static boolean isHideTimeValid(String hideTime, int hourOffset, int minoffset) {
        boolean result = false;   //可以发
        if (hideTime == null || hideTime.length() != 11) {
            return result;
        }

        hideTime = hideTime.replaceAll(":", "");
        int len = hideTime.indexOf("-");

        DateFormat format = new SimpleDateFormat("HHmm");
        Calendar calendar = Calendar.getInstance();
        //获取时间偏移量
        int zoneOffset = calendar.get(Calendar.ZONE_OFFSET);
        //获取夏令时差
        int dstOffset = calendar.get(Calendar.DST_OFFSET);
        calendar.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));

        calendar.add(Calendar.HOUR_OF_DAY, hourOffset);
        if (0 != minoffset) {
            calendar.add(Calendar.MINUTE, minoffset);
        }

        Integer nowTime = Integer.parseInt(format.format(calendar.getTime()));
        Integer startTime = Integer.parseInt(hideTime.substring(0, len));
        Integer endTime = Integer.parseInt(hideTime.substring(len + 1));

        //开始和结束时间再同一天
        if (startTime < endTime) {
            //在限制区间内 不发
            if (startTime < nowTime && nowTime < endTime) {
                result = true;
            }
        } else {
            //在限制区间内 不发
            if ((startTime < nowTime && nowTime < 2400) || nowTime < endTime) {
                result = true;
            }
        }
        return result;
    }

    public String generateNewSessionId() {
        return UUID.randomUUID().toString().replaceAll("\\-", "");
    }


    public static String getSecrateNickName(User user) {
        String nickName = "ChatGame";
        if (null != user) {
            String language = user.getLanguage();
            if (StringUtils.isNotBlank(language) && language.toLowerCase().startsWith("zh")) {
                nickName = "小秘书";
            }
        }
        return nickName;
    }

    public static String genInternalUserId(String thirdId, String appKey) {
        Integer appId = Integer.parseInt(appKey);
        if (appId <= SystemConstants.CG_APP_ID_MAX) {
            return thirdId;
        }
        return thirdId + "@{" + appKey + "}";
    }

    public static String genInternalUserId(String thirdId, Integer appKey) {
        if (appKey <= SystemConstants.CG_APP_ID_MAX) {
            return thirdId;
        }
        return thirdId + "@{" + appKey + "}";
    }

    public static String genOpenUserId(String v5Id) {
        List<String> list = Lists.newArrayList(v5Id.split(THIRDID_APPID_SPLIT));
        list.remove(list.size() - 1);
        return list.stream().collect(Collectors.joining("@{"));
    }

    public static String genOpenUserId(String v5Id, Integer appId) {
        if (appId <= SystemConstants.CG_APP_ID_MAX) {
            return v5Id;
        }
        List<String> list = Lists.newArrayList(v5Id.split(THIRDID_APPID_SPLIT));
        list.remove(list.size() - 1);
        return list.stream().collect(Collectors.joining("@{"));
    }

    public static Group genOpenGroup(Group group, Integer appId) {
        if (appId > SystemConstants.CG_APP_ID_MAX) {
            Group groupVo = new Group();
            BeanUtils.copyProperties(group, groupVo);
            groupVo.setId(GroupUtil.openID(groupVo.getId() , appId));
            groupVo.setCreator(GroupUtil.openID(groupVo.getCreator() , appId));
            List<GroupUser> groupUsers = groupVo.getMembers().stream().map(k -> {
                GroupUser openGroupUser = new GroupUser();
                BeanUtils.copyProperties(k, openGroupUser);
                openGroupUser.setId(GroupUtil.openID(openGroupUser.getId() , appId ));
                return openGroupUser;
            }).collect(Collectors.toList());
            groupVo.setMembers(groupUsers);
            return groupVo;
        } else {
            return group;
        }
    }

    public static void main(String[] args) {
        System.out.println(genOpenUserId("cc0ced70849d11e6a854d360e3188492" , 203538));
//        System.out.println(genOpenUserId("ssvsda"));
        System.out.println(genInternalUserId("cc0ced70849d11e6a854d360e3188492", 203538));
    }

}

