package jacobfix.scoreprog.sync;

import org.json.JSONException;
import org.json.JSONObject;

public interface Syncable {

    void sync(JSONObject json) throws JSONException;
}
