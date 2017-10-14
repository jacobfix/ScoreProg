package jacobfix.scorepredictor.task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.server.UserServerInterface;

public class FindMatchingUsernamesTask extends BaseTask<Map<String, String>> {

    private String partialUsername;

    public FindMatchingUsernamesTask(String partialUsername, TaskFinishedListener listener) {
        super(listener);
        this.partialUsername = partialUsername;
    }

    @Override
    public void execute() {
        try {
            JSONObject json = UserServerInterface.getDefault().findMatchingUsernames(partialUsername);
            if (!json.getBoolean("success")) {
                // TODO: Make a special method for interpreting server errors (ServerException)
                throw new RuntimeException("Server error: " + json.getInt("errno"));
            }

            JSONArray matches = json.getJSONArray("matches");
            HashMap<String, String> result = new HashMap<>();
            for (int i = 0; i < matches.length(); i++) {
                JSONObject pair = matches.getJSONObject(i);
                result.put(pair.getString("username"), pair.getString("userid"));
            }
            setResult(result);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
