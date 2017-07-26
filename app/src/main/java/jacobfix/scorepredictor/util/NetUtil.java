package jacobfix.scorepredictor.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;

public class NetUtil {

    private static final String TAG = NetUtil.class.getSimpleName();

    public static String makePostRequest(String url, Map<String, String> params) throws IOException, JSONException {
        URLConnection connection = new URL(url).openConnection();

        /* Assemble the request body containing the POST parameters. */
        StringBuilder body = new StringBuilder();
        for (String param : params.keySet()) {
            body.append(String.format("%s=%s&", URLEncoder.encode(param, "UTF-8"), URLEncoder.encode(params.get(param), "UTF-8")));
        }
        body.deleteCharAt(body.length() - 1);

        connection.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
        outputStreamWriter.write(body.toString());
        outputStreamWriter.flush();

        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        LinkedList<String> lines = new LinkedList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        String response = Util.join(lines, "\n");
        // String response = bufferedReader.readLine();

        inputStream.close();

        // return new JSONObject(jsonString);
        return response;
    }

    public static String makeGetRequest(String url) throws IOException {
        URL toConnectTo = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) toConnectTo.openConnection();

        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        // String response = bufferedReader.readLine();
        LinkedList<String> lines = new LinkedList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        String response = Util.join(lines, "\n");

        inputStream.close();

        connection.getResponseCode();

        return response;
    }

    public static String makeGetRequest(String url, Map<String, String[]> params) throws IOException {
        StringBuilder query = new StringBuilder("?");
        for (String key : params.keySet()) {
            query.append(String.format("%s=", URLEncoder.encode(key, "UTF-8")));
            for (String value : params.get(key)) {
                query.append(String.format("%s_", URLEncoder.encode(value, "UTF-8")));
            }
            query.deleteCharAt(query.length() - 1);
        }
        url = url + query.toString();
        return makeGetRequest(url);
    }

    public static JSONArray getJsonArrayFromUrl(String url) throws JSONException, IOException {
        return new JSONArray(getJsonStringFromUrl(url));
    }

    public static JSONObject getJsonObjectFromUrl(String url) throws JSONException, IOException {
        return new JSONObject(getJsonStringFromUrl(url));
    }

    public static JSONArray getLocalJsonArray(String path) throws JSONException, IOException {
        return new JSONArray(getLocalJsonString(path));
    }

    public static JSONObject getLocalJsonObject(String path) throws JSONException, IOException {
        return new JSONObject(getLocalJsonString(path));
    }

    private static String getJsonStringFromUrl(String url) throws JSONException, IOException {
        URL toConnectTo = new URL(url);
        URLConnection connection = toConnectTo.openConnection();

        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String jsonString = bufferedReader.readLine();

        inputStream.close();

        return jsonString;
    }

    private static String getLocalJsonString(String path) throws IOException, JSONException {
        InputStream inputStream = ApplicationContext.getContext().getAssets().open(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String jsonString = bufferedReader.readLine();

        inputStream.close();

        return jsonString;
    }
}
