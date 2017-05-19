package jacobfix.scorepredictor.util;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;

public class ViewUtil {

    private static final float SATURATION_SCALE_FACTOR = 0.50f;

    public static <T extends View> T findById(@NonNull View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    public static <T extends View> T findById(@NonNull Activity parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    public static float convertDpToPx(Resources resources, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static void setTwoLayerRectangleColor(Drawable rect, int color) {
        /*
        rect.findDrawableByLayerId(R.id.background).setColorFilter(color, PorterDuff.Mode.SRC_IN);
        float hsv[] = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] *= SATURATION_SCALE_FACTOR;
        rect.findDrawableByLayerId(R.id.foreground).setColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_IN);
        */
        /*
        float hsv[] = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] *= SATURATION_SCALE_FACTOR;
        rect.findDrawableByLayerId(R.id.foreground).setColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_IN);
        hsv[1] = 1.0f;
        rect.findDrawableByLayerId(R.id.background).setColorFilter(Color.HSVToColor(hsv), PorterDuff.Mode.SRC_IN);
        */
        int reduced = (color & 0x00FFFFFF)| 0x88000000;
        //rect.findDrawableByLayerId(R.id.foreground).setColorFilter(reduced, PorterDuff.Mode.SRC_IN);
        rect.setColorFilter(reduced, PorterDuff.Mode.SRC_IN);
    }
}