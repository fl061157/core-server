package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;
import info.archinnov.achilles.type.Counter;

@Entity(table = "table_counter", keyspace = "faceshow", comment="冗余计数器,统计用户数通讯录数等等")
public class TableCount {
    @Id(name = "table_name")
    private String table;

    @Column
    private Counter count;

    public TableCount() {

    }

    public TableCount(String table, Counter count) {
        this.table = table;
        this.count = count;
    }
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Counter getCount() {
        return count;
    }

    public void setCount(Counter count) {
        this.count = count;
    }
}
