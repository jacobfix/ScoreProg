package jacobfix.scorepredictor.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Window;
import android.view.WindowManager;

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
import java.util.Random;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.AtomicGame;
import jacobfix.scorepredictor.R;
import jacobfix.scorepredictor.schedule.Schedule;
import jacobfix.scorepredictor.schedule.Season;

public class Util {

    private static final String TAG = Util.class.getSimpleName();

    // private static final String[] MONTHS = ApplicationContext.getContext().getResources().getStringArray(R.array.months);
    // private static final String[] MONTHS_ABBR = ApplicationContext.getContext().getResources().getStringArray(R.array.months_abbr);

    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public static final String[] MONTHS_SHORT = {"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"};

    public static String getWeekTitle(Season.WeekType weekType, int weekNumber) {
        switch(weekType) {
            case HOF:
                return "Hall of Fame";
            case PRE:
                return "Preseason Week " + weekNumber;
            case REG:
                return "Week " + weekNumber;
            case WC:
                return "Wild Card Weekend";
            case DIV:
                return "Divisional Playoffs";
            case CON:
                return "Conference Championships";
            case SB:
                return "Super Bowl";
        }
        return new String();
    }

    public static Season.WeekType parseWeekType(String type, int week) {
        switch(type) {
            case "PRE":
                if (week == 0) return Season.WeekType.HOF;
                return Season.WeekType.PRE;
            case "REG":
                return Season.WeekType.REG;
            case "WC":
                return Season.WeekType.WC;
            case "DIV":
                return Season.WeekType.DIV;
            case "CON":
                return Season.WeekType.CON;
            case "SB":
                return Season.WeekType.SB;
        }
        return null;
    }

    public static String getDateStringFromId(String gameId) {
        return getDateStringFromId(gameId, false);
    }

    public static String getDateStringFromId(String gameId, boolean monthsAbbr) {
        int year = Integer.parseInt(gameId.substring(0, 4));
        int month = Integer.parseInt(gameId.substring(4, 6));
        int day = Integer.parseInt(gameId.substring(6, 8));

        return String.format("%d/%d", month, day);
        /*
        String[] months = (monthsAbbr) ? MONTHS_ABBR : MONTHS;
        StringBuilder builder = new StringBuilder();
        builder.append(months[month - 1]);
        builder.append(" ");
        builder.append(day);
        builder.append(", ");
        builder.append(year);
        return builder.toString();
        */
        // return months[month - 1] + " " + day + ", " + year;
    }

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

    // TODO: Try making transparent
    public static void changeStatusBarColor(Activity activity, int color) {
        /*
        if (Build.VERSION.SDK_INT < 21)
            return;
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        */
    }

    public static String randomString(int length) {
        String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder result = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            result.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
        }

        return result.toString();
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

    public static String readLocalFile(Context context, String path) throws IOException {
        InputStream inputStream = context.getAssets().open(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String in = bufferedReader.readLine();

        inputStream.close();

        return in;
    }

    public static String readLocalFile(String path) throws IOException {
        return readLocalFile(ApplicationContext.getContext(), path);
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

    public static Schedule.Day parseDayOfWeek(String day) {
        switch (day) {
            case "Sun":
                return Schedule.Day.SUN;
            case "Mon":
                return Schedule.Day.MON;
            case "Tue":
                return Schedule.Day.TUE;
            case "Tues":
                return Schedule.Day.TUE;
            case "Wed":
                return Schedule.Day.WED;
            case "Thu":
                return Schedule.Day.THU;
            case "Thurs":
                return Schedule.Day.THU;
            case "Fri":
                return Schedule.Day.FRI;
            case "Sat":
                return Schedule.Day.SAT;
        }
        return null;
    }

    public static Season.SeasonType parseSeasonType(String type) {
        switch (type) {
            case "PRE":
                return Season.SeasonType.PRE;
            case "REG":
                return Season.SeasonType.REG;
            case "POST":
                return Season.SeasonType.POST;
        }
        return null;
    }

    public static String parseQuarter(int quarter) {
        switch (quarter) {
            case AtomicGame.Q_PREGAME:
                return "Pregame";
            case AtomicGame.Q_HALFTIME:
                return "Halftime";
            case AtomicGame.Q_FINAL:
                return "Final";
            case AtomicGame.Q_FINAL_OT:
                return "Final";
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
            case 4:
                return "4th";
            case 5:
                return "OT";
        }
        return null;
    }

    public static String getDayOfWeekString(Schedule.Day dow) {
        switch (dow) {
            case SUN:
                return "Sun";
            case MON:
                return "Mon";
            case TUE:
                return "Tue";
            case WED:
                return "Wed";
            case THU:
                return "Thu";
            case FRI:
                return "Fri";
            case SAT:
                return "Sat";
            default:
                throw new RuntimeException();
        }
    }

    public static String getDateString(String gameId) {
        int year = Integer.parseInt(gameId.substring(0, 4));
        int month = Integer.parseInt(gameId.substring(4, 6));
        int day = Integer.parseInt(gameId.substring(6, 8));

        // return String.format("%s %d", Util.MONTHS_SHORT[month - 1], day);
        return String.format("%d/%d", month, day);
    }

    public static String getQuarterString(int q) {
        switch (q) {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
            case 4:
                return "4th";
            case 5:
                return "OT";
            default:
                return "???";
        }
    }
}
