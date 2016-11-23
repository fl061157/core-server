package cn.v5.entity;

import info.archinnov.achilles.persistence.PersistenceManager;

/**
 * 记录当前登录用户，通过ThreadLocal来保存
 */
public class CurrentUser {
    private static ThreadLocal<User> holder = new InheritableThreadLocal<User>();
    private static ThreadLocal<PersistenceManager> dbHolder = new InheritableThreadLocal<>();

    public static void clearUser() {
        holder.set(null);
        dbHolder.set(null);
    }

    public static User user() {
        return holder.get();
    }

    public static PersistenceManager db() {
        return dbHolder.get();
    }

    public static void user(final User user) {
        holder.set(user);
    }

    public static void setDB(final PersistenceManager db) {
        dbHolder.set(db);
    }
}
