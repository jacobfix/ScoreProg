package jacobfix.scorepredictor.server;

import org.json.JSONObject;

import java.util.Collection;

public abstract class UserJsonRetriever extends JsonRetriever {

    public abstract JSONObject getUsersJson(Collection<String> userIds);
}
