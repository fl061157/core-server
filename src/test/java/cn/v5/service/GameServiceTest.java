package cn.v5.service;

import cn.v5.entity.Friend;
import cn.v5.entity.User;
import cn.v5.entity.UserKey;
import cn.v5.entity.game.billboard.GameBoard;
import cn.v5.entity.game.billboard.GameScore;
import cn.v5.entity.game.billboard.GameScoreUid;
import cn.v5.test.TestTemplate;
import cn.v5.util.TimeUtils;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by wyang on 2014/7/3.
 */
public class GameServiceTest extends TestTemplate {


    @InjectMocks
    @Autowired
    GameService gameService;

    private static final String selectInStmt = "select * from game_score where uid in ? and app_id=? and date=?";
    private final static String selectAscStmt = "select * from game_score_uid where date=? and app_id=? order by score asc";
    private static final String uid = "me";
    private static final Integer appId = 2000;
    private static final Date today = TimeUtils.getTodayStart();
    private static final Date yesterday = TimeUtils.getYesterdayStart();

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    public void testSaveGame() {
        /*
        Game game = new Game();
        game.setId(new Game.GameKey(2000, "0086"));
        game.setName("yw_test");
        game.setRemark("yw_test");
        game.setRule("yw_test");
        game.setDesc("yw_test");
        game.setGameServers("10.162.18.49");
        game.setPicUrl(Arrays.asList("yw_test"));
        manager1.persist(game);

        game = new Game();
        game.setId(new Game.GameKey(2000, "0086"));
        game.setName("yw_test");
        game.setRemark("yw_test");
        game.setRule("yw_test");
        manager1.persist(game);

        game = manager1.find(Game.class, new Game.GameKey(2000, "0086"));
        assertNull(game.getDesc());
        assertNull(game.getGameServers());
        assertNull(game.getPicUrl());

        game = manager1.getProxy(Game.class, new Game.GameKey(2000, "0086"));
        game.setName("yw_test_proxy");
        manager1.update(game);
        game = manager1.find(Game.class, new Game.GameKey(2000, "0086"));
        assertTrue(game.getRemark().compareTo("yw_test") == 0);
        assertTrue(game.getRule().compareTo("yw_test") == 0);


        final String selectGames = "select * from game_info where country_code=? allow filtering";
        List<Game> games = manager1.typedQuery(Game.class, selectGames, "0086").get();
        assertTrue(games.size() == 1);
        games = manager1.typedQuery(Game.class, selectGames, "0001").get();
        assertTrue(games.size() == 0);
        */
    }

    @Test
    public void testBaseSongBoard() {
//        System.out.println("----------------test base song board--------------------");
//        User m = new User();
//        m.setId(uid);
//        // there is board("1,me,2") today
//        String members = "1,me,2";
//        mockBoard(new GameBoard(new GameBoard.GameSongBoardKey(uid, appId, today), members));
//
//        // add new friend "3"
//        TypedQueryBuilder friendBuilder = Mockito.mock(TypedQueryBuilder.class);
//        when(manager.typedQuery(Friend.class, "select * FROM friends where user_id = ? and app_id = ?", uid, 0))
//                .thenReturn(friendBuilder);
//        when(friendBuilder.get())
//                .thenReturn(Lists.newArrayList(new Friend(new UserKey(uid, appId, "3"), "friend_test")));
//
//        // only have my and "3" scores
//        List<GameScore> scores = Lists.newArrayList(
//                new GameScore(new GameScore.GameSongScoreKey(uid, appId, today), 40),
//                new GameScore(new GameScore.GameSongScoreKey("3", appId, today), 20)
//        );
//        mockScores(scores, today);
//
//        List<GameScore> result = gameService.getSongBoard(m, appId);
//        assertSame(result.size(), 4);
//        assertTrue(result.get(0).getKey().getUid().equals(uid));
//        assertSame(result.get(0).getScore(), 40);
//        assertSame(result.get(1).getScore(), 20);
//        assertTrue(result.get(1).getKey().getUid().equals("3"));
//        assertTrue(result.get(2).getScore() == -1);
    }

