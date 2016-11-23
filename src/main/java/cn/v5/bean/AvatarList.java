package cn.v5.bean;

import java.util.Arrays;

/**
 * Created by sunhao on 14-7-1.
 */
public class AvatarList {
    public static class AvatarInfo {
        private String id;
        private String version;
        private String url;

        public AvatarInfo() {
        }

        public AvatarInfo(String id, String version, String url) {
            this.id = id;
            this.version = version;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "AvatarInfo{" +
                    "id='" + id + '\'' +
                    ", version='" + version + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    private static AvatarList avatarList;

    public static AvatarList getAvatarList() {
        return avatarList;
    }

    public static void setAvatarList(AvatarList avatarList) {
        AvatarList.avatarList = avatarList;
    }

    private AvatarInfo[] avatars;

    public AvatarList() {
    }

    public AvatarList(AvatarInfo[] avatars) {
        this.avatars = avatars;
    }

    public AvatarInfo[] getAvatars() {
        return this.avatars;
    }

    public void setAvatars(AvatarInfo[] avatars) {
        this.avatars = avatars;
    }

    @Override
    public String toString() {
        return "AvatarList{" +
                "avatars=" + Arrays.toString(avatars) +
                '}';
    }
}
