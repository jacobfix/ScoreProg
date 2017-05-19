package jacobfix.scorepredictor.server;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import jacobfix.scorepredictor.ApplicationContext;

public abstract class JsonRetriever {

    private static final String TAG = JsonRetriever.class.getSimpleName();

    // TODO: Maybe we don't need this class, we can just put these methods in a Util class

    protected JSONArray getJsonArrayFromUrl(String url) throws JSONException, IOException {
        return new JSONArray(getJsonStringFromUrl(url));
    }

    protected JSONObject getJsonObjectFromUrl(String url) throws JSONException, IOException {
        return new JSONObject(getJsonStringFromUrl(url));
    }

    protected JSONArray getLocalJsonArray(String path) throws JSONException, IOException {
        return new JSONArray(getLocalJsonString(path));
    }

    protected JSONObject getLocalJsonObject(String path) throws JSONException, IOException {
        return new JSONObject(getLocalJsonString(path));
    }

    private String getJsonStringFromUrl(String url) throws JSONException, IOException {
        URL toConnectTo = new URL(url);
        URLConnection connection = toConnectTo.openConnection();

        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String jsonString = bufferedReader.readLine();

        inputStream.close();

        return jsonString;
    }

    protected String getLocalJsonString(String path) throws IOException, JSONException {
        InputStream inputStream = ApplicationContext.getContext().getAssets().open(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String jsonString = bufferedReader.readLine();

        inputStream.close();

        return jsonString;
    }
}
