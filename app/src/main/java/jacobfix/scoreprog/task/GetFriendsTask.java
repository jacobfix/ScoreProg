package jacobfix.scoreprog.task;

import android.util.Log;

import org.json.JSONArray;

import jacobfix.scoreprog.Friends;
import jacobfix.scoreprog.RelationDetails;
import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.UserServerInterface;

public class GetFriendsTask extends BaseTask<Friends> {

    private static final String TAG = GetFriendsTask.class.getSimpleName();

    private int source;

    public static final int SOURCE_FRIENDS = 0;

    public GetFriendsTask(TaskFinishedListener listener) {
        super(listener);
        // this.source = source;
    }

    @Override
    public void execute() {
        try {
            Friends friends = new Friends();

            JSONArray friendsJson = UserServerInterface.getDefault().getFriends(UserServerInterface.FRIENDS_ALL);
            for (int i = 0; i < friendsJson.length(); i++) {
                RelationDetails relationDetails = new RelationDetails();
                JsonParser.updateRelationDetails(relationDetails, friendsJson.getJSONObject(i));

                switch (relationDetails.getRelationStatus()) {
                    case Friends.CONFIRMED:
                        friends.addRelatedUser(relationDetails);
                        break;

                    case Friends.PENDING_SENT:
                        friends.addRelatedUser(relationDetails);
                        break;

                    case Friends.PENDING_RECEIVED:
                        friends.addRelatedUser(relationDetails);
                        break;

                    case Friends.BLOCKED:
                        friends.addRelatedUser(relationDetails);
                        break;
                }
            }

            Log.d(TAG, "After getting friends CONFIRMED: " + friends.getConfirmedFriends());
            Log.d(TAG, "After getting friends PENDING: " + friends.getPendingFriends());
            setResult(friends);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
