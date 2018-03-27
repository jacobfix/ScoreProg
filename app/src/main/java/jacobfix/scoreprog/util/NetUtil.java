package jacobfix.scoreprog.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Map;

public class NetUtil {

    private static final String TAG = NetUtil.class.getSimpleName();

    public static String makeGetRequest(String url, Map<String, String> headers, Map<String, String[]> params) throws IOException {
        if (params != null)
            url = url.concat("?").concat(makeParamString(params));

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        if (headers != null)
            for (String header : headers.keySet())
                connection.setRequestProperty(header, headers.get(header));

        return getResponse(connection);
    }

    public static String makePostRequest(String url, Map<String, String> headers, Map<String, String[]> params) throws IOException {
        if (params == null)
            throw new IllegalArgumentException("params cannot be null for a POST request");

        String body = makeParamString(params);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");

        if (headers != null)
            for (String header : headers.keySet())
                connection.setRequestProperty(header, headers.get(header));

        connection.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
        outputStreamWriter.write(body);
        outputStreamWriter.flush();

        return getResponse(connection);
    }

    private static String getResponse(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        LinkedList<String> lines = new LinkedList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        String response = Util.join(lines, "\n");
        inputStream.close();

        connection.disconnect();

        if (response.substring(0, 3).equals("<br>")) {
            Log.d(TAG, "Matches!");
            Log.d(TAG, response);
        }
        return response;
    }

    private static String makeParamString(Map<String, String[]> params) throws IOException {
        StringBuilder paramString = new StringBuilder();
        for (String key : params.keySet()) {
            paramString.append(String.format("%s=", URLEncoder.encode(key, "UTF-8")));
            for (String value : params.get(key)) {
                paramString.append(String.format("%s_", URLEncoder.encode(value, "UTF-8")));
            }
            paramString.deleteCharAt(paramString.length() - 1);
            paramString.append("&");
        }
        paramString.deleteCharAt(paramString.length() - 1);
        return paramString.toString();
    }

    public static void addAuthHeader(Map<String, String> headers, String userId, String token) {
        headers.put("Authorization", "Token " + token);
    }
}
