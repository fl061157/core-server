package cn.v5.service;

import cn.v5.cache.CacheService;
import cn.v5.code.StatusCode;
import cn.v5.entity.CurrentUser;
import cn.v5.entity.GroupMember;
import cn.v5.entity.GroupNumberMemberIndex;
import cn.v5.web.controller.ServerException;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Created by piguangtao on 15/11/12.
 * 生成群组序号
 * 如果群组序号已经被占满(不考虑中间空余场景，需要考虑提供生成序号的专门服务)
 */
@Service
public class GroupSeqService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupSeqService.class);

    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Value("${local.idc.region}")
    private String localIdc;

    @Value(("${local.idc.group.member.seq.init.value}"))
    private int localInitValue;

    @Value(("${local.idc.group.member.seq.max.value}"))
    private int localMaxValue;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    private GroupService groupService;

    private static final String GROUP_MEMEBER_NUMBER_PREFIX = "GROUP_MEMEBER_NUMBER";


    public Integer getGroupMemberNextNumber(String groupId) {
        Integer nextValue = getGroupMemberNextNumberOnce(groupId);

        if (null != nextValue) {
            boolean success = false;
            for (int i = 0; i < 3 && !success; i++) {
                //防止并发操作 需要进行碰撞检测
                GroupNumberMemberIndex groupNumberMemberIndex = new GroupNumberMemberIndex();
                GroupNumberMemberIndex.GroupNumberMemberIndexKey key = new GroupNumberMemberIndex.GroupNumberMemberIndexKey();
                key.setGroupId(groupId);
                key.setNumber(nextValue);
                groupNumberMemberIndex.setKey(key);
                groupNumberMemberIndex.setTimestamp(System.currentTimeMillis());
                //本数据中心操作
                try {
                    CurrentUser.db().insert(groupNumberMemberIndex, OptionsBuilder.ifNotExists().lwtLocalSerial());
                    success = true;
                } catch (Exception e) {
                    //碰撞检测 如果继续执行
                    //执行几次
                    success = false;
                    nextValue = getGroupMemberNextNumberOnce(groupId);
                }
            }
            if (!success) {
                throw new ServerException(StatusCode.INNER_ERROR, "fails to get group member number");
            }
        }

        if (null == nextValue) {
            throw new ServerException(StatusCode.INNER_ERROR, "fails to get group member number");
        }
        return nextValue;
    }

    protected Integer getGroupMemberNextNumberOnce(String groupId) {
        Integer result = null;
        Long nextValue = null;

        try {
            nextValue = cacheService.incBy(getGroupMemberValuePrefix(groupId, localIdc), 1);
        } catch (Exception e) {
            LOGGER.error(String.format("fails to get group member number from cache. groupId:%s", groupId), e);
        }

        //访问redis异常或者redis还没有设置初始值 则从数据库中获取
        if (null == nextValue || nextValue <= 1) {
            Long currentMaxValue = getGroupMemberMaxNumberFromDb(groupId, localIdc);
            //数据库还没有设置该值 则需要设置初始值
            if (null == currentMaxValue) {
                nextValue = Long.valueOf(localInitValue);
            } else {
                nextValue = currentMaxValue + 1;
            }

            //向redis设置初始值
            try {
                cacheService.set(getGroupMemberValuePrefix(groupId, localIdc), String.valueOf(nextValue));
            } catch (Exception e) {
                //ignore
            }
        }

        if (null != nextValue) {
            if (nextValue > localMaxValue) {
                result = null;
            } else {
                result = nextValue.intValue();
            }
        }
        return result;
    }

    public boolean setGroupInitNumber(String groupId) {
        boolean result = true;
        try {
            cacheService.set(getGroupMemberValuePrefix(groupId, localIdc), String.valueOf(localInitValue));
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * 根据群组成员 获取其成员的值 然后遍历最大值
     * 正常情况下 访问redis 不会使用db遍历
     *
     * @param groupId
     * @param region
     * @return
     */
    protected Long getGroupMemberMaxNumberFromDb(String groupId, String region) {
        Long result = null;
        //获取群组中的所有成员，依次遍历群组
        List<GroupMember> groupMembers = groupService.findMembersByGroupId(groupId);
        if (null != groupId && groupMembers.size() > 0) {
            Optional<GroupMember> maxGroupMember = groupMembers.stream().filter(groupMember -> region.equalsIgnoreCase(groupMember.getIdc())).max((o1, o2) -> o1.getSeq() - o2.getSeq());
            //该区域下还没有群组用户
            if (null != maxGroupMember && maxGroupMember.isPresent()) {
                result = Long.valueOf(maxGroupMember.get().getSeq());
            }
        }
        return result;
    }

    protected String getGroupMemberValuePrefix(String groupId, String region) {
        return String.format("%s_%s_%s", GROUP_MEMEBER_NUMBER_PREFIX, groupId, region);
    }
}
