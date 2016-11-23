package cn.v5.service;

import cn.v5.bean.game.InvitationOrSharingEvent;
import cn.v5.cache.CacheService;
import cn.v5.entity.game.*;
import cn.v5.entity.game.clean.Role;
import cn.v5.entity.game.clean.UserRole;
import cn.v5.mq.EventMessageConsumer;
import cn.v5.mq.MqSubscribe;
import cn.v5.util.Constants;
import cn.v5.util.IntegerUtils;
import cn.v5.util.JsonUtil;
import cn.v5.util.LoggerFactory;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.OptionsBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static cn.v5.entity.game.GameInteraction.INTERACTIONS;
import static cn.v5.entity.game.GameInteraction.INTERACTIONS.ENSLAVE_PROP_KEY;
import static cn.v5.entity.game.GameInteraction.INTERACTIONS.ENSLAVE_PROP_VALUE;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * Created by yangwei on 14-9-19.
 */
@Service
public class GameCleanService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(GameCleanService.class);
    private static final String INVITE_FRIEND_COUNT = "invite_friends_count";
    private static final String SHARE_CLICK_COUNT = "share_click_count";
    private static final String GAME_SHARING_CLICK_PREFIX = "game_sharing_click_%s_";

    @Autowired
    @Qualifier("manager")
    private PersistenceManager manager;

    @Autowired
    private GameService gameService;

    @Autowired
    private FriendService friendService;

    @Autowired
    private EventMessageConsumer consumer;

    @Autowired
    @Qualifier("redisCacheService")
    private CacheService cacheService;

    @Override
    public void afterPropertiesSet() throws Exception {
        consumer.register(this);
    }

    @MqSubscribe(type="user.invite.success")
    public void OnSuccessInvitation(InvitationOrSharingEvent event) {
        log.debug("on success invitation received " + event);
        if (event.getSource().compareTo("3009") == 0) {
            GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(event.getFromUserId(), Integer.parseInt(event.getSource())));
            if (player == null) {
                log.debug("not found player " + event.getFromUserId() + " for " + event.getSource());
                return;
            }
            addPlayerAttrCount(player, INVITE_FRIEND_COUNT);
            manager.update(player);
        }
    }

    @MqSubscribe(type="user.invite.click")
    public void OnSharingClick(InvitationOrSharingEvent event) {
        log.debug("on sharing click received " + event);
        if (event.getSource().compareTo("3009") == 0 && StringUtils.isEmpty(event.getReceiverId())) {
            String sharingPrefix = String.format(GAME_SHARING_CLICK_PREFIX, event.getSource());
            if (isVisited(sharingPrefix + event.getClickIp())) {
                log.debug("sharing link has been visited in 10 minutes");
                return;
            }
            GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(event.getFromUserId(), Integer.parseInt(event.getSource())));
            if (player == null) {
                log.debug("not found player " + event.getFromUserId() + " for " + event.getSource());
                return;
            }
            addPlayerAttrCount(player, SHARE_CLICK_COUNT);
            manager.update(player);
        }
    }

    private boolean isVisited(String ipWithGameId) {
        String res = cacheService.get(ipWithGameId);
        if (StringUtils.isEmpty(res)) {
            cacheService.setEx(ipWithGameId, 10 * 60, "1");
            return false;
        } else {
            return true;
        }
    }

    private void addPlayerAttrCount(GamePlayer player, String attrKey) {
        Map<String, String> attr = player.getAttr();
        if (attr == null) {
            attr = Maps.newHashMap();
            player.setAttr(attr);
        }
        String invitationCount = attr.get(attrKey);
        if (StringUtils.isEmpty(invitationCount)) {
            attr.put(attrKey, Integer.toString(1));
        } else {
            Integer count = Integer.parseInt(invitationCount);
            attr.put(attrKey, Integer.toString(count + 1));
        }
    }

    public GamePlayer getCleanPlayer(String userId, Integer appId) {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(userId, appId));
        if (player == null) {
            log.debug("player {} is null for {}, insert", userId, appId);
            player = new GamePlayer(new GamePlayerKey(userId, appId), 1, 0, 0, 1, 0);
            player.setThreeStarNum(0);
            player.setLoseNum(0);
            player.setTieNum(0);
            player.setPower(0);
            player.setPowerConsumeTime(0l);
            player.setPowerRecoverTime(0L);
            manager.insert(player);
        }
        return player;
    }

    public List<GamePlayer> getAllPlayers(Integer appId, List<String> uidList) {
        Select s = select().from("game_players").where(in("uid", uidList.toArray())).and(eq("game_id", appId)).limit(Integer.MAX_VALUE);
        List<GamePlayer> list = manager.typedQuery(GamePlayer.class, s).get();
        return list;
    }

    public List<GamePlayer> getRandomPlayer(Integer appId) {
        long r = RandomUtils.nextLong(Long.MIN_VALUE, Long.MAX_VALUE);
        Select s = select().from("game_players").where(gte("token(uid)", r)).and(eq("game_id", appId)).limit(10).allowFiltering();

        List<GamePlayer> players = manager.typedQuery(GamePlayer.class, s).get();
        if (players.size() < 10) {
            log.info("Not found enough random player gt {}, get first one instead", r);
            Select selectFromStart = select().from("game_players").where(gte("token(uid)", Long.MIN_VALUE)).and(eq("game_id", appId)).limit(10 - players.size()).allowFiltering();
            players.addAll(manager.typedQuery(GamePlayer.class, selectFromStart).get());
        }
        return players;
    }

    public Map<Integer, Map<String, String>> getPlayerRoles(String userId, Integer appId) {
        Object components[] = {userId, appId};
        List<UserRole> roles = manager.sliceQuery(UserRole.class).forSelect().withPartitionComponents(components).get();
        Map<Integer, Map<String, String>> map = Maps.newHashMap();
        for (UserRole role : roles) {
            Map<String, String> attr = Maps.newHashMap();
            if (role.getAttr() != null) {
                attr.putAll(role.getAttr());
            }
            long start = System.currentTimeMillis();
            if (role.getEnd().compareTo(Long.MAX_VALUE) == 0) {
                attr.put("expiry", String.valueOf(-1));
            } else if (role.getEnd() - start > 0) {
                attr.put("expiry", String.valueOf((role.getEnd() - start) / (60L * 60L * 1000L)));
            }
            map.put(role.getKey().getRoleId(), attr);
        }
        return map;
    }

    public List<Role> getRoles(Integer appId) {
        Object components[] = {appId};
        List<Role> roles = manager.sliceQuery(Role.class).forSelect().withPartitionComponents(components).get();
        return roles;
    }

    public Map<String, Integer> updateScore(Date date, String uid, Integer appId, Integer win,
                                            Optional<Integer> exp, Optional<Integer> score, String extraData) {
        Map<String, Integer> res = Maps.newHashMap();
        GamePlayer player = gameService.getOrCreateUser(uid, appId);

        Map<String, String> attr = player.getAttr();
        if (attr == null) {
            attr = new HashMap<String, String>();
            player.setAttr(attr);
        }

        // parse extra data
        Integer curScore = null;
        Map<String, String> exAttrs = this.parseExtra(extraData);
        if (exAttrs != null) {
            /*if (exAttrs.containsKey("score")) {
                curScore = Integer.valueOf(exAttrs.get("score"));
            }*/
            attr.putAll(exAttrs);
        }

        // just update the player attr
        if (win == -1) {
            manager.insertOrUpdate(player);
            return Maps.newHashMap();
        }

        GameService.ExpResultHolder expResult = gameService.increaseExp(player, appId, win, exp);

        // victory related
        Integer cur = player.getCurStraight();
        Integer max = player.getMaxStraight();
        Long curCoins = player.getGold();
        Long preCoins = curCoins;
        if (win.equals(Constants.GAME_WIN)) {
            player.setWinNum(IntegerUtils.sum(player.getWinNum(), 1));
            cur++;
            if (score.isPresent()) {
                Preconditions.checkArgument(score.get().compareTo(0) > 0, "score " + score + "less than 0");
                curCoins += score.get();
            } else {
                curCoins += cur > 11 ? 8 : cur > 5 ? 7 : 5;
            }
        } else if (win.equals(Constants.GAME_LOSE)) {
            player.setLoseNum(IntegerUtils.sum(player.getLoseNum(), 1));
            cur = 0;
            if (score.isPresent()) {
                //Preconditions.checkArgument(score.compareTo(0) <= 0, "score " + score + "greater than 0");
                curCoins += score.get();
            } else {
                curCoins += 2;
            }
        } else {
            if (win.equals(Constants.GAME_TIE)) {
                player.setTieNum(IntegerUtils.sum(player.getTieNum(), 1));
            }
            if (score.isPresent()) {
                //Preconditions.checkArgument(score.compareTo(0) <= 0, "score " + score + "greater than 0");
                curCoins += score.get();
            } else {
                curCoins += 3;
            }
        }
        attr.put("cur_straight", Integer.toString(cur));
        gameService.updateCoins(player, appId, curCoins);
        if (cur > max) {
            max = cur;
            attr.put("max_straight", Integer.toString(max));
        }
        manager.insertOrUpdate(player);

        // game board related
        // update game board with win num by default
        if (curScore == null) {
            curScore = player.getWinNum();
        }
        if (win.equals(Constants.GAME_WIN)) {
            gameService.saveScore(player, appId, curScore, date);
        }

        res.put("max_straight", max);
        res.put("level", player.getLevel());
        res.put("next_level_needed", expResult.nextExp);
        res.put("next_level_experience", expResult.totalExp);
        res.put("cur_coins", curCoins.intValue());
        return res;
    }

    public Map<Integer, Map<String, String>> buy(String userId, Integer appId, Integer roleId, Optional<Long> coins, String extraData) {
        Map<Integer, Map<String, String>> res = Maps.newHashMap();
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(userId, appId));
        Role role = manager.find(Role.class, new GameRoleKey(appId, roleId));
        Preconditions.checkState(role != null, "user " + userId + " buy role " + roleId + " failed");

        // price priority: coins > upgrade price > role price
        long price = role.getPrice();
        if (coins.isPresent()) {
            price = coins.get().longValue();
        }
        if (player.getGold() < price) {
            log.warn("user {}({} gold) short to buy role {}.{} for {}", userId, player.getGold(),
                    roleId, price, appId);
            res.put(-1, ImmutableMap.of("error_code", "-1"));
            return res;
        }

        UserRole oriUserRole = manager.find(UserRole.class, new UserRole.PlayerRoleKey(userId, appId, roleId));
        if (oriUserRole != null && extraData == null) {
            log.info("user {}({} gold) has role {}.{} for {} already", userId, player.getGold(),
                    roleId, price, appId);
            // not the case of upgrading roles
            res.put(-2, ImmutableMap.of("error_code", "-2"));
            return res;
        }
        Map<Integer, Long> pro = role.getProbability();
        Long end;
        // hour
        Long expiry;
        Long start = System.currentTimeMillis();
        if (pro == null) {
            end = Long.MAX_VALUE;
            expiry = -1L;
        } else {
            int p = RandomUtils.nextInt(0, 100);
            NavigableMap<Integer, Long> nm = new TreeMap(pro);
            expiry = nm.higherEntry(p).getValue();
            end = expiry == -1 ? Long.MAX_VALUE : start + expiry * 60L * 60L * 1000L;
        }
        log.info("user {}({} gold) buy role {}.{} expiry {} for {}", userId, player.getGold(),
                roleId, price, expiry, appId);
        UserRole userRole = oriUserRole;
        if (userRole == null) {
            userRole = new UserRole(new UserRole.PlayerRoleKey(userId, appId, roleId));
        }
        userRole.setStart(start);
        userRole.setEnd(end);
        Map<String, String> exAttrs = parseExtra(extraData);
        if (exAttrs != null) {
            Map<String, String> attr = userRole.getAttr();
            if (attr == null) {
                userRole.setAttr(exAttrs);
            } else {
                attr.putAll(exAttrs);
            }
        }
        manager.insertOrUpdate(userRole);

        if (player.getAttr() == null) {
            player.setAttr(ImmutableMap.of("gold", Long.toString(player.getGold() - price)));
        } else {
            player.getAttr().put("gold", Long.toString(player.getGold() - price));
        }
        manager.update(player);
        res.put(roleId, ImmutableMap.of("expiry", String.valueOf(expiry)));
        res.put(-100, ImmutableMap.of("gold", String.valueOf(player.getGold())));
        return res;
    }

    private Map<String, String> parseExtra(String extraData) {
        Map<String, String> exAttrs = null;
        if (extraData != null) {
            try {
                exAttrs = JsonUtil.fromJson(extraData, Map.class);
            } catch (IOException e) {
                log.error("extraData {} not json map", extraData);
            }
        }
        return exAttrs;
    }

    public List<GameInteraction> getSlaves(Integer gameId, String uid) throws Exception {
        Objects.requireNonNull(gameId);
        Objects.requireNonNull(uid);

        Iterator<GameInteraction> it = manager.sliceQuery(GameInteraction.class).forIteration().withPartitionComponents(uid, gameId).iterator();
        List<GameInteraction> slaves = Lists.newArrayList(it);
        log.info("user[{}] has {} enslave relations, gameId: {}, friend: {}", uid, slaves.size(), gameId);
        return slaves;
    }

    public void enslave(String master, String slave, Integer gameId) throws Exception {
        Objects.requireNonNull(master);
        Objects.requireNonNull(slave);
        Objects.requireNonNull(gameId);

        boolean isFriend = friendService.isFriendOfUser(master, gameId, slave);

        log.info("user[{}] enslaves user[{}], gameId: {}, friend: {}", master, slave, gameId, isFriend);

        GameInteraction masterInteraction = createEnslaveInteraction(master, gameId, slave, true);
        GameInteraction slaveInteraction = createEnslaveInteraction(slave, gameId, master, false);
        if (isFriend) {
            saveGameInteractionWithTTL(masterInteraction, Integer.MAX_VALUE);
            saveGameInteractionWithTTL(slaveInteraction, Integer.MAX_VALUE);
        } else {
            saveGameInteractionWithTTL(masterInteraction, 24);
            saveGameInteractionWithTTL(slaveInteraction, 24);
        }
    }

    private GameInteraction createEnslaveInteraction(String uid, Integer gameId, String target, boolean isMaster) {
        GameInteraction interaction = new GameInteraction();
        GameInteractionKey key = new GameInteractionKey(uid, gameId, target);
        interaction.setKey(key);

        Map<String, Map<String, String>> interactions = new HashMap<>();
        Map<String, String> enslaveProperties = new HashMap<>();
        enslaveProperties.put(ENSLAVE_PROP_KEY.BEGIN, String.valueOf(System.currentTimeMillis()));
        enslaveProperties.put(ENSLAVE_PROP_KEY.ROLE, isMaster ? ENSLAVE_PROP_VALUE.MASTER : ENSLAVE_PROP_VALUE.SLAVE);
        interactions.put(INTERACTIONS.ENSLAVE, enslaveProperties);
        interaction.setInteractions(interactions);
        return interaction;
    }

    private void saveGameInteractionWithTTL(GameInteraction interaction, int ttl) {
        if (ttl == Integer.MAX_VALUE) {
            interaction.getInteractions().get(INTERACTIONS.ENSLAVE).put(ENSLAVE_PROP_KEY.DURATION, ENSLAVE_PROP_VALUE.INFINITE);
            manager.insert(interaction);
        } else {
            interaction.getInteractions().get(INTERACTIONS.ENSLAVE).put(ENSLAVE_PROP_KEY.DURATION, String.valueOf(ttl));
            int ttlInSeconds = ttl * 60 * 60;
            manager.insert(interaction, OptionsBuilder.withTtl(ttlInSeconds > 0 ? ttlInSeconds : Integer.MAX_VALUE));
        }
    }

    public boolean interact(Integer gameId, String uid, String target) throws Exception {
        Objects.requireNonNull(gameId);
        Objects.requireNonNull(uid);
        Objects.requireNonNull(target);

        GameInteraction interaction = manager.find(GameInteraction.class, new GameInteractionKey(uid, gameId, target));

        // interaction may expire
        if (interaction == null) {
            log.debug("user[{}] fails to interact with user[{}], gameId: {} (relation expired)", uid, target, gameId);
            return false;
        }

        Map<String, String> enslave = interaction.getInteractions().get(INTERACTIONS.ENSLAVE);

        boolean isMaster = enslave.get(ENSLAVE_PROP_KEY.ROLE).equals(ENSLAVE_PROP_VALUE.MASTER);

        GameInteraction partnerInteraction = manager.find(GameInteraction.class, new GameInteractionKey(target, gameId, uid));
        if (partnerInteraction == null) {
            log.debug("user[{}] fails to interact with user[{}], gameId: {} (partner expired)", uid, target, gameId);
        }
        if (isMaster) {
            return saveFlirtAndCuteInteraction(interaction, partnerInteraction, true);
        } else {
            return saveFlirtAndCuteInteraction(interaction, partnerInteraction, false);
        }
    }

    private boolean saveFlirtAndCuteInteraction(GameInteraction interaction, GameInteraction partnerInteraction, boolean isFlirt) {
        Map<String, Map<String, String>> interactions = interaction.getInteractions();
        interactions.put(isFlirt ? INTERACTIONS.FLIRT : INTERACTIONS.CUTE, null);
        interaction.setInteractions(interactions);
        if (partnerInteraction.getInteractions().get(isFlirt ? INTERACTIONS.CUTE : INTERACTIONS.FLIRT) != null) {
            increaseTTL(interaction);
            increaseTTL(partnerInteraction);
            log.info("slave duration extended between user[{}] and user[{}], gameId: {}", interaction.getKey().getUid(), partnerInteraction.getKey().getUid(), interaction.getKey().getGameId());
            return true;
        } else {
            manager.update(interaction);
            return false;
        }
    }

    private static final int ttl_increment = 24; //hours

    private void increaseTTL(GameInteraction interaction) {
        Map<String, Map<String, String>> interactions = interaction.getInteractions();
        Map<String, String> enslave = interactions.get(INTERACTIONS.ENSLAVE);
        String durationProp = enslave.get(ENSLAVE_PROP_KEY.DURATION);
        if (durationProp.equals(ENSLAVE_PROP_VALUE.INFINITE)) {
            return;
        }
        int duration = Integer.parseInt(durationProp);
        long begin = Long.parseLong(enslave.get(ENSLAVE_PROP_KEY.BEGIN));

        int newTTL = (int) (duration - (System.currentTimeMillis() - begin) + ttl_increment * 60 * 60);
        enslave.put(ENSLAVE_PROP_KEY.DURATION, String.valueOf(duration + ttl_increment));
        interactions.put(INTERACTIONS.ENSLAVE, enslave);
        interaction.setInteractions(interactions);
        manager.insertOrUpdate(interaction, OptionsBuilder.withTtl(newTTL > 0 ? newTTL : Integer.MAX_VALUE));

    }

    public void trade(Integer gameId, String uid, String target) throws Exception {
        Objects.requireNonNull(gameId);
        Objects.requireNonNull(uid);
        Objects.requireNonNull(target);

        log.info("slave relation between user[{}] an user[{}] has been lifted, gameId: {}", uid, target, gameId);
        manager.deleteById(GameInteraction.class, new GameInteractionKey(uid, gameId, target));
        manager.deleteById(GameInteraction.class, new GameInteractionKey(target, gameId, uid));
    }

}
