package cn.v5.trade.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by fangliang on 16/9/1.
 */
public class MD5Util {

    static ThreadLocal<MessageDigest> messageDigestHolder = new ThreadLocal();
    static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {

        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            messageDigestHolder.set(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Init security.MessageDigest failure ", e);
        }

    }

    public static String md5(String data) {
        try {
            MessageDigest e = messageDigestHolder.get();
            if (e == null) {
                e = MessageDigest.getInstance("MD5");
                messageDigestHolder.set(e);
            }

            e.update(data.getBytes());
            byte[] b = e.digest();
            String digestHexStr = "";

            for (int i = 0; i < 16; ++i) {
                digestHexStr = digestHexStr + byteHEX(b[i]);
            }

            return digestHexStr;
        } catch (Exception e) {
            throw new RuntimeException("Md5 Format Failure ", e);
        }
    }

    private static String byteHEX(byte ib) {
        char[] ob = new char[]{hexDigits[ib >>> 4 & 15], hexDigits[ib & 15]};
        String s = new String(ob);
        return s;
    }

}
