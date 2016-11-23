package cn.v5.service;
import cn.v5.code.SystemConstants;
import cn.v5.entity.Conversation;
import cn.v5.entity.ConversationKey;
import cn.v5.entity.CurrentUser;
import cn.v5.util.JsonUtil;
import cn.v5.util.LoggerFactory;
import cn.v5.util.ReqMetricUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户会话Service
 */
@Service
public class ConversationService {
    private static final Logger log = LoggerFactory.getLogger(ConversationService.class);
    public static final int MAX_VALUE = 0x7fffffff;

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    @Qualifier("opManager")
    private PersistenceManager opManager;

    @Inject
    private ObjectMapper mapper;

    @Autowired
    private ReqMetricUtil reqMetricUtil;


    @Inject
    private MessageQueueService messageQueueService;

    public Map<String, Conversation> findByUserId(String userId) {
        List<Conversation> conversations = manager.sliceQuery(Conversation.class).forSelect().withPartitionComponents(userId).get(MAX_VALUE);
        return Maps.uniqueIndex(conversations, new Function<Conversation, String>() {
            @Override
            public String apply(Conversation input) {
                return input.getId().getEntityId();
            }
        });
    }


    public List<Conversation> findConversationByUserId(String userId) {
        return manager.sliceQuery(Conversation.class).forSelect().withPartitionComponents(userId).get(MAX_VALUE);
    }

    public Map<String, Integer> getConversations(String userId, Integer appId) {
        String tmp = getConversationByUId(userId);
        Map<String, Integer> conversations = Maps.newHashMap();
        try {
            conversations = JsonUtil.fromJson(tmp, Map.class);
        } catch (IOException e) {
            log.error("get user {} appId {} conversation maps errors {}", userId, appId, tmp);
        }
        return conversations;
    }

    public String getConversationByUId(String userId) {
        String result = "";
        Map map = new HashMap();

        List<Conversation> list = findConversationByUserId(userId);
        if (!list.isEmpty()) {
            for (Conversation conversation : list) {
                map.put(conversation.getId().getEntityId(), conversation.getType());
            }
            try {
                result = mapper.writeValueAsString(map);
            } catch (IOException e) {
                log.error(String.format("fails to parse user conversation. userId:%s", userId), e);
            }

        }
        return result;

    }


    public Conversation findConversationByEntityId(String userId, String entityId, int appId) {
        PersistenceManager realManager = getRealManager(appId);
        return realManager.find(Conversation.class, new ConversationKey(userId, entityId));
    }
    public Conversation findConversationByEntityId(String userId, String entityId) {
        return findConversationByEntityId(userId,entityId,0);
    }

    public void save(String userId, String entityId, int type, Integer appId) {
        PersistenceManager realManager = getRealManager(appId);

        Conversation c = new Conversation();
        c.setId(new ConversationKey(userId, entityId));
        c.setType(type);
        c.setCreateTime(System.currentTimeMillis());
        realManager.insert(c);

    }

    public void add(String userId, String entityId, Integer type, Integer appId) {
        Conversation conversation = findConversationByEntityId(userId, entityId, appId);
        int TYPE_FLAG = 0x02;

        if (!(appId > SystemConstants.CG_APP_ID_MAX)) {
            if (((type & TYPE_FLAG) != 0) && ((conversation == null) || (conversation.getType() & TYPE_FLAG) == 0)) {
                Map<String, Object> data = new HashMap<>();
                data.put("gameId", appId);
                data.put("blackRequestUser", userId);
                data.put("blackedUser", entityId);
                messageQueueService.sendGameMatchMsg("request_black", data);
            }
        }

        if (conversation != null) {
            type = conversation.getType() | type;
        }
        save(userId, entityId, type, appId);
    }

    public void remove(String userId, String entityId, Integer type, Integer appId) {

        Conversation conversation = findConversationByEntityId(userId, entityId, appId);
        if (conversation != null) {
            int TYPE_FLAG = 0x02;
            if (!(appId > SystemConstants.CG_APP_ID_MAX)) {
                if (((type & TYPE_FLAG) != 0) && ((conversation.getType() & TYPE_FLAG) == TYPE_FLAG)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("gameId", appId);
                    data.put("RemoveblackRequestUser", userId);
                    data.put("RemovedblackedUser", entityId);
                    messageQueueService.sendGameMatchMsg("remove_black", data);
                }
            }

            int result = conversation.getType() & (~type);
            if (result == 0) {
                manager.deleteById(Conversation.class, conversation.getId());
            } else {
                save(userId, entityId, result, appId);
            }
        }
    }


    public void delete(String userId, String entityId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(entityId)) {
            return;
        }
        ConversationKey key = new ConversationKey(userId, entityId);
        CurrentUser.db().deleteById(Conversation.class, key);
    }

    public void saveOrUpdateConversation(Conversation conversation) {
        CurrentUser.db().insertOrUpdate(conversation);
    }

    public PersistenceManager getRealManager(int appId) {
        return appId > SystemConstants.CG_APP_ID_MAX ? opManager : manager;
    }

}
