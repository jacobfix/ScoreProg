package jacobfix.scorepredictor.deprecated;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;

import jacobfix.scorepredictor.Prediction;
import jacobfix.scorepredictor.server.JsonParser;
import jacobfix.scorepredictor.server.UserJsonRetriever;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;

public class OriginalSyncUsersTask extends BaseTask {

    private static final String TAG = OriginalSyncUsersTask.class.getSimpleName();

    private Collection<User> mUsersToSync;
    private Collection<String> mGameIds;
    private boolean mGetFriends;

    private UserJsonRetriever mJsonRetriever;

    public OriginalSyncUsersTask(Collection<User> users, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        super(listener);
        mUsersToSync = users;
        mJsonRetriever = jsonRetriever;
    }

    public OriginalSyncUsersTask(Collection<User> users, Collection<String> gameIds, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        this(users, jsonRetriever, listener);
        mGameIds = gameIds;
    }

    @Override
    public void execute() {
        String[] userIds = new String[mUsersToSync.size()];
        Log.d(TAG, "Executing OriginalSyncUsersTask on");
        int i = 0;
        for (User user : mUsersToSync) {
            Log.d(TAG, "USER: " + user.getId());
            userIds[i++] = user.getId();
        }
        Log.d(TAG, "Done");
        try {
            JSONObject detailsJson = mJsonRetriever.getUserInfoJson(userIds);

            Log.d(TAG, "detailsJson: ");
            Log.d(TAG, detailsJson.toString());

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
            detailsJson = detailsJson.getJSONObject("users");
            for (User user : mUsersToSync) {
                Log.d(TAG, "Updating " + user.getId());
                userDetailsJson = detailsJson.optJSONObject(user.getId());
                if (userDetailsJson != null) {
                    Log.d(TAG, "About to update user details");
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
