package jacobfix.scorepredictor.sync;

import org.json.JSONException;
import org.json.JSONObject;

public interface Syncable {

    void sync(JSONObject json) throws JSONException;
}
