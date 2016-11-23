package cn.v5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by piguangtao on 15/8/3.
 */
public class FlumeTest {
    public static void testMatch(String patterStr, String data) {
        Pattern pattern = Pattern.compile(patterStr);
        Matcher matcher = pattern.matcher(data);

        System.out.println("pattern:" + patterStr);
        System.out.println("data:" + data);

        //部分匹配
        System.out.println("find===" + matcher.find());


//        while (matcher.find()) {
//            System.out.print("Start index: " + matcher.start());
//            System.out.print(" End index: " + matcher.end() + " ");
//            System.out.println(matcher.group());
//        }

    }

    public static void main(String[] args) {
        String patterStr = "";
        String data = "";
        patterStr = "((^.*?\\|.*?\\|CoreServer\\|.*?\\|.*?\\|/api)|(^.+?\\|.+?\\|AttachServer\\|.+?\\|.+?\\|/api/game/online_op\\|))";

        data = "200|10.0.20.114|CoreServer|2015-08-02 16:18:50,430|1438532330430|/api/user/message/unread|6ce30670cab011e481cc3d3160ae1ee8|6491413b26384cf8b7551c533176c72a|96800748|hassan aziza|99239901|0968|CG2.0.157_iOS_8.40_Apple_iPhone6,2_iOS_ar-OM_0.0|chatgame-2.0.157|188.140.162.228, 31.186.228.60, 10.0.10.98|1426381981784|2015-03-15 01:13:01|2";
        testMatch(patterStr, data);

        data = "200|10.0.20.114|CoreServer|2015-08-02 16:18:50,430|1438532330430|user_add_friend|6ce30670cab011e481cc3d3160ae1ee8|6491413b26384cf8b7551c533176c72a|96800748|hassan aziza|99239901|0968|CG2.0.157_iOS_8.40_Apple_iPhone6,2_iOS_ar-OM_0.0|chatgame-2.0.157|188.140.162.228, 31.186.228.60, 10.0.10.98|1426381981784|2015-03-15 01:13:01|2";
        testMatch(patterStr, data);

        data = "jp_1|52.69.39.196|AttachServer|2 08:00:12.217|1438588812216|/api/game/online_op|f127d8c038d811e591b38d8d78252756|1a14142552ff489fafc381a4c0fc110b|8618159|我自欲为懵懂客|15116968997|0086|0|f127d8c038d811e591b38d8d78252756||1|1438495379030|218.106.182.99|CG2.0.157_iOS_8.40_Apple_iPhone7,1_iOS_zh-CN_8.0|chatgame-2.0.157";
        testMatch(patterStr, data);
    }
}
