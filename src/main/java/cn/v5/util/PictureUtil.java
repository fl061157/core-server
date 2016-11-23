package cn.v5.util;


public class PictureUtil {

    public static final String SUFFIX_THUMB = ".thumb";

    /**
     * @param avatarStoragePath configUtils.getString("avatar.storage.path")
     * @param fileName          文件名
     * @return path
     */
    public static String genPicturePath(String avatarStoragePath, String fileName) {
        return avatarStoragePath + fileName.substring(0, 2) + "/" + fileName.substring(2, 4) + "/";
    }

    /**
     * @param baseUrl   configUtils.getString("base.url")
     * @param avatarUrl configUtils.getString("avatar.url")
     * @param fileName  文件名
     * @return url
     */
    public static String genPictureThumbUrl(String baseUrl, String avatarUrl, String fileName) {
        return baseUrl + avatarUrl + fileName;
    }

}