    //@Test
    public void testBaseAllFriends() {
        System.out.println("----------------test base all friends--------------------");
        User m = new User();
        m.setId(uid);
        mockBoard(null);
        int num = 40;
        List<Friend> friends = Lists.newArrayList();
        for (int i = 0; i < num; i++) {
            Friend f = new Friend(new UserKey(uid, appId, Integer.toString(i)), Integer.toString(i) + "-c");
            friends.add(f);
        }
        mockFriends(friends);

        // only have my and "14" scores
        List<GameScore> scores = Lists.newArrayList(
                new GameScore(new GameScore.GameSongScoreKey("5", appId, today), 10),
                new GameScore(new GameScore.GameSongScoreKey("14", appId, today), 50)
        );
        mockScores(scores, today);

        List<GameScore> result = gameService.getSongBoard(m, appId);
        assertSame(result.size(), 41);
        assertTrue(result.get(0).getKey().getUid().equals("14"));
        assertSame(result.get(0).getScore(), 50);
        assertSame(result.get(1).getScore(), 10);
        assertTrue(result.get(1).getKey().getUid().equals("5"));
        assertTrue(result.get(2).getScore() == -1);
    }

    //@Test
    public void testBasePartFriends() {
        System.out.println("----------------test base part friends--------------------");
        User m = new User();
        m.setId(uid);
        mockBoard(null);
        int num = 5;
        List<Friend> friends = Lists.newArrayList();
        int i;
        for (i = 0; i < num; i++) {
            Friend f = new Friend(new UserKey(uid, appId, Integer.toString(i)), Integer.toString(i) + "-c");
            friends.add(f);
        }
        mockFriends(friends);

        // only have my and "14" scores
        List<GameScore> yscores = Lists.newArrayList(
                new GameScore(new GameScore.GameSongScoreKey("1", appId, yesterday), 10),
                new GameScore(new GameScore.GameSongScoreKey("2", appId, yesterday), 50)
        );
        mockScores(yscores, yesterday);
        mockScores(yscores, today);

        int ori = i;
        // 1) not enough players
        List<GameScoreUid> scoreUids = Lists.newArrayList(
                new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 50, "2")),
                new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 11, Integer.toString(i++))), // "5"
                new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 18, Integer.toString(i++)))  // "6"
        );
        mockScoreUids(scoreUids);
        System.out.println("----------------test not enough players--------------------");
        List<GameScore> result = gameService.getSongBoard(m, appId);
        assertSame(result.size(), i + 1);
        assertTrue(result.get(0).getKey().getUid().equals("2"));
        assertSame(result.get(0).getScore(), 50);
        assertSame(result.get(1).getScore(), 18);
        assertTrue(result.get(1).getKey().getUid().equals("6"));
        assertTrue(result.get(2).getScore() == 11);
        assertTrue(result.get(2).getKey().getUid().equals("5"));
        assertSame(result.get(3).getScore(), 10);
        assertTrue(result.get(3).getKey().getUid().equals("1"));


        // 2) no yesterday scores for friends and me
        mockScores(new ArrayList<GameScore>(), yesterday);
        scoreUids.clear();
        i = ori;
        for (int j = 0; j < 25; j++) {
            int score = 0;
            scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, score, Integer.toString(i++))));
        }
        // this one should be filtered
        scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 80, "3")));
        mockScoreUids(scoreUids);
        System.out.println("----------------test no yesterday scores--------------------");
        result = gameService.getSongBoard(m, appId);
        assertSame(result.size(), 30);
        assertTrue(result.get(0).getKey().getUid().equals("2"));
        assertSame(result.get(0).getScore(), 50);
        assertSame(result.get(1).getScore(), 10);
        assertTrue(result.get(1).getKey().getUid().equals("1"));


        // 3) yesterday score is lowest of all
        yscores = Lists.newArrayList(
                new GameScore(new GameScore.GameSongScoreKey("1", appId, yesterday), 7),
                new GameScore(new GameScore.GameSongScoreKey("2", appId, yesterday), 1)
        );
        mockScores(yscores, yesterday);
        scoreUids.clear();
        i = ori;
        for (int j = 0; j < 25; j++) {
            int score = j + 11;
            scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, score, Integer.toString(i++))));
        }
        // this one should be filtered
        scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 80, "3")));
        mockScoreUids(scoreUids);
        System.out.println("----------------test lowest yesterday scores--------------------");
        result = gameService.getSongBoard(m, appId);
        assertSame(result.size(), 30);
        assertTrue(result.get(0).getKey().getUid().equals("2"));
        assertSame(result.get(0).getScore(), 50);
        assertSame(result.get(1).getScore(), 34);
        assertSame(result.get(2).getScore(), 33);
        assertSame(result.get(3).getScore(), 32);
        assertSame(result.get(25).getScore(), 10);
        assertTrue(result.get(25).getKey().getUid().equals("1"));

        // 4) yesterday score is highest of all
        yscores = Lists.newArrayList(
                new GameScore(new GameScore.GameSongScoreKey("1", appId, yesterday), 90),
                new GameScore(new GameScore.GameSongScoreKey("2", appId, yesterday), 87)
        );
        mockScores(yscores, yesterday);
        scoreUids.clear();
        i = ori;
        for (int j = 0; j < 25; j++) {
            int score = j + 60;
            scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, score, Integer.toString(i++))));
        }
        // this one should be filtered
        scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 80, "3")));
        mockScoreUids(scoreUids);
        System.out.println("----------------test highest yesterday scores--------------------");
        result = gameService.getSongBoard(m, appId);
        assertSame(result.size(), 30);
        assertTrue(result.get(0).getKey().getUid().equals(Integer.toString(i - 1)));
        assertSame(result.get(0).getScore(), 84);
        assertSame(result.get(1).getScore(), 83);
        assertSame(result.get(2).getScore(), 82);
        assertSame(result.get(3).getScore(), 81);
        assertSame(result.get(24).getScore(), 50);
        assertTrue(result.get(24).getKey().getUid().equals("2"));
        assertSame(result.get(25).getScore(), 10);
        assertTrue(result.get(25).getKey().getUid().equals("1"));
        assertTrue(result.get(26).getKey().getUid().equals(uid));
        assertSame(result.get(26).getScore(), -1);

        // 5) yesterday score is in the range
        yscores = Lists.newArrayList(
                new GameScore(new GameScore.GameSongScoreKey("1", appId, yesterday), 24),
                new GameScore(new GameScore.GameSongScoreKey("2", appId, yesterday), 25)
        );
        mockScores(yscores, yesterday);
        scoreUids.clear();
        i = ori;
        for (int j = 0; j < 25; j++) {
            int score = j + 20;
            if (score == 24) {
                score = 25;
            }
            scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, score, Integer.toString(i++))));
        }
        // this one should be filtered
        scoreUids.add(new GameScoreUid(new GameScoreUid.GameSongScoreUidKey(today, appId, 80, "3")));
        mockScoreUids(scoreUids);
        System.out.println("----------------test middle yesterday scores--------------------");
        result = gameService.getSongBoard(m, appId);
        assertSame(result.size(), 30);
        assertTrue(result.get(0).getKey().getUid().equals("2"));
        assertSame(result.get(0).getScore(), 50);
        assertSame(result.get(1).getScore(), 43);
        assertSame(result.get(2).getScore(), 42);
        assertSame(result.get(3).getScore(), 41);
        assertSame(result.get(25).getScore(), 10);
        assertTrue(result.get(25).getKey().getUid().equals("1"));
        assertTrue(result.get(26).getKey().getUid().equals(uid));
        assertSame(result.get(26).getScore(), -1);
    }

    private void mockBoard(GameBoard mockBoard) {
        when(manager.find(eq(GameBoard.class), isA(GameBoard.GameSongBoardKey.class))).thenReturn(mockBoard);
    }

    private void mockFriends(List<Friend> friends) {
//        Object components[] = {uid, 0};
//        SliceQueryBuilder mockSlice = Mockito.mock(SliceQueryBuilder.class);
//        SliceQueryBuilder.SliceShortcutQueryBuilder mockSliceShort =
//                Mockito.mock(SliceQueryBuilder.SliceShortcutQueryBuilder.class);
//        when(manager.sliceQuery(Friend.class)).thenReturn(mockSlice);
//        when(mockSlice.partitionComponents(components)).thenReturn(mockSliceShort);
//        when(mockSliceShort.get()).thenReturn(friends);
    }

    private void mockScores(List<GameScore> scores, Date date) {
//        TypedQueryBuilder scoreBuilder = Mockito.mock(TypedQueryBuilder.class);
//        when(manager.typedQuery(eq(GameScore.class), eq(selectInStmt), anyListOf(String.class), eq(appId), eq(date)))
//                .thenReturn(scoreBuilder);
//        when(scoreBuilder.get()).thenReturn(scores);
    }

    private void mockScoreUids(List<GameScoreUid> scoreUids) {
//        TypedQueryBuilder scoreUidBuilder = Mockito.mock(TypedQueryBuilder.class);
//        when(manager.typedQuery(GameScoreUid.class, selectAscStmt, today, appId)).thenReturn(scoreUidBuilder);
//        when(scoreUidBuilder.get()).thenReturn(scoreUids);
    }
}
