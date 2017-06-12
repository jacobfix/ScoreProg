package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import jacobfix.scorepredictor.Play;
import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.server.JsonParser;
import jacobfix.scorepredictor.server.UserJsonRetriever;
import jacobfix.scorepredictor.users.User;

public class SyncUsersTask extends BaseTask<LinkedList<User>> {

    private static final String TAG = SyncUsersTask.class.getSimpleName();

    private Collection<User> mUsersToSync;
    private Collection<String> mGameIds;
    private boolean mGetFriends;

    private UserJsonRetriever mJsonRetriever;

    public SyncUsersTask(Collection<User> users, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        super(listener);
        mUsersToSync = users;
        mJsonRetriever = jsonRetriever;
    }

    public SyncUsersTask(Collection<User> users, Collection<String> gameIds, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        this(users, jsonRetriever, listener);
        mGameIds = gameIds;
    }

    @Override
    public void execute() {
        Log.d(TAG, "Executing SyncUsersTask");
        String[] userIds = new String[mUsersToSync.size()];
        int i = 0;
        for (User user : mUsersToSync)
            userIds[i++] = user.getId();

        try {
            JSONObject detailsJson = mJsonRetriever.getUserDetailsJson(userIds);

            JSONObject predictionsJson = null;
            String[] gameIds = null;
            if (mGameIds != null && !mGameIds.isEmpty()) {
                gameIds = new String[mGameIds.size()];
                i = 0;
                for (String gameId : mGameIds)
                    gameIds[i++] = gameId;
                predictionsJson = mJsonRetriever.getUserPredictionsJson(userIds, gameIds);
            }

            /* Should we iterate over the given list of users or over the users in the JSON result? */
            JSONObject userDetailsJson;
            for (User user : mUsersToSync) {
                userDetailsJson = detailsJson.optJSONObject(user.getId());
                if (userDetailsJson != null) {
                    synchronized (user) {
                        JsonParser.updateUserDetailsFromJson(userDetailsJson, user);
                    }
                }
            }

            if (predictionsJson != null) {
                /* Game IDs mapped to user IDs mapped to predictions. */
                /* {
                     game_id1 => {
                                   uid1 => {
                                            "away" => 17,
                                            "home" => 21
                                           },
                                   uid2 => {
                                            "away" => 0,
                                            "home" => 10,
                                           }
                                 }
                    }
                */
                JSONObject userPredictionsJson, individualUserPredictionJson;
                for (String gameId : gameIds) {
                    userPredictionsJson = predictionsJson.getJSONObject(gameId);

                    for (User user : mUsersToSync) {
                        individualUserPredictionJson = userPredictionsJson.optJSONObject(user.getId());

                        if (individualUserPredictionJson != null) {
                            synchronized (user) {
                                // JsonParser.updateUserPredictionsFromJson(individualUserPredictionJson, user);
                                user.getPredictions().put(gameId, new Prediction(individualUserPredictionJson.getInt("away"), individualUserPredictionJson.getInt("home")));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            reportError(e, TAG, TaskError.IO_ERROR, e.toString());
        } catch (JSONException e) {
            reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
        } catch (Exception e) {
            reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
        }
    }
}
