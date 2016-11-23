package cn.v5.util;

import org.apache.commons.lang.RandomStringUtils;

import java.util.HashSet;
import java.util.Set;

public class NameGenerator {
   final static String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
    /**
     * @param args
     */

    public static void main(String[] args) {

        String result = generateKey(6);
        System.out.println("num generateKey  :U_" + result);
//        Set<String> set = new HashSet<String>();
//         int max = 1000000;
//        for (int i = 1; i <= max; i++) {
//             String result = generateKey(6);
//             set.add(result);
//
//            // 打印出结果
//            System.out.println(result);
//        }
//        System.out.println("num is  :" + (max - set.size()));
//        System.out.println("ok is :" + (set.size() == max));
//    System.out.println(generateKey());
    }




    public static String generateKey(int num) {

        return  RandomStringUtils.random(num, letters);
    }

}


