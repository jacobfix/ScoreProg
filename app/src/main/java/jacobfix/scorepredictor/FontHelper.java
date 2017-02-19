package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.Typeface;

public class FontHelper {
    private static Typeface mWorkSansRegular;
    private static Typeface mWorkSansBold;
    private static Typeface mWorkSansMedium;
    private static Typeface mWorkSansThin;
    private static Typeface mYellowtailRegular;
    private static Typeface mArimoRegular;
    private static Typeface mArimoBold;
    private static Typeface mYantramanavRegular;
    private static Typeface mYantramanavMedium;
    private static Typeface mYantramanavBold;
    private static Typeface mYantramanavLight;
    private static Typeface mHindVadodaraRegular;
    private static Typeface mHindVadodaraMedium;

    private static Typeface getFont(Context context, String fontFile) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/" + fontFile);
    }

    public static Typeface getWorkSansRegular(Context context) {
        if (mWorkSansRegular == null) {
            mWorkSansRegular = getFont(context, "WorkSans-Regular.ttf");
        }
        return mWorkSansRegular;
    }

    public static Typeface getWorkSansBold(Context context) {
        if (mWorkSansBold == null) {
            mWorkSansBold = getFont(context, "WorkSans-Bold.ttf");
        }
        return mWorkSansBold;
    }

    public static Typeface getWorkSansMedium(Context context) {
        if (mWorkSansMedium == null) {
            mWorkSansMedium = getFont(context, "WorkSans-Medium.ttf");
        }
        return mWorkSansMedium;
    }

    public static Typeface getWorkSansThin(Context context) {
        if (mWorkSansThin == null) {
            mWorkSansThin = getFont(context, "WorkSans-Thin.ttf");
        }
        return mWorkSansThin;
    }

    public static Typeface getYellowTailRegular(Context context) {
        if (mYellowtailRegular == null) {
            mYellowtailRegular = getFont(context, "Yellowtail-Regular.ttf");
        }
        return mYellowtailRegular;
    }

    public static Typeface getYantramanavRegular(Context context) {
        if (mYantramanavRegular == null) {
            mYantramanavRegular = getFont(context, "Yantramanav-Regular.ttf");
        }
        return mYantramanavRegular;
    }

    public static Typeface getYantramanavMedium(Context context) {
        if (mYantramanavMedium == null) {
            mYantramanavMedium = getFont(context, "Yantramanav-Medium.ttf");
        }
        return mYantramanavMedium;
    }

    public static Typeface getYantramanavBold(Context context) {
        if (mYantramanavBold == null) {
            mYantramanavBold = getFont(context, "Yantramanav-Bold.ttf");
        }
        return mYantramanavBold;
    }

    public static Typeface getYantramanavLight(Context context) {
        if (mYantramanavLight == null) {
            mYantramanavLight = getFont(context, "Yantramanav-Light.ttf");
        }
        return mYantramanavLight;
    }

    public static Typeface getArimoRegular(Context context) {
        if (mArimoRegular == null) {
            mArimoRegular = getFont(context, "Arimo-Regular.ttf");
        }
        return mArimoRegular;
    }

    public static Typeface getArimoBold(Context context) {
        if (mArimoBold == null) {
            mArimoBold = getFont(context, "Arimo-Bold.ttf");
        }
        return mArimoBold;
    }

    public static Typeface getHindVadodaraRegular(Context context) {
        if (mHindVadodaraRegular == null) {
            mHindVadodaraRegular = getFont(context, "HindVadodara-Regular.ttf");
        }
        return mHindVadodaraRegular;
    }

    public static Typeface getHindVadodaraMedium(Context context) {
        if (mHindVadodaraMedium == null) {
            mHindVadodaraMedium = getFont(context, "HindVadodara-Medium.ttf");
        }
        return mHindVadodaraMedium;
    }
}
