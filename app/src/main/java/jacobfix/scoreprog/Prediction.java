package jacobfix.scoreprog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

public class Prediction {

    private static final String TAG = Prediction.class.getSimpleName();

    private String userId;
    private String gameId;

    private int awayScore = NULL;
    private int homeScore = NULL;

    private boolean modified;
    private boolean onServer;

    private boolean updatesDisabled = false;

    private int exposure = X_FRIENDS;

    private final Object lock = new Object();
    
    public static final int NULL = -1;
    
    public static final int W_AWAY = 0;
    public static final int W_HOME = 1;
    public static final int W_TIE = 2;
    public static final int W_NONE = 3;

    public static final int X_FRIENDS = 1;
    public static final int X_INVITE = 2;

    public Prediction() {

    }
    
    public Prediction(JSONObject json) throws JSONException {
        userId = json.getString("user_id");
        gameId = json.getString("game_id");
        awayScore = json.getInt("away");
        homeScore = json.getInt("home");
    }

    public Prediction(String gid) {
        gameId = gid;
        awayScore = NULL;
        homeScore = NULL;
    }

    public Prediction(String userId, String gameId) {
        this.userId = userId;
        this.gameId = gameId;
    }

    public Prediction(String gid, int awayScore, int homeScore) {
        gameId = gid;
        awayScore = awayScore;
        homeScore = homeScore;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setAwayScore(int score) {
        awayScore = score;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setHomeScore(int score) {
        homeScore = score;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public int getSpread(Game game) {
        return Math.abs(awayScore - game.getAwayScore())
                + Math.abs(homeScore - game.getHomeScore());
    }

    /*
    public int getSpread(FullGame game) {
        return Math.abs(awayScore - game.getAwayScore()) + Math.abs(homeScore - game.getHomeScore().getScore());
    }
    */

    /*
    public int getSpread(Game game) {
        return Math.abs(awayScore - game.getAwayScore()) + Math.abs(homeScore - game.getHomeScore());
    }
    */

    public static int computeSpread(int awayPredictedScore, int homePredictedScore, int awayActualScore, int homeActualScore) {
        return Math.abs(awayPredictedScore - awayActualScore) + Math.abs(homePredictedScore - homeActualScore);
    }

    public int getExposure() {
        return exposure;
    }
    
    public boolean isComplete() {
        return awayScore != NULL && homeScore != NULL;
    }
    
    public int winner() {
        if (!isComplete())
            return W_NONE;
        
        int t = awayScore - homeScore;
        if (t > 0)
            return W_AWAY;
        if (t < 0)
            return W_HOME;
        return W_TIE;
    }

    public static int winner(int awayScore, int homeScore) {
        if (awayScore == NULL || homeScore == NULL)
            return W_NONE;

        int t = awayScore - homeScore;
        if (t > 0) return W_AWAY;
        if (t < 0) return W_HOME;
        return W_TIE;
    }

    public Object acquireLock() {
        return lock;
    }

    public void updateFromJson(JSONObject json) throws JSONException {
        awayScore = json.getInt("away");
        homeScore = json.getInt("home");
    }

    public static void disableUpdates(Prediction prediction) {
        synchronized (prediction.lock) {
            prediction.updatesDisabled = true;
        }
    }

    public static void enableUpdates(Prediction prediction) {
        synchronized (prediction.lock) {
            prediction.updatesDisabled = false;
        }
    }

    public static void disableUpdates(Collection<Prediction> predictions) {
        for (Prediction prediction : predictions) {
            disableUpdates(prediction);
        }
    }

    public static void enableUpdates(Collection<Prediction> predictions) {
        for (Prediction prediction : predictions) {
            enableUpdates(prediction);
        }
    }

    public boolean updatesDisabled() {
        return updatesDisabled;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return this.modified;
    }

    public void setOnServer(boolean onServer) {
        this.onServer = onServer;
    }

    public boolean isOnServer() {
        return this.onServer;
    }

    public Object getLock() {
        return lock;
    }

    public enum Visibility {
        FRIENDS,
        INVITE,
        PUBLIC
    }
}
