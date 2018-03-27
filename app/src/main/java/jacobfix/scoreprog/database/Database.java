package jacobfix.scoreprog.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import jacobfix.scoreprog.NflGame;
import jacobfix.scoreprog.Prediction;


public class Database {

    private static final String TAG = Database.class.getSimpleName();

    private SQLiteDatabase mSqlDatabase;
    private SQLiteOpenHelper mDatabaseHelper;
    private Context mContext;

    private static final String DATABASE_NAME = "ScoreProgDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "scores_table";

    public static final String KEY_ROWID = "_id";
    public static final String KEY_GAMEID = "gameid";
    public static final String KEY_AWAY_NAME = "away_name";
    public static final String KEY_HOME_NAME = "home_name";
    public static final String KEY_AWAY_SCORE = "away_score";
    public static final String KEY_HOME_SCORE = "home_score";
    public static final String KEY_AWAY_PREDICTION = "away_prediction";
    public static final String KEY_HOME_PREDICTION = "home_prediction";

    private static final String WHERE_GAMEID = KEY_GAMEID + " = ?";

    private static final int NO_SCORE = -1;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "CREATING SQL DATABASE");
            db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_GAMEID + " TEXT NOT NULL, "
                    + KEY_AWAY_NAME + " TEXT NOT NULL, "
                    + KEY_HOME_NAME + " TEXT NOT NULL, "
                    + KEY_AWAY_SCORE + " INTEGER NOT NULL, "
                    + KEY_HOME_SCORE + " INTEGER NOT NULL, "
                    + KEY_AWAY_PREDICTION + " INTEGER NOT NULL, "
                    + KEY_HOME_PREDICTION + " INTEGER NOT NULL);"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public Database(Context context) {
        mContext = context;
    }

    public synchronized void open() {
        mDatabaseHelper = new DatabaseHelper(mContext);
        mSqlDatabase = mDatabaseHelper.getWritableDatabase();
    }

    public synchronized void close() {
        mDatabaseHelper.close();
        mSqlDatabase.close();
    }

    public synchronized long createEntry(NflGame game) {
        /* Adds the game to the database if it is not already present. */
        if (entryExists(game.getGameId()))
            return -1;

        ContentValues cv = new ContentValues();
        cv.put(KEY_GAMEID, game.getGameId());
        cv.put(KEY_AWAY_NAME, game.getAwayTeam().getAbbr());
        cv.put(KEY_HOME_NAME, game.getHomeTeam().getAbbr());
        cv.put(KEY_AWAY_SCORE, game.getAwayTeam().getScore());
        cv.put(KEY_HOME_SCORE, game.getHomeTeam().getScore());
        cv.put(KEY_AWAY_PREDICTION, NO_SCORE);
        cv.put(KEY_HOME_PREDICTION, NO_SCORE);
        return mSqlDatabase.insert(TABLE_NAME, null, cv);
    }

    public synchronized boolean entryExists(String gameId) {
        Cursor cursor = mSqlDatabase.query(TABLE_NAME, new String[]{KEY_ROWID}, WHERE_GAMEID, new String[]{gameId}, null, null, null);
        boolean success = cursor.moveToFirst();
        cursor.close();
        return success;
    }

    public synchronized void syncGameIfExists(NflGame game) {
        Cursor cursor = mSqlDatabase.query(TABLE_NAME, new String[]{KEY_AWAY_PREDICTION, KEY_HOME_PREDICTION}, WHERE_GAMEID, new String[]{game.getGameId()}, null, null, null);
        if (cursor.moveToFirst()) {
            Log.d(TAG, "Found " + game.getGameId() + " in the database");
            game.getAwayTeam().setPredictedScore(cursor.getInt(0));
            game.getHomeTeam().setPredictedScore(cursor.getInt(1));
        }
        cursor.close();
    }

    public synchronized void updatePrediction(String gameId, boolean isHome, int prediction) {
        ContentValues cv = new ContentValues();
        cv.put((isHome ? KEY_HOME_PREDICTION : KEY_AWAY_PREDICTION), prediction);
        mSqlDatabase.update(TABLE_NAME, cv, WHERE_GAMEID, new String[]{gameId});
        Log.d(TAG, "Updated prediction for " + gameId);
    }

    public synchronized void updatePrediction(String gameId, Prediction prediction) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_AWAY_PREDICTION, prediction.getAwayScore());
        cv.put(KEY_HOME_PREDICTION, prediction.getHomeScore());
    }
}
