package jacobfix.scoreprog.util;

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
    private static Typeface mYantramanavThin;
    private static Typeface mHindVadodaraRegular;
    private static Typeface mHindVadodaraMedium;
    private static Typeface mPermanentMarker;
    private static Typeface mLobsterRegular;
    private static Typeface mAntonRegular;
    private static Typeface mBitterRegular;
    private static Typeface mBitterBold;
    private static Typeface mNewsCycleRegular;
    private static Typeface mNewsCycleBold;
    private static Typeface mAbelRegular;
    private static Typeface mAssistantRegular;
    private static Typeface mPlayRegular;
    private static Typeface mPlayBold;
    private static Typeface mFjallaOneRegular;
    private static Typeface mSonsieOneRegular;

    private static Typeface mNflFont;
    private static Typeface mNflFontBold;

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

    public static Typeface getYantramanavThin(Context context) {
        if (mYantramanavThin == null) {
            mYantramanavThin = getFont(context, "Yantramanav-Thin.ttf");
        }
        return mYantramanavThin;
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
    
    public static Typeface getPermanentMarker(Context context) {
        if (mPermanentMarker == null) {
            mPermanentMarker = getFont(context, "PermanentMarker.ttf");
        }
        return mPermanentMarker;
    }

    public static Typeface getLobsterRegular(Context context) {
        if (mLobsterRegular == null) {
            mLobsterRegular = getFont(context, "Lobster-Regular.ttf");
        }
        return mLobsterRegular;
    }

    public static Typeface getAntonRegular(Context context) {
        if (mAntonRegular == null) {
            mAntonRegular = getFont(context, "Anton-Regular.ttf");
        }
        return mAntonRegular;
    }

    public static Typeface getBitterRegular(Context context) {
        if (mBitterRegular == null)
            mBitterRegular = getFont(context, "Bitter-Regular.ttf");
        return mBitterRegular;
    }

    public static Typeface getBitterBold(Context context) {
        if (mBitterBold == null)
            mBitterBold = getFont(context, "Bitter-Bold.ttf");
        return mBitterBold;
    }

    public static Typeface getNewsCycleRegular(Context context) {
        if (mNewsCycleRegular == null)
            mNewsCycleRegular = getFont(context, "NewsCycle-Regular.ttf");
        return mNewsCycleRegular;
    }

    public static Typeface getNewsCycleBold(Context context) {
        if (mNewsCycleBold == null)
            mNewsCycleBold = getFont(context, "NewsCycle-Bold.ttf");
        return mNewsCycleRegular;
    }

    public static Typeface getAbelRegular(Context context) {
        if (mAbelRegular == null)
            mAbelRegular = getFont(context, "Abel-Regular.ttf");
        return mAbelRegular;
    }

    public static Typeface getAssistantRegular(Context context) {
        if (mAssistantRegular == null)
            mAssistantRegular = getFont(context, "Assistant-Regular.ttf");
        return mAssistantRegular;
    }

    public static Typeface getNflFont(Context context) {
        if (mNflFont == null)
            mNflFont = getFont(context, "medium-cond.ttf");
        return mNflFont;
    }

    public static Typeface getNflFontBold(Context context) {
        if (mNflFontBold == null) {
            mNflFontBold = getFont(context, "bold.ttf");
        }
        return mNflFontBold;
    }

    public static Typeface getFjallOneRegular(Context context) {
        if (mFjallaOneRegular == null)
            mFjallaOneRegular = getFont(context, "FjallaOne-Regular.ttf");
        return mFjallaOneRegular;
    }

    public static Typeface getPlayRegular(Context context) {
        if (mPlayRegular == null)
            mPlayRegular = getFont(context, "Play-Regular.ttf");
        return mPlayRegular;
    }

    public static Typeface getPlayBold(Context context) {
        if (mPlayBold == null)
            mPlayBold = getFont(context, "Play-Bold.ttf");
        return mPlayBold;
    }

    public static Typeface getSonsieOneRegular(Context context) {
        if (mSonsieOneRegular == null)
            mSonsieOneRegular = getFont(context, "SonsieOne-Regular.ttf");
        return mSonsieOneRegular;
    }
}
