package jacobfix.scorepredictor;

import android.content.res.Resources;
import android.support.annotation.StringRes;

public class Util {

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

    public static int softenColor(int color) {
        return (color & 0x00FFFFFF) | 0xDA000000;
    }
}
