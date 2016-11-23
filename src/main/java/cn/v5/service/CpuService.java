package cn.v5.service;


import org.slf4j.Logger;
import cn.v5.util.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * 根据cpu主频与核数返回相应iPhone的计算能力
 */
@Service
public class CpuService implements InitializingBean {
    private static Logger LOG = LoggerFactory.getLogger(CpuService.class);
    private Map<Integer,Map<Integer,Integer>> cpuMaps = new HashMap<>();
    private NavigableMap<Integer,NavigableMap<Integer, Integer>> cpus = new TreeMap<>();
    public Map<Integer, Map<Integer, Integer>> getCpuMaps() {
        return cpuMaps;
    }

    public void setCpuMaps(Map<Integer, Map<Integer, Integer>> cpuMaps) {
        this.cpuMaps = cpuMaps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for(Integer key : cpuMaps.keySet()) {
            TreeMap<Integer,Integer> treeMap = new TreeMap<>();
            Map<Integer, Integer> item = cpuMaps.get(key);
            for(Integer hzkey : item.keySet()) {
                treeMap.put(hzkey, item.get(hzkey));
            }
            cpus.put(key, treeMap);
        }
        LOG.info(cpus.toString());

    }

    public Integer getCpu(int cpuNum, int hz) {
        return cpus.floorEntry(cpuNum).getValue().floorEntry(hz).getValue();
    }

}
