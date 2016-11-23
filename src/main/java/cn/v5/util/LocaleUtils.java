package cn.v5.util;

import java.util.Locale;

/**
 * 语言转换成标准的Locale的工具类
 *
 * 注意：在区分大小写的操作系统上，区分语言文件名的大小写。文件名必须按照Locale类的结果命名
 *
 * 具体文档参见Locale.forLanguageTag的javadoc和IETF BCP 47
 *
 */
public class LocaleUtils {
    public static Locale parseLocaleString(String localeCode, Locale defaultLocale) {
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault();
        }
        if (localeCode == null || localeCode.isEmpty()) {
            return defaultLocale;
        }
        String code = localeCode.replace("_", "-");
        return Locale.forLanguageTag(code);
    }

    public static Locale parseLocaleString(String localeCode) {
        return parseLocaleString(localeCode, Locale.US);
    }
}
