package jacobfix.scorepredictor.util;

import android.support.v4.content.ContextCompat;

import jacobfix.scorepredictor.ApplicationContext;
import jacobfix.scorepredictor.R;

/**
 * Created by jake on 8/25/17.
 */
public class ColorUtil {

    public static int WHITE = ContextCompat.getColor(ApplicationContext.getContext(), android.R.color.white);
    public static int STANDARD_TEXT = ContextCompat.getColor(ApplicationContext.getContext(), R.color.standard_text);
    public static int STANDARD_COLOR = ContextCompat.getColor(ApplicationContext.getContext(), R.color.standard_color);
    public static int TRANSPARENT = ContextCompat.getColor(ApplicationContext.getContext(), android.R.color.transparent);
    public static int LIGHT_RED = ContextCompat.getColor(ApplicationContext.getContext(), android.R.color.holo_red_light);

}
