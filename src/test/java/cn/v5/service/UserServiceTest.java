package cn.v5.service;

import cn.v5.entity.User;
import cn.v5.entity.vo.UserVo;
import cn.v5.test.TestTemplate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class UserServiceTest extends TestTemplate {

    @Autowired
    private UserService userService;

    @Test
    public void testGetUserDefaultAvatarAccessKey() {
        String accessKey = userService.getUserDefaultAvatarAccessKey();
        List<String> accessKeys = Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7", "8"});
        System.out.println("accesskey:" + accessKey);
        Assert.assertTrue(accessKeys.contains(accessKey));

        for (int i = 0; i < 100; i++) {
            accessKey = userService.getUserDefaultAvatarAccessKey();
            System.out.println("accesskey:" + accessKey);
            Assert.assertTrue(accessKeys.contains(accessKey));
        }
    }


    @Test
    public void testGetUserByAccount() {
        User user = userService.createNewUser("123451", "123451", "en_US", "0001", "chatgame-1.1", "nick1", null, 1);
        User user2 = userService.createNewUser("123452", "123452", "en_US", "0001", "chatgame-1.1", "nick2", null, 1);
        assertThat(user.getId()).isNotNull().isNotEmpty();
        assertThat(user.getAccount()).isNotNull().isNotEmpty();
        assertThat(user2.getId()).isNotNull().isNotEmpty();
        assertThat(user2.getAccount()).isNotNull().isNotEmpty();

        User gotUser = userService.getUserByAccoundId(user.getAccount());
        assertThat(gotUser.getId()).isEqualTo(user.getId());
        User gotUser2 = userService.getUserByAccoundId(user2.getAccount());
        assertThat(gotUser2.getId()).isEqualTo(user2.getId());
    }

    @Test
    public void testFindFriends() {
        User user = new User();
        user.setLanguage("zh_CN");
        List<UserVo> userVo = userService.findFriendUserList(user, "0cb6d443a4f811e3ac7abfd52f77bd19", 0);
        assertThat(userVo.size()).isEqualTo(0);
//        UserVo vo = userVo.get(0);
//        assertThat(vo.getId()).isEqualTo("0cb6d442a4f811e3ac7abfd52f77bd17");
//        assertThat(vo.getContactName()).isEqualTo("Ethernet");
        user = userService.findById(0,"0cb6d440a4f811e3ac7abfd52f77bd17");
        userService.updateContactName(user, "0cb6d442a4f811e3ac7abfd52f77bd17", 0, "Quake");


        userVo = userService.findFriendUserList(user, "0cb6d440a4f811e3ac7abfd52f77bd17", 0);
        assertThat(userVo.size()).isEqualTo(1);
//        vo = userVo.get(0);
//        assertThat(vo.getId()).isEqualTo("0cb6d442a4f811e3ac7abfd52f77bd17");
//        assertThat(vo.getContactName()).isEqualTo("Quake");

    }

    @Test
    public void testAuthorize() {
        String session = "12345678900987654321";
        User userLogin = userService.createNewUser("99999", "99999", "en_US", "0001", "chatgame-1.1", "nick-login", null, 1);
        //保存人 最新的session_Id
        userService.saveUserSessionIndex(userLogin.getId(), userLogin.getAppId(), session);
        //保存session对应的user_Id
        userService.saveUserSession(session, userLogin.getId(), userLogin.getAppId());
        Assert.assertEquals(userService.authorize(session, 0), userLogin);
    }

    @Test
    public void testIsExistMobile() {
        User user = userService.createNewUser("99999", "123451", "en_US", "0001", "chatgame-1.1", "nick1", null, 1);
        Assert.assertNotNull(userService.isExistMobile("123451", "0001"));
        Assert.assertNull(userService.isExistMobile("123451", "0002"));
    }

    @Test
    public void testFindUserListByNames() {
        List<String> mems = Lists.newArrayList();
        mems.add("0cb6d442a4f811e3ac7abfd52f77bd17");
        mems.add("0cb6d443a4f811e3ac7abfd52f77bd17");
        Assert.assertEquals(userService.findUserListByNames(mems).size(), 2);
    }

    @Test
    public void testFindMobileIndex() {
        Assert.assertEquals(userService.findMobileIndex("099891fc266cad3d6dae315d9518dd32", "0086").getUserId(), "0cb6d440a4f811e3ac7abfd52f77bd17");
    }

    @Test
    public void testFindMobileIndexList() {
        List<String> mobileList = Lists.newArrayList();
        mobileList.add("099891fc266cad3d6dae315d9518dd32");
        mobileList.add("9412413345a0b706a74e7f0694132859");
        Map<String, String> mobileCodes = Maps.newHashMap();
        mobileCodes.put("099891fc266cad3d6dae315d9518dd32", "0086");
        mobileCodes.put("9412413345a0b706a74e7f0694132859", "0086");
        Assert.assertEquals(userService.findMobileIndexList(mobileList, mobileCodes).size(), 2);
    }

    @Test
    public void testfindUserByMobileIndex() {
        Assert.assertEquals(userService.findUserByMobileIndex("099891fc266cad3d6dae315d9518dd32").size(), 1);
    }


    @Test
    public void testfindById() {
        Assert.assertEquals(userService.findById(0,"0cb6d440a4f811e3ac7abfd52f77bd17").getMobile(), "099891fc266cad3d6dae315d9518dd32");
    }

    @Test
    public void testfindByIdList() {
        List<String> r = Lists.newArrayList();
        r.add("0cb6d440a4f811e3ac7abfd52f77bd17");
        r.add("0cb6d442a4f811e3ac7abfd52f77bd17");
        Assert.assertEquals(userService.findByIdList(r).size(), 2);
    }

    @Test
    public void testfindUserSessionIndexByKey() {
        userService.saveUserSessionIndex("0cb6d440a4f811e3ac7abfd52f77bd17", 0, "0");
        Assert.assertEquals(userService.findUserSessionIndexByKey("0cb6d440a4f811e3ac7abfd52f77bd17", 0).getSessionId(), "0");
    }

    @Test
    public void testSaveToken() {
        Assert.assertNotNull(userService.saveToken("asdfsadf", 0, "0cb6d440a4f811e3ac7abfd52f77bd17", 1, "test"));
        userService.removeToken("0cb6d440a4f811e3ac7abfd52f77bd17");
        userService.saveToken("asdfsadf", 0, "0cb6d440a4f811e3ac7abfd52f77bd17", 1, "test");
        Assert.assertNotNull(userService.getTokenInfo("0cb6d440a4f811e3ac7abfd52f77bd17", 0));
        userService.removeToken("0");
    }

    @Test
    public void testUserSession() {
        userService.saveUserSession("1", "0cb6d444a4f811e3ac7abfd52f77bd17", 0);
        Assert.assertNotNull(userService.findUserSession("1"));
        Assert.assertNull(userService.findUserSession("3"));
    }

    @Test
    public void testUserModify() {
        User user = userService.findById("0cb6d440a4f811e3ac7abfd52f77bd17");
//        userService.saveUserSessionToRedis(user, "0", 0);
        userService.setUserDisturb(user, "yes", "0", "0");
    }

    @Test
    public void testCreateNewUser() {

        User user = userService.createNewUser("13801543111", "13801543111", "zh", "0086", "1", "Jc", null, 2);
        Assert.assertEquals(userService.findById(user.getId()), user);
    }

    @Test
    public void testFindUserByMobileIndex() {
        String mobile = "110";
        List<User> users = userService.findUserByMobileIndex(mobile);
        System.out.print(users);
    }

}
