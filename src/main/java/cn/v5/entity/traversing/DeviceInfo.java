package cn.v5.entity.traversing;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by piguangtao on 15/3/27.
 */
@Entity(table = "traversing_device", comment = "穿越设备信息")
public class DeviceInfo {
    @Id(name = "id")
    private String deviceid;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "area")
    private String area;

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeviceInfo{");
        sb.append("deviceid='").append(deviceid).append('\'');
        sb.append(", country='").append(country).append('\'');
        sb.append(", city='").append(city).append('\'');
        sb.append(", area='").append(area).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
