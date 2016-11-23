package cn.v5.entity.game;

import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.EmbeddedId;
import info.archinnov.achilles.annotations.Entity;
import info.archinnov.achilles.annotations.JSON;

import java.util.Map;

/**
 * Created by xzhang on 2015/3/9.
 */
@Entity(table = "game_interaction")
public class GameInteraction {

    public static class INTERACTIONS {
        public static String ENSLAVE = "enslave";
        public static String CUTE = "cute";
        public static String FLIRT = "flirt";

        public static class ENSLAVE_PROP_KEY {
            public static final String BEGIN = "begin";
            public static final String DURATION = "duration";
            public static final String ROLE = "role";
        }

        public static class ENSLAVE_PROP_VALUE {
            public static final String INFINITE = "infinite";
            public static final String MASTER = "master";
            public static final String SLAVE = "slave";
        }
    }

    @EmbeddedId
    private GameInteractionKey key;

    @Column
    @JSON
    private Map<String, Map<String, String>> interactions;

    public GameInteractionKey getKey() {
        return key;
    }

    public void setKey(GameInteractionKey key) {
        this.key = key;
    }

    public Map<String, Map<String, String>> getInteractions() {
        return interactions;
    }

    public void setInteractions(Map<String, Map<String, String>> interactions) {
        this.interactions = interactions;
    }
}
