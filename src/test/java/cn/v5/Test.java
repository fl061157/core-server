package cn.v5;

import cn.v5.bean.RecommandUserWithOtherApp;
import cn.v5.code.StatusCode;
import cn.v5.entity.Group;
import cn.v5.entity.MobileIndex;
import cn.v5.entity.User;
import cn.v5.json.ObjectMapperFactoryBean;
import cn.v5.util.JsonUtil;
import cn.v5.web.controller.ServerException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by piguangtao on 15/9/26.
 */
public class Test {

    private Function<MobileIndex, User> findMobileIndexToUser = new Function<MobileIndex, User>() {
        @Override
        public User apply(MobileIndex input) {
            User user = null;
            if (null != input) {
                if ("c".equals(input.getUserId())) {
                    return null;
                } else {
                    user = new User();
                    user.setId(input.getUserId());
                }
            }
            return user;
        }
    };

    @org.junit.Test
    public void test() {
        List<MobileIndex> listMobiles = new ArrayList<>();

//        MobileIndex mobileIndex1 = new MobileIndex();
//        MobileKey mobileKey = new MobileKey();
//        mobileKey.setCountrycode("0092");
//        mobileKey.setMobile("1");
//        mobileIndex1.setMobileKey(mobileKey);
//        mobileIndex1.setUserId("a");
//
//        listMobiles.add(mobileIndex1);
//
//        MobileIndex mobileIndex2 = new MobileIndex();
//        MobileKey mobileKey2 = new MobileKey();
//        mobileKey2.setCountrycode("0095");
//        mobileKey2.setMobile("1");
//        mobileIndex2.setMobileKey(mobileKey2);
//        mobileIndex2.setUserId("c");
//
//        listMobiles.add(mobileIndex2);

        List list = FluentIterable.from(listMobiles).transform(findMobileIndexToUser).filter(input -> input != null).toList();
//        List list = FluentIterable.from(listMobiles).toList();

        System.out.println(list);
    }


