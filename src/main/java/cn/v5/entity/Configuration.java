package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

import java.util.HashMap;
import java.util.Map;

@Entity(table = "configuration",keyspace = "faceshow", comment = "通用元数据配置表,key/value pair")
public class Configuration {
    @Id
    private String key;

    @Column
    private Map<String,String> attrs = new HashMap<>();

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }
}
