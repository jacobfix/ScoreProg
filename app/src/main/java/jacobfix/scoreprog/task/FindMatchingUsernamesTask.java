package jacobfix.scoreprog.task;

import org.json.JSONArray;

import java.util.HashSet;

import jacobfix.scoreprog.LocalAccountManager;
import jacobfix.scoreprog.RelationDetails;
import jacobfix.scoreprog.server.JsonParser;
import jacobfix.scoreprog.server.UserServerInterface;


public class FindMatchingUsernamesTask extends BaseTask<HashSet<RelationDetails>> {

    private static final String TAG = FindMatchingUsernamesTask.class.getSimpleName();

    private String partialUsername;

    public FindMatchingUsernamesTask(String partialUsername, TaskFinishedListener listener) {
        super(listener);
        this.partialUsername = partialUsername;
    }

    @Override
    public void execute() {
        try {
            HashSet<RelationDetails> suggestions = new HashSet<>();

            JSONArray suggestionsJson = UserServerInterface.getDefault().getMatchingUsernames(partialUsername);
            for (int i = 0; i < suggestionsJson.length(); i++) {
                RelationDetails suggestion = JsonParser.createFindUsersSuggestion(suggestionsJson.getJSONObject(i));

                if (suggestion.getUserId().equals(LocalAccountManager.get().userId))
                    continue;

                suggestion.setRelationStatus(LocalAccountManager.get().friends().getRelationStatus(suggestion.getUserId()));
                suggestions.add(suggestion);
            }
            setResult(suggestions);
        } catch (Exception e) {
            reportError(e);
        }
    }
}