    @org.junit.Test
    public void testJson() {
        String contact = "[[\"0066a22d5e0e01af5a7bb4f77c286078f95b\",\"ลาน\n" +
                "\n" +
                "น้ำริด.\"]]";

//        contact = "[[\"00947a28354566df14d515ff13ee3680e77e\",\"Ainas\"],[\"00945ff14aab203693bf772573668b787541\",\"a\\cakil\"]]";

        contact = contact.replaceAll("\n", " ").replaceAll("\\\\", "");
        List<List<String>> mobileNames = null;
        try {
            mobileNames = JsonUtil.fromJson(contact, List.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(mobileNames);
    }

    @org.junit.Test
    public void test1() {
        String fromVersion = "2.4.44";
        String toVersion = "3.0.11";

        assertTrue(fromVersion.startsWith("2."));
        assertTrue(toVersion.startsWith("3."));

        fromVersion = "3.4.44";
        assertFalse(fromVersion.startsWith("2."));

        String info = "{\"to\":\"3.4.44\",\"from\":\"2.4.44\"}";

        ObjectMapper objectMapper = new ObjectMapper();


        if (StringUtils.isBlank(info)) return;
        JsonNode infoNode = null;
        try {
            infoNode = objectMapper.readTree(info);
            String from = infoNode.get("from").textValue();
            String to = infoNode.get("to").textValue();
            if (StringUtils.isNotBlank(from) && StringUtils.isNotBlank(to)) {
                System.out.println(from);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        info = "{\\\"to\\\":\\\"3.4.46\\\",\\\"from\\\":\\\"3.4.44\\\"}";
        System.out.print(info.replaceAll("\\\\", ""));
    }

    @org.junit.Test
    public void test2() {
        String singleMobile = "1345678";

        if (singleMobile.length() < 5) {
            return;
        }
        String authCode = singleMobile.substring(singleMobile.length() - 4);
        assertEquals("5678", authCode);


    }

    @org.junit.Test
    public void test3() {
        System.out.println("chatgame-3.0.6".compareTo("chatgame-3.1") < 0);
        System.out.println("chatgame-3.0.18".compareTo("chatgame-3.1") < 0);

        System.out.println("chatgame-3.1.0".compareTo("chatgame-3.1") < 0);

        System.out.println("chatgame-2.1.0".compareTo("chatgame-3.1") < 0);
    }

    @org.junit.Test
    public void testObjectMapper() throws Exception {
        ObjectMapperFactoryBean bean = new ObjectMapperFactoryBean();
        ObjectMapper objectMapper = bean.getObject();

//        GroupContainFailUserVo vo = new GroupContainFailUserVo();
//        vo.setId("dadadfa");
//
//        FailUserInfo userInfo = new FailUserInfo();
//        userInfo.setUserId("dafada");
//
//        vo.getFail().add(userInfo);
//        System.out.println(objectMapper.writeValueAsString(vo));
        String src = "{\"id\":\"ca2946109a4d11e5a208fb907fee57ba\",\"creator\":\"d0f1414098e611e5b23809cabb80e679\",\"avatarUrl\":\"http://new.image.chatgame.me/api/file/avatar/2015/11/24/8/20fc6bdd-a827-4c1a-835e-613d844def64\",\"createTime\":1449209459953,\"updateTime\":1449209459953,\"enableValidate\":\"no\",\"desc\":\"暂无群简介\",\"region\":\"0086\",\"account\":\"8600311\",\"name\":\"7\",\"number\":5,\"members\":[{\"id\":\"834cf1c0996611e5b5567ddd1535be6d\",\"nickname\":\"1111\",\"mobile\":\"55987659f07cf357aadbbb88ce37a3de\",\"sex\":2,\"avatar\":\"http://us.image.chatgame.me/api/file/avatar/2015/12/3/2/27e8ff36-0645-4b41-88fe-c98d1fbbd510\",\"avatar_url\":\"http://us.image.chatgame.me/api/file/avatar/2015/12/3/2/27e8ff36-0645-4b41-88fe-c98d1fbbd510\",\"regSource\":\"chatgame\",\"language\":\"zh_CN\",\"userType\":0,\"hideTime\":null,\"timezone\":\"+8:00\",\"countrycode\":\"0945\",\"mobileVerify\":1,\"createTime\":1449110127372,\"lastLoginTime\":1449110127372,\"lastUpdateTime\":1449110127372,\"appId\":0,\"publicKey\":null,\"account\":\"9451569403\",\"conversation\":null,\"touch\":null,\"mobilePlaintext\":\"1234567890\",\"tcpServer\":null,\"fileServer\":null,\"sessionId\":null,\"seq\":2,\"locale\":\"zh_CN\"},{\"id\":\"87180ae0996311e5b5567ddd1535be6d\",\"nickname\":\"Test\",\"mobile\":\"55987659f07cf357aadbbb88ce37a3de\",\"sex\":2,\"avatar\":\"http://us.image.chatgame.me/api/file/avatar/2015/12/3/2/7cc411b4-a22d-4005-b439-f030a4ddbc80\",\"avatar_url\":\"http://us.image.chatgame.me/api/file/avatar/2015/12/3/2/7cc411b4-a22d-4005-b439-f030a4ddbc80\",\"regSource\":\"chatgame\",\"language\":\"zh_CN\",\"userType\":0,\"hideTime\":null,\"timezone\":\"+8:00\",\"countrycode\":\"0966\",\"mobileVerify\":1,\"createTime\":1449108845223,\"lastLoginTime\":1449108845223,\"lastUpdateTime\":1449108845223,\"appId\":0,\"publicKey\":null,\"account\":\"9661569390\",\"conversation\":null,\"touch\":null,\"mobilePlaintext\":\"1234567890\",\"tcpServer\":null,\"fileServer\":null,\"sessionId\":null,\"seq\":4,\"locale\":\"zh_CN\"},{\"id\":\"8c8eb19098e611e5983eb3c6d46399e0\",\"nickname\":\"11\",\"mobile\":\"a33696dee36f415cfb7b9273c234b6a1\",\"sex\":2,\"avatar\":\"http://us.image.chatgame.me/api/file/avatar/2015/12/2/11/f3ec4e1c-d0cf-48c7-993a-60b7742ea570\",\"avatar_url\":\"http://us.image.chatgame.me/api/file/avatar/2015/12/2/11/f3ec4e1c-d0cf-48c7-993a-60b7742ea570\",\"regSource\":\"chatgame\",\"language\":\"zh_CN\",\"userType\":0,\"hideTime\":null,\"timezone\":\"+8:00\",\"countrycode\":\"0967\",\"mobileVerify\":1,\"createTime\":1449055167285,\"lastLoginTime\":1449055167285,\"lastUpdateTime\":1449055167285,\"appId\":0,\"publicKey\":null,\"account\":\"9671567670\",\"conversation\":null,\"touch\":null,\"mobilePlaintext\":\"12345678\",\"tcpServer\":null,\"fileServer\":null,\"sessionId\":null,\"seq\":3,\"locale\":\"zh_CN\"},{\"id\":\"d0f1414098e611e5b23809cabb80e679\",\"nickname\":\"90\",\"mobile\":\"55987659f07cf357aadbbb88ce37a3de\",\"sex\":2,\"avatar\":\"http://new.image.chatgame.me/api/file/avatar/2015/12/2/11/06f370f3-ec36-49b9-b707-b8f1bb39b0f8\",\"avatar_url\":\"http://new.image.chatgame.me/api/file/avatar/2015/12/2/11/06f370f3-ec36-49b9-b707-b8f1bb39b0f8\",\"regSource\":\"chatgame\",\"language\":\"zh_CN\",\"userType\":0,\"hideTime\":null,\"timezone\":\"+8:00\",\"countrycode\":\"0086\",\"mobileVerify\":1,\"createTime\":1449055282013,\"lastLoginTime\":1449055282013,\"lastUpdateTime\":1449055282013,\"appId\":0,\"publicKey\":null,\"account\":\"86375431\",\"conversation\":null,\"touch\":null,\"mobilePlaintext\":\"1234567890\",\"tcpServer\":null,\"fileServer\":null,\"sessionId\":null,\"seq\":1,\"locale\":\"zh_CN\"},{\"id\":\"e763ac40d03511e3bbf113abbfd80b2e\",\"nickname\":\"皮皮\",\"mobile\":\"fa1fc49b2b5a8de27e2b3e9f850f7f80\",\"sex\":0,\"avatar\":\"http://new.image.chatgame.me/api/file/avatar/2015/9/23/9/cae9aad6-e936-4411-8a14-1665cb8064c4\",\"avatar_url\":\"http://new.image.chatgame.me/api/file/avatar/2015/9/23/9/cae9aad6-e936-4411-8a14-1665cb8064c4\",\"regSource\":\"chatgame\",\"language\":\"zh\",\"userType\":0,\"hideTime\":\"\",\"timezone\":\"+8:00\",\"countrycode\":\"0086\",\"mobileVerify\":1,\"createTime\":1398841568516,\"lastLoginTime\":1398841568516,\"lastUpdateTime\":1442999218762,\"appId\":0,\"publicKey\":null,\"account\":\"8600777\",\"conversation\":null,\"touch\":null,\"mobilePlaintext\":\"13770508309\",\"tcpServer\":null,\"fileServer\":null,\"sessionId\":null,\"seq\":5,\"locale\":\"zh\"}],\"conversation\":0}";
        try {
            Group group = objectMapper.readValue(src, Group.class);
            System.out.println(group);
        } catch (IOException e) {
            throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
        }

    }

    @org.junit.Test
    public void testMap() {
        Map<String, RecommandUserWithOtherApp> userIdRecommandMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            RecommandUserWithOtherApp otherApp = new RecommandUserWithOtherApp();
            otherApp.setAppUserId(UUID.randomUUID().toString());
            userIdRecommandMap.put(UUID.randomUUID().toString(), otherApp);
        }

        RecommandUserWithOtherApp[] recommandUserWithOtherAppList = (userIdRecommandMap.values().toArray(new RecommandUserWithOtherApp[]{}));

        System.out.println(Arrays.toString(recommandUserWithOtherAppList));

    }

    @org.junit.Test
    public void testResult() {
        String test = "{\"error\":\"the group does not exist.\",\"error_code\":4019}";
        ObjectMapperFactoryBean bean = new ObjectMapperFactoryBean();
        try {
            Group group = bean.getObject().readValue(test, Group.class);
            if (null != group) {
                if (StringUtils.isBlank(group.getId())) {
                    throw new ServerException(StatusCode.GROUP_INFO_NOT_EXIST, "the group does not exist.");
                }
            }
        } catch (Exception e) {
            assertTrue(Boolean.TRUE);
        }
    }
}
