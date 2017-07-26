package jacobfix.scorepredictor.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.res.ResourcesCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.R;

public class Util {

    private static final String TAG = Util.class.getSimpleName();

    public static String formatQuarter(Resources resources, int qtr) {
        switch (qtr) {
            case 1:
                return resources.getString(R.string.first_quarter);
            case 2:
                return resources.getString(R.string.second_quarter);
            case 3:
                return resources.getString(R.string.third_quarter);
            case 4:
                return resources.getString(R.string.fourth_quarter);
            case 5:
                return resources.getString(R.string.overtime);
            default:
                return resources.getString(R.string.empty_string);
        }
    }

    public static long millisSinceEpoch(Date date) {
        return date.getTime();
    }

    public static Date convertTimeZone(String from, String to) {
        return null;
    }

    public static int softenColor(int color) {
        return (color & 0x00FFFFFF) | 0xDA000000;
    }

    public static String getColorInfoString(Context context, @ColorRes int colorRes) {
        int color = ResourcesCompat.getColor(context.getResources(), colorRes, null);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return Integer.toHexString(color) + ", " + floatArrayToString(hsv);
    }

    public static float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private static String floatArrayToString(float[] array) {
        String s = new String();
        for (int i = 0; i < array.length - 1; i++) {
            s += array[i] + ", ";
        }
        s += array[array.length - 1];
        return s;
    }

    public static String readLocalFile(String path) throws IOException, JSONException {
        InputStream inputStream = ApplicationContext.getContext().getAssets().open(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String in = bufferedReader.readLine();

        inputStream.close();

        return in;
    }

    public static String join(List<String> strings, String delimiter) {
        StringBuilder builder = new StringBuilder();
        String toAppend = "";
        for (String string : strings) {
            builder.append(toAppend);
            builder.append(string);
            toAppend = delimiter;
        }
        return builder.toString();
    }
}
