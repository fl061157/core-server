package cn.v5.entity;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.Id;

/**
 * Created by hi on 14-3-12.
 */
//@Entity(table = "server_pools")
public class ServerPool {

    @Id(name = "country_code")
    private String countryCode;   //tcp //file

    @Column
    private String addr;


    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
    public ServerPool() {
    	
    }
}
