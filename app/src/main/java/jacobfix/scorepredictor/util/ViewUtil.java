package jacobfix.scorepredictor.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class ViewUtil {

    private static final float SATURATION_SCALE_FACTOR = 0.50f;

    public static <T extends View> T findById(@NonNull View parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    public static <T extends View> T findById(@NonNull Activity parent, @IdRes int resId) {
        return (T) parent.findViewById(resId);
    }

    public static void initializeToolbar(AppCompatActivity activity, Toolbar toolbar) {
        toolbar.setPadding(0, getStatusBarHeight(activity), 0, 0);
        toolbar.getLayoutParams().height += getStatusBarHeight(activity);

        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static void setCompoundDrawablesColor(TextView view, int color) {
        for (Drawable d : view.getCompoundDrawables())
            if (d != null) d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    public static float convertDpToPx(Resources resources, int dp) {
        // return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return dp * resources.getDisplayMetrics().density;
    }

    public static int convertPxToDp(Resources resources, float px) {
        return (int) (px / resources.getDisplayMetrics().density);
    }

    /*
    public void fitToText(View v, Paint paint, String boundsString, int padding) {
        Rect boundingRect = new Rect();
        paint.getTextBounds(boundsString, 0, boundsString.length(), boundingRect);
        int length = (boundingRect.width() > boundingRect.height()) ? boundingRect.width() : boundingRect.height();
        length += (2 * ViewUtil.convertDpToPx(getResources(), this.padding));
        background.getLayoutParams().width = length;
        background.getLayoutParams().height = length;
        background.requestLayout();
    }
    */

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

    public static void applyFilter(TextView textView, float[] direction, float ambient, float specular, float blurRadius) {
        textView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        EmbossMaskFilter filter = new EmbossMaskFilter(direction, ambient, specular, blurRadius);
        textView.getPaint().setMaskFilter(filter);
    }

    public static void applyDeboss(TextView textView) {
        applyFilter(textView, new float[]{0f, -1f, 0.5f}, 0.8f, 15f, 1f);
    }

    public static int getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resId);
    }


}
