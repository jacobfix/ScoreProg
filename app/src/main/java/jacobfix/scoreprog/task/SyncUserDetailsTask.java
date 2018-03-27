package jacobfix.scoreprog.task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.UserServerInterface;
import jacobfix.scoreprog.users.UserDetails;

public class SyncUserDetailsTask extends BaseTask<Collection<UserDetails>> {

    private static final String TAG = SyncUserDetailsTask.class.getSimpleName();

    private Collection<UserDetails> userDetails;

    public SyncUserDetailsTask(Collection<UserDetails> userDetails, TaskFinishedListener listener) {
        super(listener);
        this.userDetails = new HashSet<>(userDetails);
    }

    @Override
    public void execute() {
        Collection<UserDetails> result = new HashSet<>();
        try {
            Map<String, UserDetails> userDetailsMap = new HashMap<>();
            for (UserDetails details : userDetails)
                userDetailsMap.put(details.getUserId(), details);

            JSONArray allUserDetailsJson = UserServerInterface.getDefault().getUserDetails(userDetailsMap.keySet());
            for (int i = 0; i < allUserDetailsJson.length(); i++) {
                JSONObject individualUserDetailsJson = allUserDetailsJson.getJSONObject(i);
                UserDetails individualUserDetails = userDetailsMap.get(individualUserDetailsJson.getString("user_id").toLowerCase());
                JsonParser.updateUserDetails(individualUserDetails, individualUserDetailsJson);
                result.add(individualUserDetails);
            }
            setResult(result);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
