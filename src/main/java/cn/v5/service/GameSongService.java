package cn.v5.service;

import cn.v5.code.StatusCode;
import cn.v5.entity.game.GamePlayer;
import cn.v5.entity.game.GamePlayerKey;
import cn.v5.entity.game.billboard.GameScore;
import cn.v5.entity.game.billboard.GameScoreUid;
import cn.v5.entity.game.level.GameLevel;
import cn.v5.entity.game.song.*;
import cn.v5.entity.vo.game.SongBagVo;
import cn.v5.entity.vo.game.SongStarVo;
import cn.v5.util.Constants;
import cn.v5.util.JsonUtil;
import cn.v5.util.LoggerFactory;
import cn.v5.web.controller.ServerException;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import info.archinnov.achilles.persistence.PersistenceManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-24 下午3:30
 */
@Service
public class GameSongService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(GameSongService.class);

    @Value("${game.song.id}")
    private int gameSongId;

    @Value("${game.song.power}")
    private int gameSongPower;

    @Value("${game.song.power.recover.time}")
    private int songRecoverTime;

    private List<GameSongBag> gameSongBags;

    private List<GameSongScoreStar> gameSongScoreStars;


    private Select.Where selectInStmt(List idList) {
        return select().from("game_songs").where(in("id", idList.toArray()));
    }

    private final static Select selectAllStmt = select().from("game_songs");

    @Inject
    private PersistenceManager manager;

    @Override
    public void afterPropertiesSet() throws Exception {
        /*
         * achilles complains about "Cannot perform typed query because the entityClass 'cn.v5.entity.game.song.GameSongScoreStar' is not managed by Achilles"
         */
        /*
        // 初始化不变的数据
        gameSongScoreStars = manager.typedQuery(GameSongScoreStar.class, "select * from game_song_score_stars").get();
        for(int i = gameSongScoreStars.size()-1; i > 0; i--) {
            for(int j = 0; j < i; j++) {
                GameSongScoreStar score1 = gameSongScoreStars.get(j);
                GameSongScoreStar score2 = gameSongScoreStars.get(j+1);
                if(score1.getScore() > score2.getScore()) {
                    GameSongScoreStar temp = gameSongScoreStars.get(j);
                    gameSongScoreStars.set(j, gameSongScoreStars.get(j+1));
                    gameSongScoreStars.set(j+1, temp);
                }
            }
        }

        gameSongBags = manager.typedQuery(GameSongBag.class, "select * from game_song_bags").get();
        for(int i = gameSongBags.size()-1; i > 0; i--) {
            for(int j = 0; j < i; j++) {
                GameSongBag bag1 = gameSongBags.get(j);
                GameSongBag bag2 = gameSongBags.get(j+1);
                if(bag1.getId() > bag2.getId()) {
                    GameSongBag temp = gameSongBags.get(j);
                    gameSongBags.set(j, gameSongBags.get(j+1));
                    gameSongBags.set(j+1, temp);
                }
            }
        }
        */
    }

    public void setDefaultValue() {
        List<GamePlayer> list = manager.typedQuery(GamePlayer.class, select().from("game_players")).get();
        for (GamePlayer player : list) {
            player.setPower(5);
            player.setPowerConsumeTime(0l);
            manager.update(player);
        }
    }

    public GamePlayer getSongPlayer(String id) {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(id, gameSongId));
        return player;
    }

    /**
     * 根据歌曲id获取玩家获取的星星
     *
     * @param songIds
     * @param uid
     * @return
     */
    public List<SongStarVo> getGameSongStars(List<String> songIds, String uid) {
        List<GameSongStar> gameSongStars = Lists.newArrayList();
        List<SongStarVo> res = Lists.newArrayList();
        Table<Integer, Integer, Integer> songStars = HashBasedTable.create();
        for (String songId : songIds) {
            gameSongStars.addAll(manager.typedQuery(GameSongStar.class,
                    select().from("game_song_stars").where(eq("uid", uid)).and(eq("song_id", Integer.valueOf(songId)))
            ).get());
        }
        for (GameSongStar songStar : gameSongStars) {
            songStars.put(songStar.getId().getSongId(), songStar.getId().getMode(), songStar.getStars());
        }
        for (String songId : songIds) {
            Integer id = Integer.valueOf(songId);
            res.add(new SongStarVo(id, songStars.row(id)));
        }
        return res;
    }

    private Integer[] sumGameSongStars(String uid) {
        Integer allStars = 0;
        Integer threeStarNum = 0;
        List<GameSongStar> list = manager.typedQuery(GameSongStar.class, select().from("game_song_stars").where(eq("uid", uid))).get();
        for (GameSongStar star : list) {
            if (star.getId().getMode() != Constants.GAME_SINGLE_EASY) {
                allStars += star.getStars();
                if (star.getStars() == 3)
                    threeStarNum++;
            }
        }

        return new Integer[]{allStars, threeStarNum};
    }

    /**
     * 根据分数生成星星
     *
     * @param
     * @return
     */
    /*
    public Integer createGameSongStar(Integer score, Integer songId, String uid) {
        log.debug("createGameSongStar score {}, songId {}, uid {}", score, songId, uid);
        Integer stars = 0;
        for(GameSongScoreStar scoreStar : gameSongScoreStars) {
            if(score >= scoreStar.getScore())
                stars = scoreStar.getStars();
            else break;
        }

        GameSongStar.GameSongStarKey id = new GameSongStar.GameSongStarKey(songId, uid);
        GameSongStar songStar = manager.find(GameSongStar.class, id);
        if(null == songStar) {
            songStar = new GameSongStar(id, stars);
            manager.persist(songStar);
        } else {
            //if(songStar.getStars() < stars) {  TODO:对于单首歌曲只保存最多的星星?
            songStar.setStars(stars);
            manager.update(songStar);
            //}
        }

        return stars;
    }
    */
    public Integer createGameSongStar(Integer stars, Integer songId, String uid, Integer mode) {
        log.debug("createGameSongStar stars {}, songId {}, uid {}", stars, songId, uid);
        /*
        Integer stars = 0;
        for(GameSongScoreStar scoreStar : gameSongScoreStars) {
            if(score >= scoreStar.getScore())
                stars = scoreStar.getStars();
            else break;
        }
        */

        GameSongStar.GameSongStarKey id = new GameSongStar.GameSongStarKey(songId, uid, mode);
        GameSongStar songStar = manager.find(GameSongStar.class, id);
        if (null == songStar) {
            songStar = new GameSongStar(id, stars);
            manager.insert(songStar);
        } else {
            /*
            //if(songStar.getStars() < stars) {  TODO:对于单首歌曲只保存最多的星星?
            songStar.setStars(stars);
            manager.update(songStar);
            //}
            */
            if (songStar.getStars() < stars) {
                songStar.setStars(stars);
                manager.update(songStar);
            }
        }

        return stars;
    }

    /**
     * 消耗体力
     *
     * @param uid
     * @return
     */
    public Integer consumeSongPlayerPower(String uid, Integer power) {

        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, gameSongId));

        Integer currentPower = player.getPower();
        if (currentPower == 0) {
            return -1;
        } else {
            Integer result = currentPower - power;
            player.setPower(result > 0 ? result : 0);
            if (player.getPowerConsumeTime() == 0) { // 记录消耗时间
                player.setPowerConsumeTime(System.currentTimeMillis() / 1000);
            }
            manager.update(player);
            return player.getPower();
        }
    }

    public void consumeSongPlayerPower(String uid, Integer power, Map<String, Integer> res) {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, gameSongId));
        int initPower = gameSongPower;
        Integer currentPower = player.getPower();
        long current = System.currentTimeMillis() / 1000; // 当前时间
        int mod = 0;
        // first recovery
        if (currentPower < initPower) {
            long time = player.getPowerConsumeTime();
            if (time <= 0) {
                time = player.getPowerRecoverTime();
            }
            long interval = current - time;
            int count = (int) interval / songRecoverTime;   // 恢复的次数
            mod = (int) (interval % songRecoverTime);
            if (count > 0) {
                log.debug("Current power {}  recovered {} and elapsed {}", currentPower, count, mod);
                player.setPower(currentPower + count > initPower ? initPower : currentPower + count);
                player.setPowerRecoverTime(current - mod);
                player.setPowerConsumeTime(0l);
                currentPower = player.getPower();
                manager.update(player);
            }
        }

        // then consume
        if (currentPower > 0) {
            Integer result = currentPower - power;
            player.setPower(result > 0 ? result : 0);
            if ((player.getPowerConsumeTime() == null || player.getPowerConsumeTime() <= 0) &&
                    currentPower == initPower) { // 仅第一次记录消耗时间
                player.setPowerConsumeTime(System.currentTimeMillis() / 1000);
                mod = 0;
            }
            manager.update(player);
        } else {
            throw new ServerException(StatusCode.GAME_PLAYER_LACK_POWER, "玩家缺少体力");
        }

        String fmt = "last consume_time %d, recovery_time %d and current_time %d";
        log.debug(String.format(fmt, player.getPowerConsumeTime().longValue(),
                player.getPowerRecoverTime().longValue(), current));
        res.put("elapsed", mod);
        res.put("power", player.getPower());
        res.put("rec_time", songRecoverTime);
    }

    /**
     * 获取玩家的体力
     *
     * @param uid
     * @return
     */
    public Integer getSongPlayerPower(String uid) {
        Integer gameSongId = gameSongPower;
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, gameSongId));
        int initPower = gameSongPower;
        Integer power = player.getPower();
        if (power < initPower) {
            // 体力不满 看要是否要恢复
            long current = System.currentTimeMillis() / 1000; // 当前时间
            long time = player.getPowerConsumeTime();
            if (0 == time) {
                time = player.getPowerRecoverTime();
            }
            long interval = current - time;
            int count = (int) interval / songRecoverTime;   // 恢复的次数
            if (count > 0) {
                player.setPower(power + count > initPower ? initPower : power + count);
                player.setPowerRecoverTime(current);
                player.setPowerConsumeTime(0l);

                manager.update(player);
            }
        }
        return player.getPower();
    }

    public void getSongPlayerPower(String uid, Map<String, Integer> res) {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, gameSongId));
        int initPower = gameSongPower;
        Integer power = player.getPower();
        if (power < initPower) {
            // 体力不满 看要是否要恢复
            long current = System.currentTimeMillis() / 1000; // 当前时间
            String fmt = "last consume_time %d, recovery_time %d and current_time %d";
            log.debug(String.format(fmt, player.getPowerConsumeTime().longValue(),
                    player.getPowerRecoverTime().longValue(), current));
            long time = player.getPowerConsumeTime();
            if (time <= 0) {
                time = player.getPowerRecoverTime();
            }
            long interval = current - time;
            int count = (int) interval / songRecoverTime;   // 恢复的次数
            int mod = (int) (interval % songRecoverTime);
            if (count > 0) {
                player.setPower(power + count > initPower ? initPower : power + count);
                player.setPowerRecoverTime(current - mod);
                player.setPowerConsumeTime(0l);
                manager.update(player);
            }
            res.put("elapsed", mod);
        } else {
            res.put("elapsed", 0);
        }
        res.put("power", player.getPower());
        res.put("rec_time", songRecoverTime);
    }

    /**
     * 更新玩家的分数，返回计算后的分数
     *
     * @param date
     * @param uid
     * @param score
     * @param mode
     * @param win
     * @return
     * @throws IOException
     */
    /*
    public Integer updateSongScore(Date date, String uid, Integer score, Integer mode, Integer win) throws IOException {
        Integer gameSongId = ConfigUtils.getInt("game.song.id");
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayer.GamePlayerKey(uid, gameSongId));
        if(null == player) {  // 没有就初始化
            player = new GamePlayer();
            player.setKey(new GamePlayer.GamePlayerKey(uid, gameSongId));
            player.setExperience(0);
            player.setLevel(1);
            player.setLoseNum(0);player.setStars(0);player.setThreeStarNum(0);
            player.setTitle(1);
            player.setTieNum(0);player.setWinNum(0);
            player.setPower(ConfigUtils.getInt("game.song.power"));
            player.setPowerConsumeTime(0l);
            player.setPowerRecoverTime(0l);
            manager.persist(player);
            player = manager.find(GamePlayer.class, new GamePlayer.GamePlayerKey(uid, gameSongId));
        }
        Integer level = player.getLevel();
        GameLevel gameSongLevel = manager.find(GameLevel.class, level);
        GameLevel gameSongNextLevel = manager.find(GameLevel.class, level+1);
        boolean up = false;
        if(gameSongNextLevel != null) {  // 还没有到顶级
            if(mode == Constants.GAME_BATTLE_MODE) {
                player.setExperience(player.getExperience() + gameSongLevel.getTwoExperience());
            } else {
                player.setExperience(player.getExperience() + gameSongLevel.getSingleExperience());
            }

            if(player.getExperience() > gameSongLevel.getExperience()) {
                player.setLevel(player.getLevel() + 1);
                up = true; // 升级了
            }
        }

        int addition = gameSongLevel.getAddition();
        if(up) {
            addition = gameSongNextLevel.getAddition(); // 按升级后的等级加成
        }
        Float fScore = new Float(score);
        Float fAddition = (new Float(addition))/100;
        score = Math.round(fScore * (1 + fAddition));

        Integer[] nums = sumGameSongStars(uid);
        player.setStars(nums[0]);
        player.setThreeStarNum(nums[1]);

        if(mode == Constants.GAME_BATTLE_MODE) {
            if(win == Constants.GAME_WIN)
                player.setWinNum(player.getWinNum() + 1);
            else if(win == Constants.GAME_LOSE)
                player.setLoseNum(player.getLoseNum() + 1);
            else
                player.setTieNum(player.getTieNum() + 1);
        }
        player.setTitle(createPlayerTitle(player));
        manager.update(player);

        GameSongScore.GameSongScoreKey key = new GameSongScore.GameSongScoreKey(uid, date);
        GameSongScore songScore = manager.find(GameSongScore.class, key);
        if(null == songScore) {
            log.debug("save daily score first");
            songScore = new GameSongScore(key, score);
            manager.persist(songScore);
            GameSongScoreUid songScoreUid = new GameSongScoreUid(new GameSongScoreUid.GameSongScoreUidKey(date, score, uid));
            manager.persist(songScoreUid);
        } else if(score > songScore.getScore()) {
            log.debug("update daily score");
            Integer oldScore = songScore.getScore();
            songScore.setScore(score);
            manager.update(songScore);

            GameSongScoreUid songScoreUid = manager.find(GameSongScoreUid.class, new GameSongScoreUid.GameSongScoreUidKey(date, oldScore, uid));
            if(null != songScoreUid) manager.remove(songScoreUid);
            songScoreUid = new GameSongScoreUid(new GameSongScoreUid.GameSongScoreUidKey(date, score, uid));
            manager.persist(songScoreUid);
        }

        return score;
    }
    */
    public Map<String, Integer> updateSongScore(Date date, String uid, Integer appId, Integer score, Integer stars, Integer mode, Integer win) throws IOException {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, appId));
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
            player.setPower(gameSongPower);
            player.setPowerConsumeTime(0l);
            player.setPowerRecoverTime(0l);
            player.setRecord(0);
            player = manager.insert(player);
        }
        Integer level = player.getLevel();
        GameLevel gameLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, level));
        GameLevel gameSongNextLevel = manager.find(GameLevel.class, new GameLevel.GameLevelKey(appId, level + 1));
        boolean up = false;
        if (gameSongNextLevel != null) {  // 还没有到顶级
            if (mode == Constants.GAME_BATTLE_MODE) {
                player.setExperience(player.getExperience() + gameLevel.getTwoExperience());
            } else {
                player.setExperience(player.getExperience() + gameLevel.getSingleExperience());
            }

            if (player.getExperience() > gameLevel.getExperience()) {
                player.setLevel(player.getLevel() + 1);
                up = true; // 升级了
            }
        }

        /*
        int addition = gameLevel.getAddition();
        if(up) {
            addition = gameSongNextLevel.getAddition(); // 按升级后的等级加成
        }
        Float fScore = new Float(score);
        Float fAddition = (new Float(addition))/100;
        score = Math.round(fScore * (1 + fAddition));
        */

        Integer[] nums = sumGameSongStars(uid);
        player.setStars(nums[0]);
        player.setThreeStarNum(nums[1]);

        if (mode == Constants.GAME_BATTLE_MODE) {
            if (win == Constants.GAME_WIN)
                player.setWinNum(player.getWinNum() + 1);
            else if (win == Constants.GAME_LOSE)
                player.setLoseNum(player.getLoseNum() + 1);
            else
                player.setTieNum(player.getTieNum() + 1);
        }
        player.setTitle(createPlayerTitle(player));
        if (player.getRecord() == null || score > player.getRecord()) {
            player.setRecord(score);
        }
        manager.update(player);

        GameScore.GameSongScoreKey key = new GameScore.GameSongScoreKey(uid, appId, date);
        GameScore songScore = manager.find(GameScore.class, key);
        if (null == songScore) {
            log.debug(String.format("save daily score %d first", score.intValue()));
            songScore = new GameScore(key, score);
            manager.insert(songScore);

            GameScoreUid songScoreUid = new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(date, appId, score, uid));
            manager.insert(songScoreUid);
        } else if (score > songScore.getScore()) {
            Integer oldScore = songScore.getScore();
            songScore.setScore(score);
            log.debug(String.format("update daily score, old score %d and new score %d",
                    oldScore.intValue(), score.intValue()));
            manager.update(songScore);

            /*
            GameSongScoreUid songScoreUid = manager.find(GameSongScoreUid.class, new GameSongScoreUid.GameSongScoreUidKey(date, oldScore, uid));
            if(null != songScoreUid) manager.remove(songScoreUid);
            */
            GameScoreUid songScoreUid = new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(date, appId, score, uid));
            manager.insert(songScoreUid);
            manager.deleteById(GameScoreUid.class, new GameScoreUid.GameSongScoreUidKey(date, appId, oldScore, uid));
        }

        Map<String, Integer> map = Maps.newHashMap();
        map.put("stars", stars);
        map.put("score", score);
        return map;
    }

    public List<SongBagVo> getSongBag(String uid) throws IOException {
        GamePlayer player = manager.find(GamePlayer.class, new GamePlayerKey(uid, gameSongId));
        List<SongBagVo> res = Lists.newArrayList();
        for (int i = 0; i < gameSongBags.size(); i++) {
            GameSongBag bag = gameSongBags.get(i);

            SongBagVo bagVo = new SongBagVo();
            bagVo.setId(bag.getId());
            bagVo.setUnlocked(player.getTitle() >= bag.getId());
            bagVo.setName(bag.getName());
            if (StringUtils.isNotBlank(bag.getBagLimit())) {
                Map<String, Integer> limit = JsonUtil.fromJson(bag.getBagLimit(), new TypeReference<Map<String, Integer>>() {
                });
                bagVo.setLimit(limit);
            } else {
                bagVo.setLimit(null);
            }

            String[] songIds = StringUtils.split(bag.getSongIds(), ",");
            List<Integer> songIdList = Lists.newArrayList();
            for (String songId : songIds) {
                songIdList.add(Integer.valueOf(songId));
            }
            bagVo.setSongIds(songIdList);
            /*List<GameSong> songs = manager.typedQuery(GameSong.class, "select * from game_songs where id in ?", songIdList).get();
            bagVo.setSongs(songs); TODO: 暂时是客户端存储*/
            res.add(bagVo);
        }

        return res;
    }

    private Integer createPlayerTitle(GamePlayer player) throws IOException {
        Integer title = 1;
        for (int i = 1; i <= gameSongBags.size(); i++) {
            GameSongBag bag = gameSongBags.get(i);
            String limit = bag.getBagLimit();
            title = bag.getId();
            if (StringUtils.isBlank(limit)) {
                continue;
            } else {
                boolean levelFlag = false, starsFlag = false, expFlag = false,
                        threeStarsFlag = false, winNumFlag = false;
                Map<String, Integer> map = JsonUtil.fromJson(limit, new TypeReference<Map<String, Integer>>() {
                });
                if (!map.containsKey("level") || (map.containsKey("level") && map.get("level") <= player.getLevel()))
                    levelFlag = true;
                if (!map.containsKey("stars") || (map.containsKey("stars") && map.get("stars") <= player.getStars()))
                    starsFlag = true;
                if (!map.containsKey("three_stars") || (map.containsKey("three_stars") && map.get("three_stars") <= player.getThreeStarNum()))
                    threeStarsFlag = true;
                if (!map.containsKey("win_num") || (map.containsKey("win_num") && map.get("win_num") <= player.getWinNum()))
                    winNumFlag = true;
                if (!map.containsKey("experience") || (map.containsKey("experience") && map.get("experience") <= player.getExperience()))
                    expFlag = true;

                boolean pass = threeStarsFlag && starsFlag && levelFlag && winNumFlag && expFlag;
                if (pass == false) {
                    title = bag.getId() - 1; // 这关不过
                    break;
                }
            }
        }

        return title;
    }

    private List<GameScore> sortGameSongScore(List<GameScore> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                GameScore score1 = list.get(j);
                GameScore score2 = list.get(j + 1);
                if (score1.getScore() < score2.getScore()) {
                    GameScore temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                }
            }
        }

        return list;
    }

    public List<GameSong> getSongs(List<Integer> ids) {
        return manager.typedQuery(GameSong.class, selectInStmt(ids)).get();
    }

    public List<GameSong> getAllSongs() {
        return manager.typedQuery(GameSong.class, selectAllStmt).get();
    }

    public List<Integer> getLibraySongs(String uid) {
        List<Integer> songIds = Lists.newArrayList();
        Set<Integer> songIdSet = Sets.newHashSet();
        List<GameSongStar> list = manager.sliceQuery(GameSongStar.class).forSelect().withPartitionComponents(uid).get();
        for (GameSongStar star : list) {
            if (star.getStars() >= 3 && star.getId().getMode() != Constants.GAME_SINGLE_EASY) {
                songIdSet.add(star.getId().getSongId());
            }
        }
        songIds.addAll(songIdSet);

        return songIds;
    }

    public void setDailySong(Integer songId, Integer appId, Date date) {
        DailySong dailySong = new DailySong(new DailySong.DailySongKey(appId, date));
        dailySong.setSongId(songId);
        manager.insert(dailySong);
    }

    public Integer getDailySong(Integer appId, Date date) {
        DailySong dailySong = manager.find(DailySong.class, new DailySong.DailySongKey(appId, date));
        if (dailySong == null) {
            return null;
        }
        return dailySong.getSongId();
    }

    private static class FilterUidPredicate implements Predicate<GameScoreUid> {
        private final Set<String> filter;

        private FilterUidPredicate(Set<String> filter) {
            this.filter = filter;
        }

        @Override
        public boolean apply(GameScoreUid input) {
            return filter.contains(input.getKey().getUid()) == false;
        }
    }

    public static void main(String[] args) {
        int score = 1999;
        int addition = 6;
        Float s = new Float(score);
        Float a = new Float(addition) / 100;
        System.out.println(s);
        System.out.println(1 + a);

        //score = ;

        System.out.println(Math.round(s * (1 + a)));
    }
}
