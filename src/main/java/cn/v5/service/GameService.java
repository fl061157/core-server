package cn.v5.service;

import cn.v5.bean.game.UserLoginLogoutGameEvent;
import cn.v5.entity.Friend;
import cn.v5.entity.User;
import cn.v5.entity.game.*;
import cn.v5.entity.game.billboard.GameBoard;
import cn.v5.entity.game.billboard.GameScore;
import cn.v5.entity.game.billboard.GameScoreUid;
import cn.v5.entity.game.level.GameLevel;
import cn.v5.entity.vo.game.GameVo;
import cn.v5.util.Constants;
import cn.v5.util.LoggerFactory;
import cn.v5.util.TimeUtils;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import info.archinnov.achilles.persistence.PersistenceManager;
import info.archinnov.achilles.type.CounterBuilder;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-10 下午1:48
 */
@Service
public class GameService implements InitializingBean {
    private static int MAX_VALUE = 10000;
    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    @Value("${game.static.url}")
    private String staticUrl;

    @Value("${game.song.power}")
    private int songPower;

    @Inject
    private TaskService taskService;

    @Autowired
    private MessageQueueService queueService;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.assembleUrlsTransformer = new Function<String, String>() {
            @Override
            public String apply(String input) {
                return staticUrl + "/" + input;
            }
        };
    }

    private final static Splitter COMMA_SPLITER = Splitter.on(',').trimResults().omitEmptyStrings();


    private final static Select selectGames = select().from("game_info");

    private Select selectInStmt(List uidList, Object app_id, Object date) {
        return select().from("game_score").where(in("uid", uidList.toArray())).and(eq("app_id", app_id)).and(eq("date", date)).limit(MAX_VALUE);
    }

    private Select selectAscStmt(Object date, Object appId) {
        return select().from("game_score_uid").where(eq("date", date)).and(eq("app_id", appId)).orderBy(asc("score"));
    }

    private Function<String, String> assembleUrlsTransformer;
    private final static Function<Friend, String> friendsIdTransformer = new Function<Friend, String>() {
        @Override
        public String apply(Friend input) {
            return input.getId().getFriendId();
        }
    };
    private final static Function<GameScoreUid, String> scoresIdTransformer =
            new Function<GameScoreUid, String>() {
                @Override
                public String apply(GameScoreUid input) {
                    return input.getKey().getUid();
                }
            };
    private final static Function<GameScoreUid, GameScore> idsScoreTransformer =
            new Function<GameScoreUid, GameScore>() {
                @Override
                public GameScore apply(GameScoreUid input) {
                    return new GameScore(
                            new GameScore.GameSongScoreKey(input.getKey().getUid(), input.getKey().getAppId(), input.getKey().getDate()),
                            input.getKey().getScore());
                }
            };
    private final static Predicate<Integer> blackFilter = new Predicate<Integer>() {
        @Override
        public boolean apply(Integer input) {
            if (input != null && input.intValue() == 2) {
                return true;
            }
            return false;
        }
    };


    @Inject
    private PersistenceManager manager;

    @Inject
    private UserService userService;

    @Inject
    private ConversationService conversationService;

    public Game getGame(Game.GameKey id) {
        return manager.find(Game.class, id);
    }

    public List<Game> getGames() {
        return manager.typedQuery(Game.class, selectGames).get();
    }

    public String getMatchedLangVersionName(String fieldValue, String countryCode) {
        String newValue = fieldValue;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> map = objectMapper.readValue(newValue, Map.class);
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (entry.getKey().trim().equals(countryCode)) {
                    newValue = entry.getValue();
                    break;
                }
            }

            if (newValue.equals(fieldValue) && StringUtils.isNotEmpty(map.get("default"))) {
                newValue = map.get("default");
            }
        } catch (Exception e) {
        }
        return newValue;
    }

    public GameVo fromGame(Game game, String countryCode) {
        Iterable<String> urls = getGameUrls(game.getGameImagesUrl());
        //List<String> gameImagesUrls = Lists.newArrayList();
        //gameImagesUrls.addAll(FluentIterable.from(urls).transform(assembleUrlsTransformer).toList());
        List<String> gameImagesUrls = ImmutableList.copyOf(urls);

        String avatarUrl = game.getAvatarUrl();
        String descImageUrl = game.getDescImageUrl();
        String descVideoUrl = game.getDescVideoUrl();
        String downloadUrl = game.getDownloadUrl();
        /*
        if (avatarUrl != null) {
            Iterable<String> dlUrls = Iterables.transform(getGameUrls(avatarUrl), assembleUrlsTransformer);
            avatarUrl = Joiner.on(',').skipNulls().join(dlUrls);
        }
        if (descImageUrl != null) {
            Iterable<String> bgUrls = Iterables.transform(getGameUrls(descImageUrl), assembleUrlsTransformer);
            descImageUrl = Joiner.on(',').skipNulls().join(bgUrls);
        }
        if (descVideoUrl != null) {
            Iterable<String> bgUrls = Iterables.transform(getGameUrls(descVideoUrl), assembleUrlsTransformer);
            descVideoUrl = Joiner.on(',').skipNulls().join(bgUrls);
        }
        if (downloadUrl != null) {
            Iterable<String> dlUrls = Iterables.transform(getGameUrls(downloadUrl), assembleUrlsTransformer);
            downloadUrl = Joiner.on(',').skipNulls().join(dlUrls);
        }
        */


        GameVo gv = new GameVo(game.getClassification(), getMatchedLangVersionName(game.getName(), countryCode), getMatchedLangVersionName(game.getDescription(), countryCode), avatarUrl,
                game.getId().getId(), game.getVersion(), game.getBbsComments(),
                descImageUrl, descVideoUrl, gameImagesUrls,
                game.getVideoRes(), downloadUrl, new ArrayList<String>(), game.getMediaType());
        Map<String, String> incUpdate = game.getIncUpdate();
        if (incUpdate != null && !incUpdate.isEmpty()) {
            String incVer = incUpdate.keySet().iterator().next();
            String incUrl = incUpdate.get(incVer);
            gv.setMinVer(incVer);
            gv.setUpdateUrl(incUrl);
        }
        return gv;
    }

    public void saveGame(Game game) {
        manager.insert(game);
    }

    public List<String> getGameUrls(List<String> urls) {
        if (urls == null) {
            urls = Lists.newArrayList();
        } else {
            urls = manager.removeProxy(urls);
        }
        return urls;
    }

    public Iterable<String> getGameUrls(String urls) {
        return COMMA_SPLITER.split(urls);
    }

    public List<GameOnlineCount> getGameOnlineNum(List<Integer> gameIds) {
        return manager.typedQuery(GameOnlineCount.class, select().from("game_online_counter").where(in("id", gameIds.toArray(new Integer[gameIds.size()])))).get();
    }

    public void addGameOnlineNum(Integer gameId, Integer num, boolean addPlayerNum) {
        GameOnlineCount onlineCount = new GameOnlineCount();
        onlineCount.setId(gameId);
        if (num > 0) {
            if (addPlayerNum) {
                onlineCount.setPlayerNum(CounterBuilder.incr(num));
            } else {
                onlineCount.setCounter(CounterBuilder.incr(num));
            }
        } else {
            if (addPlayerNum) {
                onlineCount.setPlayerNum(CounterBuilder.decr(-num));
            } else {
                onlineCount.setCounter(CounterBuilder.decr(-num));
            }
        }
        manager.insert(onlineCount);
    }

    public List<GameScore> getSongBoard(User you, Integer appId) {
        Date today = TimeUtils.getTodayStart();
        return getSongBoard(you, appId, today);
    }

    public List<GameScore> getSongBoard(User you, Integer appId, Date today) {
        String uid = you.getId();
        GameBoard gameBoard = manager.find(GameBoard.class, new GameBoard.GameSongBoardKey(uid, appId, today));
        if (gameBoard != null) {
            return baseSongBorad(gameBoard, uid, appId);
        }
        /*
        List<Friend> friends = manager.typedQuery(Friend.class,
                "select * FROM friends where user_id = ? and app_id = ? and resource_app_id=?", you.getId(), 0, ConfigUtils.getInt("game.song.id")).get();
        */
        Object components[] = {you.getId(), 0};
        List<Friend> friends = manager.sliceQuery(Friend.class).forSelect().withPartitionComponents(components).get();
        Map<String, Integer> conversations = conversationService.getConversations(you.getId(), 0);
        conversations = userService.addRobotInConversations(conversations);

        userService.excludeBlackFriends(friends, conversations);
        //if(!CollectionUtils.isEmpty(friends)) {
        //if (friends.size() >= 10) {
        return baseAllFriends(you, appId, friends, today);
        //} else {
        //    return basePartFriends(you.getId(), appId, friends, conversations, today);
        //}
        //}
        /*
        else { // 没有好友
            return baseSelf(you.getId(), appId);
        }
        */
    }

    /*
     * complete the absent persons with default game scores
     */
    @VisibleForTesting
    public List<GameScore> fillGameScores(List<GameScore> songScores, List<String> allIds, Integer appId, Date date) {
        //assert(songScores.size() == ids.size());
        log.trace("songScores: {} and allIds: {}", songScores, allIds);
        List<GameScore> result = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < allIds.size(); i++) {
            if (j >= songScores.size()) {
                result.add(i, new GameScore(new GameScore.GameSongScoreKey(allIds.get(i), appId, date)));
            } else if (songScores.get(j).getKey().getUid().compareTo(allIds.get(i)) != 0) {
                result.add(i, new GameScore(new GameScore.GameSongScoreKey(allIds.get(i), appId, date)));
            } else {
                result.add(songScores.get(j));
                j++;
            }
        }
        return result;
    }

    /**
     * 榜单已经生成好，直接返回
     *
     * @param gameBoard
     * @return
     */
    private List<GameScore> baseSongBorad(GameBoard gameBoard, String uid, Integer appId) {
        String members = gameBoard.getMembers();
        log.debug("get song board from GameScore, members is {}", members);
        Date date = gameBoard.getKey().getDate();
        String[] ids = StringUtils.split(members, ",");
        List<String> idList = Lists.newArrayList();
        idList.addAll(Arrays.asList(ids));
        Set<String> baseIds = ImmutableSet.copyOf(ids);
        // 看今天有没有加好友，如果有，将加的好友加入排行榜
        boolean change = false;
        String newMemberIds = members + ",";
        List<Friend> friends = manager.typedQuery(Friend.class,
                select().from("friends").where(eq("user_id", uid)).and(eq("app_id", 0))).get();
        for (Friend friend : friends) {
            String fid = friend.getId().getFriendId();
            if (!baseIds.contains(fid)) {
                idList.add(fid); // 将新增的好友加入榜
                newMemberIds += fid + ",";
                change = true;
            }
        }
        if (change) {  // 排行榜发生变化 重新保存
            log.debug("has new friends, update user {} game socre board members", uid);
            gameBoard.setMembers(StringUtils.substringBeforeLast(newMemberIds, ","));
            manager.update(gameBoard);
        }

        List<GameScore> list = manager.typedQuery(GameScore.class, selectInStmt(idList, appId, date)).get();
        List<GameScore> result = fillGameScores(list, idList, appId, date);
        Collections.sort(result, new GameScore.ScoreComparator());
        return result;
    }

    /**
     * 没有好友，就以自身为基准补充
     *
     * @param uid
     */
    private List<GameScore> baseSelf(String uid, Integer appId) {
        Date yesterday = TimeUtils.getYesterdayStart();
        Date today = TimeUtils.getTodayStart();
        List<GameScore> list = Lists.newArrayList();
        GameScore yourSongScore = manager.find(GameScore.class, new GameScore.GameSongScoreKey(uid, appId, today));
        if (null == yourSongScore) {
            yourSongScore = new GameScore(new GameScore.GameSongScoreKey(uid, appId, today), -1);
            //manager.persist(yourSongScore);
            list.add(yourSongScore);
        } else {
            list.add(yourSongScore);
        }

        String members = uid + ",";
        Set<String> filter = ImmutableSet.of(uid);  // 需要排除的id
        List<GameScoreUid> gameScoreUids = manager.typedQuery(GameScoreUid.class, selectAscStmt(today, appId)).get();
        gameScoreUids = removeExculde(gameScoreUids, filter);  // 去除已经添加的好友成绩
        List<GameScoreUid> rest = Lists.newArrayList();
        int needSize = 30 - list.size();  // 需要补位的个数
        int poolSize = gameScoreUids.size(); // 池子大小
        int min = Math.min(needSize, poolSize);
        int yScore = yourSongScore.getScore();
        GameScoreUid yScoreUid =
                new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, yScore, uid));
        int index = Collections.binarySearch(gameScoreUids, yScoreUid,
                new GameScoreUid.ScoreComparator());
        int insert = index;
        if (insert < 0) {
            insert = -(index + 1);
        }
        /* not enough players */
        if (min == poolSize) {
            log.debug(String.format("Only %d players", poolSize));
            rest.addAll(gameScoreUids);
        } else if (insert == 0 || insert == gameScoreUids.size()) {
            log.debug("your socre " + yScore + " is out of others");
            for (int i = 0; i < min; i++) {
                int j = RandomUtils.nextInt(gameScoreUids.size());
                GameScoreUid gameScoreUid = gameScoreUids.get(j);
                rest.add(gameScoreUid);
                gameScoreUids.remove(index); // 删除
            }
        } else {
            int low = insert;
            int high = poolSize - insert;
            int range = Math.min(Math.min(low, high), min / 2);
            log.debug(String.format("insert point %d, min %d, low %d, high %d, range %d",
                    insert, min, low, high, range));
            rest.addAll(gameScoreUids.subList(insert - range, insert + range));
            int pad = min - 2 * range;
            if (insert + range >= poolSize) {
                rest.addAll(gameScoreUids.subList(insert - range - pad, insert - range));
            } else {
                rest.addAll(gameScoreUids.subList(insert + range, insert + range + pad));
            }
        }

        members = members + "," +
                StringUtils.join(FluentIterable.from(rest).transform(scoresIdTransformer).filter(input -> input != null).toList(), ",");
        list.addAll(FluentIterable.from(rest).transform(idsScoreTransformer).filter(input -> input != null).toList());
        if (list.size() >= 30) {
            GameBoard gameBoard = new GameBoard(new GameBoard.GameSongBoardKey(uid, appId, today), members);
            manager.insert(gameBoard); // 保存生成的榜单，下次直接获取
        }
        Collections.sort(list, new GameScore.ScoreComparator());
        return list;
    }

    private List<GameScoreUid> removeExculde(List<GameScoreUid> gameScoreUids, Set<String> excludeIds) {
        List<GameScoreUid> filteredScoreUids = new ArrayList<>();
        for (int i = gameScoreUids.size() - 1; i >= 0; i--) {
            GameScoreUid gameScoreUid = gameScoreUids.get(i);
            if (!excludeIds.contains(gameScoreUid.getKey().getUid())) {
                filteredScoreUids.add(gameScoreUids.get(i));
            }
        }
        return filteredScoreUids;
    }

    private List<GameScore> basePartFriends(String uid, Integer appId, List<Friend> friends,
                                            Map<String, Integer> conversations, Date today) {
        log.debug("get song board from lowest score friend");
        Date yesterday = TimeUtils.getYesterdayStart();

        log.debug("add my friends first");
        Integer lowestScore = 0;
        String lowestFriend = "";
        List<String> friendUids = Lists.newArrayList();

        if (null == friends) {
            friends = new ArrayList<>();
        }
        // add my self
        friendUids.add(uid);
        friendUids.addAll(FluentIterable.from(friends).transform(friendsIdTransformer).filter(input -> input != null).toList());
        List<GameScore> beforeScores = manager.typedQuery(GameScore.class, selectInStmt(
                friendUids, appId, yesterday)).get();
        for (int i = 0; i < beforeScores.size(); i++) {
            GameScore score = beforeScores.get(i);
            if (score != null && score.getScore() > 0 && (lowestScore == 0 ||
                    lowestScore > score.getScore())) {
                lowestScore = score.getScore();
                lowestFriend = friendUids.get(i);
            }
        }
        List<GameScore> list = Lists.newArrayList();
        List<GameScore> tmp = manager.typedQuery(GameScore.class, selectInStmt(
                friendUids, appId, today)).get();
        String members = StringUtils.join(friendUids, ",");
        Set<String> filter = ImmutableSet.copyOf(friendUids);  // 需要排除的id
        list.addAll(fillGameScores(tmp, friendUids, appId, today));

        log.debug("after add friends list size is {}, friends {}", list.size(), friendUids);
        List<GameScoreUid> gameScoreUids = manager.typedQuery(GameScoreUid.class, selectAscStmt(today, appId)).get();
        gameScoreUids = removeExculde(gameScoreUids, filter);  // 去除已经添加的好友成绩
        Map<String, Integer> blackCon = Maps.filterValues(conversations, blackFilter);
        gameScoreUids = removeExculde(gameScoreUids, blackCon.keySet());
        List<GameScoreUid> rest = Lists.newArrayList();
        int needSize = 30 - list.size();  // 需要补位的个数
        int poolSize = gameScoreUids.size(); // 池子大小
        int min = Math.min(needSize, poolSize);
        if (min == poolSize) { // not enough players
            log.debug(String.format("Only %d players", poolSize));
            rest.addAll(gameScoreUids);
        } else if ("" == lowestFriend) {
            log.debug("nobody played yesterday");
            for (int i = 0; i < min; i++) {
                int index = RandomUtils.nextInt(gameScoreUids.size());
                GameScoreUid gameScoreUid = gameScoreUids.get(index);
                rest.add(gameScoreUid);
                gameScoreUids.remove(index); // 删除
            }
        } else {
            GameScoreUid yScoreUid =
                    new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(yesterday, appId, lowestScore, lowestFriend));
            int index = Collections.binarySearch(gameScoreUids, yScoreUid,
                    new GameScoreUid.ScoreComparator());
            int insert = index;
            if (insert < 0) {
                insert = -(index + 1);
            }

            int low = insert;
            int high = poolSize - insert;
            int range = Math.min(Math.min(low, high), min / 2);
            log.debug(String.format("insert point %d, min %d, low %d, high %d, range %d",
                    insert, min, low, high, range));
            rest.addAll(gameScoreUids.subList(insert - range, insert + range));
            int pad = min - 2 * range;
            if (insert + range >= poolSize) {
                rest.addAll(gameScoreUids.subList(insert - range - pad, insert - range));
            } else {
                rest.addAll(gameScoreUids.subList(insert + range, insert + range + pad));
            }
        }
        members = members + "," +
                StringUtils.join(FluentIterable.from(rest).transform(scoresIdTransformer).filter(input -> input != null).toList(), ",");
        list.addAll(FluentIterable.from(rest).transform(idsScoreTransformer).filter(input -> input != null).toList());
        if (list.size() >= 30) {
            GameBoard gameBoard = new GameBoard(new GameBoard.GameSongBoardKey(uid, appId, today), members);
            manager.insert(gameBoard); // 保存生成的榜单，下次直接获取
        }
        Collections.sort(list, new GameScore.ScoreComparator());
        return list;
    }

    /**
     * 排行榜完全由好友组成
     *
     * @param you
     * @param friends
     */
    private List<GameScore> baseAllFriends(User you, Integer appId, List<Friend> friends, Date today) {
        log.debug("get song board from friends, friends size is {}", friends.size());
        String uid = you.getId();
        List<String> allUids = Lists.newArrayList();
        if (null == friends) {
            friends = new ArrayList<>();
        }

        allUids.add(uid);
        allUids.addAll(FluentIterable.from(friends).transform(friendsIdTransformer).filter(input -> input != null).toList());
        String members = StringUtils.join(allUids, ",");
        GameBoard gameBoard = new GameBoard(new GameBoard.GameSongBoardKey(you.getId(), appId, today), members);
        manager.insert(gameBoard); // 保存生成的榜单，下次直接获取

        // 对list 根据score排序
        List<GameScore> list = manager.typedQuery(GameScore.class, selectInStmt(allUids, appId, today)).get();
        List<GameScore> result = fillGameScores(list, allUids, appId, today);
        Collections.sort(result, new GameScore.ScoreComparator());
        return result;
    }

    /**
     * 获取玩家下一等级升级最少的经验值
     *
     * @param currentLevel
     * @return
     */
    public Map<String, Integer> getNextLevelExperience(Integer appId, Integer currentLevel, Integer exp) {
        Map<String, Integer> res = Maps.newHashMap();
        GameLevel gameLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, currentLevel));
        GameLevel gameSongNextLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, currentLevel + 1));
        if (null == gameSongNextLevel) {
            // 已经是最高级
            res.put("next_level_exp", -1);
            res.put("next_level_needed", -1);
        } else {
            res.put("next_level_exp", gameSongNextLevel.getExperience() - gameLevel.getExperience());
            res.put("next_level_needed", gameSongNextLevel.getExperience() - exp);
        }
        return res;
    }

    public void updateGameOnlineNum(Integer gameId, boolean incr) {
        GameOnlineCount onlineCount = manager.forUpdate(GameOnlineCount.class, gameId);
        if (incr) {
            onlineCount.getCounter().incr();
        } else {
            onlineCount.getCounter().decr();
        }
        manager.update(onlineCount);
    }

    public void reportUser(Integer appId, String uid, String desc, User user) {
        UserReport ur = new UserReport(new UserReport.UserReportKey(uid, appId));
        if (StringUtils.isNotEmpty(desc)) {
            ur.setAttr(ImmutableMap.of("desc", desc, "reporter", user.getId()));
        }
        manager.insert(ur);
    }

    public void giveLike(Integer appId, String uid, User user) {
        GameLike like = manager.find(GameLike.class, new GameInteractionKey(uid, appId, user.getId()));
        Integer count = like.getLike();
        if (count == null) {
            like.setLike(1);
        } else {
            like.setLike(count + 1);
        }
        manager.update(like);
    }

    public GamePlayer getOrCreateUser(String uid, Integer appId) {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, appId));
        log.debug("player {} is null for {}, insert", uid, appId);
        if (null == player) {  // 没有就初始化
            player = new GamePlayer();
            player.setKey(new GamePlayerKey(uid, appId));
            player.setExperience(0);
            player.setLevel(1);
            player.setLoseNum(0);
            player.setStars(0);
            player.setThreeStarNum(0);
            player.setTitle(1);
            player.setTieNum(0);
            player.setWinNum(0);
            player.setRecord(0);

            addGameOnlineNum(appId, 1, true);
        }
        return player;
    }

    public List<GamePlayer> getGamePlayers(Collection<String> idList, Integer appId) {
        return new ArrayList<>(manager.typedQuery(GamePlayer.class, select().from("game_players").where(in("uid", idList.toArray())).and(eq("game_id", appId))).get());
    }

    public ExpResultHolder increaseExp(GamePlayer player, Integer appId, Integer win, com.google.common.base.Optional<Integer> exp) {
        Integer level = player.getLevel();
        GameLevel gameLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, level));
        GameLevel gameSongNextLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, level + 1));
        boolean up = false;
        int nextExp = -1;
        int totalExp = -1;
        if (exp.isPresent()) {
            player.setExperience(player.getExperience() + exp.get());
        } else if (win.equals(Constants.GAME_WIN) && gameLevel.getTwoExperience() != null) {
            player.setExperience(player.getExperience() + gameLevel.getTwoExperience());
        }
        if (gameSongNextLevel != null) {  // 还没有到顶级
            if (player.getExperience() >= gameSongNextLevel.getExperience()) { // 升级了
                up = true;
                player.setLevel(player.getLevel() + 1);
                GameLevel gameSongAfterNextLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, level + 2));
                if (gameSongAfterNextLevel != null) {
                    nextExp = gameSongAfterNextLevel.getExperience() - player.getExperience();
                    totalExp = gameSongAfterNextLevel.getExperience() - gameSongNextLevel.getExperience();
                }
            } else { // 没有升级
                nextExp = gameSongNextLevel.getExperience() - player.getExperience();
                totalExp = gameSongNextLevel.getExperience() - gameLevel.getExperience();
            }
        }
        return new ExpResultHolder(up, nextExp, totalExp);
    }

    public class ExpResultHolder {
        public final boolean isLevelUp;
        public final int nextExp;
        public final int totalExp;

        public ExpResultHolder(boolean isLevelUp, int nextExp, int totalExp) {
            this.isLevelUp = isLevelUp;
            this.nextExp = nextExp;
            this.totalExp = totalExp;
        }
    }

    public void saveScore(final GamePlayer player, Integer appId, Integer score, Date date) {
        /*
        String uid = player.getKey().getUid();
        GameScore.GameSongScoreKey key = new GameScore.GameSongScoreKey(uid, appId, date);
        GameScore gameScore = manager.find(GameScore.class, key);
        if (null == gameScore) {
            gameScore = new GameScore(key, score);
            log.debug(String.format("user %s save daily win Num %d first",
                    player.getKey().getUid(), player.getWinNum().intValue()));
            manager.insert(gameScore);
            GameScoreUid CleanScoreUid = new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(date, appId, score, uid));
            manager.insert(CleanScoreUid);
        }
        Integer oldScore = gameScore.getScore();
        if (!oldScore.equals(score)) {
            gameScore.setScore(score);
            log.debug(String.format("user %s update daily win Num, old score %d and new score %d",
                    player.getKey().getUid(), oldScore.intValue(), score.intValue()));
            manager.update(gameScore);
            GameScoreUid cleanScoreUid = new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(date, appId, score, uid));
            manager.insert(cleanScoreUid);
            manager.deleteById(GameScoreUid.class, new GameScoreUid.GameSongScoreUidKey(date, appId, oldScore, uid));
        }
        */
        log.debug(String.format("user %s save win Num %d first",
                player.getKey().getUid(), player.getWinNum().intValue()));
        String uid = player.getKey().getUid();
        GameScore.GameSongScoreKey key = new GameScore.GameSongScoreKey(uid, appId, date);
        GameScore gameScore = manager.find(GameScore.class, key);
        if (null == gameScore) {
            gameScore = new GameScore(key, 1);
            manager.insert(gameScore);
        } else {
            gameScore.setScore(gameScore.getScore() + 1);
            manager.update(gameScore);
        }
    }

    public void updateCoins(GamePlayer player, Integer appId, Long coins) {
        Long preCoins = player.getGold();
        //player.setGold(coins);
        player.getAttr().put("gold", Long.toString(coins));
        log.debug(String.format("user %s previous coins %d and current coins %d", player.getKey().getUid(), preCoins.longValue(), coins.longValue()));
    }

    public void userLoginLogoutGame(UserLoginLogoutGameEvent gameEvent) {
        try {
            this.taskService.execute(() -> {
                //发布用户登录登录游戏事件
                if (null != gameEvent) {
                    String routeKey = "game.event.us";
                    if ("0086".equals(gameEvent.getCountryCode())) {
                        routeKey = "game.event.cn";
                    }
                    String eventType = "game.login.or.logout";
                    queueService.sendEvent(routeKey, eventType, gameEvent);
                }
            });
        } catch (Exception e) {
            //捕获异常，否则event bus会重复调度执行
            log.error(e.getMessage(), e);
        }
    }


}
