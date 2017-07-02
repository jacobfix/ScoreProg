package jacobfix.scorepredictor.task;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import jacobfix.scorepredictor.UserJsonParser;
import jacobfix.scorepredictor.server.UserJsonRetriever;
import jacobfix.scorepredictor.users.User;

public class SyncUsersTask extends BaseTask {

    private static final String TAG = SyncUsersTask.class.getSimpleName();

    // private User[] mUsersToSync;
    // private String[] mGameIds;
    private Collection<User> mUsersToSync;
    private Collection<String> mGameIds;

    private UserJsonRetriever mJsonRetriever;

/*    public SyncUsersTask(User[] users, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        super(listener);
        mUsersToSync = users;
        mJsonRetriever = jsonRetriever;
    }
    */
/*
    public SyncUsersTask(User[] users, String[] gameIds, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        this(users, jsonRetriever, listener);
        // mGameIds = gameIds;
    }
*/
    public SyncUsersTask(Collection<User> users, Collection<String> gameIds, UserJsonRetriever jsonRetriever, TaskFinishedListener listener) {
        // this(users.toArray(new User[users.size()]), gameIds.toArray(new String[gameIds.size()]), jsonRetriever, listener);
        // Log.d(TAG, "In constructor: " + gameIds.toString());
        super(listener);
        mUsersToSync = users;
        mGameIds = gameIds;
        mJsonRetriever = jsonRetriever;
    }

    @Override
    public void execute() {
        Log.d(TAG, "Executing SyncUsersTask with...");
        Log.d(TAG, "Users: " + mUsersToSync.toString());
        if (mGameIds != null)
            Log.d(TAG, "Games: " + mGameIds.toString());
        else
            Log.d(TAG, "They're null");
        String[] userIds = new String[mUsersToSync.size()];
        int i = 0;
        // for (int i = 0; i < mUsersToSync.size(); i++)
        //    userIds[i] = mUsersToSync..getId();
        for (User user : mUsersToSync)
            userIds[i++] = user.getId();

        ArrayList<String> updatedInfo;
        ArrayList<String> updatedPredictions;
        /* Might get a network error. */
        try {
            JSONObject infoJson = mJsonRetriever.getUserInfoJson(userIds);
            if (!infoJson.getBoolean("success")) {
                Log.d(TAG, "Unsuccessful user sync!");
                return;
            }
            infoJson = infoJson.getJSONObject("users");

            User usersToSync[] = mUsersToSync.toArray(new User[mUsersToSync.size()]);

            updatedInfo = UserJsonParser.getInstance().updateUserInfo(usersToSync, infoJson);
            if (updatedInfo.size() != userIds.length) {
                /* Not all of the users had their information successfully updated. */
            }

            if (mGameIds == null || mGameIds.size() == 0)
                return;

            String gamesToSync[] = mGameIds.toArray(new String[mGameIds.size()]);

            Log.d(TAG, "Doing the predictions");
            // TODO: Maybe predictions should be retrieved one game at a time!
            JSONObject predictionsJson = mJsonRetriever.getUserPredictionsJson(userIds, gamesToSync);
            if (!predictionsJson.getBoolean("success")) {
                Log.d(TAG, "Unsuccessful prediction sync!");
                return;
            }
            predictionsJson = predictionsJson.getJSONObject("predictions");

            updatedPredictions = UserJsonParser.getInstance().updateUserPredictions(usersToSync, predictionsJson);
            if (updatedPredictions.size() != userIds.length) {
                /* Not all of the users had their predictions successfully updated. */
            }

        } catch (JSONException e) {
            reportError(e, TAG, TaskError.JSON_ERROR, e.toString());
        } catch (IOException e) {
            reportError(e, TAG, TaskError.IO_ERROR, e.toString());
        } catch (Exception e) {
            reportError(e, TAG, TaskError.UNKNOWN_ERROR, e.toString());
        } finally {
            /* We can tell if the info sync or the prediction sync completely failed by
               checking if updatedInfo/updatedPredictions is null. */
        }
    }
}
