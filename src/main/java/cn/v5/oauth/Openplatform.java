package cn.v5.oauth;

/**
 * Created by fangliang on 16/5/7.
 */
public enum Openplatform {

    WeiXin(10) {
        @Override
        public OpenPlatformService createService(String key, String secret) {
            return new WeiXinAuthService(key, secret);
        }
    },

    QQ(11) {
        @Override
        public OpenPlatformService createService(String key, String secret) {
            return new QQAuthService(key, secret);
        }
    },

    WeiBo(12) {
        @Override
        public OpenPlatformService createService(String key, String secret) {
            return new WeiBoAuthService(key,secret);
        }
    },

    FaceBook(13) {
        @Override
        public OpenPlatformService createService(String key, String secret) {
            return new FaceBookAuthService(key,secret);
        }
    };

    private int type;

    private Openplatform(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public abstract OpenPlatformService createService(String key, String secret);

    public static Openplatform getOpenplatform(int type) {
        switch (type) {
            case 10:
                return WeiXin;
            case 11:
                return QQ;
            case 12:
                return WeiBo;
            case 13:
                return FaceBook;
        }
        return null;
    }

    public String formatUnioID(String unioid) {
        return String.format("%s_%s", unioid, this.getType());
    }

}
