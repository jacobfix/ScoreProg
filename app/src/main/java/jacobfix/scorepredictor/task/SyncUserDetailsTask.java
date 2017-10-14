package jacobfix.scorepredictor.task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jacobfix.scorepredictor.server.UserServerInterface;
import jacobfix.scorepredictor.users.UserDetails;

public class SyncUserDetailsTask extends BaseTask<ArrayList<UserDetails>> {

    private Collection<UserDetails> userDetails;

    public SyncUserDetailsTask(Collection<UserDetails> userDetails, TaskFinishedListener listener) {
        super(listener);
        this.userDetails = userDetails;
    }

    @Override
    public void execute() {
        ArrayList<UserDetails> result = new ArrayList<>();
        try {
            ArrayList<String> userIds = new ArrayList<>();
            for (UserDetails ud : userDetails)
                userIds.add(ud.getId());

            JSONObject response = UserServerInterface.getDefault().getUserDetailsJson(userIds);
            if (response.getBoolean("success")) {
                JSONObject allUserDetailsJson = response.getJSONObject("users");

                for (UserDetails details : userDetails) {
                    JSONObject userDetailsJson = allUserDetailsJson.optJSONObject(details.getId());
                    if (userDetailsJson != null) {
                        details.sync(userDetailsJson);
                        result.add(details);
                    }
                }
            }
            setResult(result);
        } catch (Exception e) {
            reportError(e);
            e.printStackTrace();
        } // TODO: setResult() in finally?
    }
}
