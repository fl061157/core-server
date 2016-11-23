package cn.v5.service;

import cn.v5.entity.game.GamePlayer;
import cn.v5.entity.game.GamePlayerKey;
import cn.v5.entity.game.billboard.GameScore;
import cn.v5.test.TestTemplate;
import org.junit.Test;

/**
 * Created by wyang on 2014/7/3.
 */

public class GameSongServiceTest extends TestTemplate {
    private static final int appId = 2000;

    @Test(expected = NullPointerException.class)
    public void testGamePlayer() {
        GamePlayer gamePlayer = new GamePlayer();
        gamePlayer.setKey(new GamePlayerKey("yw_test", 1000));
        manager.insert(gamePlayer);

        gamePlayer = manager.find(GamePlayer.class, new GamePlayerKey("yw_test", 1000));
        //assertThat(gamePlayer.getPowerConsumeTime()).isEqualTo(0);
        long time = gamePlayer.getPowerConsumeTime();
        assertNull(time);
    }

    private boolean isSameGameSongScore(final GameScore o1, final GameScore o2) {
        if (!o1.getKey().getUid().equals(o2.getKey().getUid())) {
            System.out.println("actual " + o1.getKey().getUid() + " expected " + o2.getKey().getUid());
            return false;
        }
        if (!o1.getKey().getDate().equals(o2.getKey().getDate())) {
            System.out.println("actual " + o1.getKey().getDate() + " expected " + o2.getKey().getDate());
            return false;
        }
        if (o1.getScore().compareTo(o2.getScore()) != 0) {
            System.out.println("actual " + o1.getScore() + " expected " + o2.getScore());
            return false;
        }
        return true;
    }

    @Test
    public void testGetMultiGameSongScores() {
//        String[] uids = {new String("yw_test1"), new String("yw_test2"), new String("yw_test3")};
//        Date date = Calendar.getInstance().getTime();
//        List<GameScore> songScores = Lists.newArrayList();
//        Random rand = new Random();
//        List<String> all = new ArrayList<String>();
//        for (String uid : uids) {
//            int r = rand.nextInt(101);
//            GameScore score = new GameScore(new GameScore.GameSongScoreKey(uid, appId, date), r);
//            songScores.add(score);
//            manager.insert(score);
//            all.add(uid);
//        }
//        all.add(1, "yw_test4");
//        assertThat(all.size()).isEqualTo(4);
//        String selectInStmt = "select * from game_score where uid in ? and app_id=? and date=?";
//        List<GameScore> result = manager.typedQuery(GameScore.class, selectInStmt, all, appId, date).get();
//        assertThat(result.size()).isEqualTo(3);
//        assertTrue(isSameGameSongScore(result.get(0), songScores.get(0)));
//        assertTrue(isSameGameSongScore(result.get(1), songScores.get(1)));
//        assertTrue(isSameGameSongScore(result.get(2), songScores.get(2)));
//
//        new GameService().nonNull(result, all, appId, date);
//        assertThat(result.size()).isEqualTo(4);
//        assertTrue(isSameGameSongScore(result.get(1),
//                new GameScore(new GameScore.GameSongScoreKey("yw_test4", appId, date), -1)));
//        assertTrue(isSameGameSongScore(result.get(0), songScores.get(0)));
//        assertTrue(isSameGameSongScore(result.get(2), songScores.get(1)));
//        assertTrue(isSameGameSongScore(result.get(3), songScores.get(2)));
    }

    @Test
    public void testGetFriends() {
//        Object components[] = {"yw_test", 0};
//        List<Friend> friends = manager.sliceQuery(Friend.class).
//                partitionComponents(components).get();
//        assertThat(friends.size()).isEqualTo(0);
    }
}
