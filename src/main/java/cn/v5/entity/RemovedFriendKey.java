package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Order;

public class RemovedFriendKey {
    @Column(name = "user_id")
    @Order(1)
    private String id;

    @Column(name = "removed_mobile")
    @Order(2)
    private String mobile; //country_code + mobile

    public RemovedFriendKey() {
    }

    public RemovedFriendKey(String id, String mobile) {
        this.id = id;
        this.mobile = mobile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof RemovedFriendKey)) return false;

        RemovedFriendKey that = (RemovedFriendKey) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (mobile != null ? !mobile.equals(that.mobile) : that.mobile != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "RemovedFriendKey{" +
                "id='" + id + '\'' +
                ", mobile='" + mobile + '\'' +
                '}';
    }
}
