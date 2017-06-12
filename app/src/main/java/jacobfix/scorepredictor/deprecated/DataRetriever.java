package jacobfix.scorepredictor.deprecated;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public abstract class DataRetriever {

    private static final String TAG = DataRetriever.class.getSimpleName();

    protected Context mContext;

    public DataRetriever(Context context) {
        mContext = context;
    }

    public abstract JSONObject getAllGamesJson() throws IOException, JSONException;

    public abstract JSONObject getIndividualGameJson(String gameId) throws IOException, JSONException;

    protected JSONObject getJsonFromUrl(String url) throws IOException, JSONException {
        URL toConnectTo = new URL(url);
        URLConnection connection = toConnectTo.openConnection();

        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        JSONObject data = new JSONObject(bufferedReader.readLine());

        inputStream.close();

        return data;
    }

    protected JSONObject getLocalJson(String location) throws IOException, JSONException {
        InputStream inputStream = mContext.getAssets().open(location);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject json = new JSONObject(bufferedReader.readLine());
        inputStream.close();
        return json;
    }
}
