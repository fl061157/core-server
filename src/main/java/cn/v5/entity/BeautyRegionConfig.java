package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by fangliang on 20/10/15.
 */

@Entity(table = "beauty_region_config", keyspace = "faceshow", comment = "美颜配置")
public class BeautyRegionConfig {

    @Id
    private String countrycode;

    @Column(name = "light")
    private Integer light;

    @Column(name = "color")
    private Integer color;

    @Column(name = "epo")
    private Integer epo;

    @Column(name = "thin")
    private Integer thin;

    @Column(name = "strength")
    private Integer strength ;


    public String getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(String countrycode) {
        this.countrycode = countrycode;
    }

    public Integer getLight() {
        return light;
    }

    public void setLight(Integer light) {
        this.light = light;
    }

    public Integer getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color = color;
    }

    public Integer getEpo() {
        return epo;
    }

    public void setEpo(Integer epo) {
        this.epo = epo;
    }

    public Integer getThin() {
        return thin;
    }

    public void setThin(Integer thin) {
        this.thin = thin;
    }

    public Integer getStrength() {
        return strength;
    }

    public void setStrength(Integer strength) {
        this.strength = strength;
    }
}
