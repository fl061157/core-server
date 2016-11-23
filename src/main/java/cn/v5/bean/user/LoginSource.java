package cn.v5.bean.user;

/**
 * Created by haoWang on 2016/5/5.
 */
public enum LoginSource {
    WeiXin {
        @Override
        public String prefix() {
            return "weixin";
        }
    },
    FaceBook {
        @Override
        public String prefix() {
            return "facebook";
        }
    };

    public abstract String prefix();

    public static String getPrefix(int i) {
        for (LoginSource source : LoginSource.values()) {
            if (source.ordinal() == i) {
                return source.prefix();
            }
        }
        throw new IllegalArgumentException("Illegal type");
    }
}
