package jacobfix.scorepredictor.task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import jacobfix.scorepredictor.sync.JsonProvider;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.users.UserDetails;

public class SyncDetailsTask extends BaseTask<User[]> {

    private LinkedList<User> users;

    public SyncDetailsTask(Collection<User> requested, TaskFinishedListener listener) {
        super(listener);
        users = new LinkedList<>(requested);
    }

    @Override
    public void execute() {
        LinkedList<String> ids = new LinkedList<>();
        for (User user : users)
            ids.add(user.getId());

        User[] result = new User[users.size()];
        int i = 0;

        try {
            JSONObject json = JsonProvider.get().getDetailsJson(ids);

            for (User user : users) {
                JSONObject detailsJson = json.optJSONObject(user.getId());
                if (detailsJson != null) {
                    user.sync(detailsJson);
                    result[i++] = user;
                }
            }
        } catch (IOException e) {

        } catch (JSONException e) {

        } finally {
            setResult(result);
        }
    }
}
