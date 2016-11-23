package cn.v5.localentity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * @author xiechanglei 文件存储的类 主要存放有 id(文件对应的唯一标示 ),
 *         service(文件所属的存储服务，如amazons3，阿里云), bucketname(文件所属的空间),
 *         region(文件存放的地点,在amazon s3中有这个设置需求) date(文件上传的日期)
 */
@Entity(table = "fileinfo")
public class PersistentFile {
    @Id
    private String id;// 文件id (uuid) 程序中已经生成
    @Column
    private String service; // 存储服务 amazon s3 ，阿里云
    @Column
    private String bucketname;// 空间名称
    @Column
    private String region;// 地区 amazon 分数据中心
    @Column
    private String date; // 文件上传日期 格式为yyyy-MM-dd HH:mm:ss
    @Column
    private String creator;

    @Column
    private String ext; //文件后缀

    @Column
    private Boolean pub;

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getBucketname() {
        return bucketname;
    }

    public void setBucketname(String bucketname) {
        this.bucketname = bucketname;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Boolean getPub() {
        return pub;
    }

    public void setPub(Boolean pub) {
        this.pub = pub;
    }

    public PersistentFile() {

    }
}
