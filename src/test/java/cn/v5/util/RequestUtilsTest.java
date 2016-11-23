package cn.v5.util;

import junit.framework.TestCase;
import org.springframework.mock.web.MockHttpServletRequest;

public class RequestUtilsTest extends TestCase {

    private RequestUtils requestUtils = new RequestUtils();

    public void testIsChatgameV1() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("client-version","chatgame-1.2.206");

        assertTrue(requestUtils.isChatgameV1(request));


        request = new MockHttpServletRequest();
        request.addHeader("client-version","chatgame-2.0.206");

        assertFalse(requestUtils.isChatgameV1(request));

    }
}