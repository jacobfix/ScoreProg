package jacobfix.scoreprog.util;

import android.support.v4.content.ContextCompat;

import jacobfix.scoreprog.ApplicationContext;
import jacobfix.scoreprog.R;

/**
 * Created by jake on 8/25/17.
 */
public class ColorUtil {

    public static final int WHITE = ContextCompat.getColor(ApplicationContext.getContext(), android.R.color.white);
    public static final int STANDARD_TEXT = ContextCompat.getColor(ApplicationContext.getContext(), R.color.standard_text);
    public static final int STANDARD_COLOR = ContextCompat.getColor(ApplicationContext.getContext(), R.color.standard_color);
    public static final int TRANSPARENT = ContextCompat.getColor(ApplicationContext.getContext(), android.R.color.transparent);
    public static final int LIGHT_RED = ContextCompat.getColor(ApplicationContext.getContext(), android.R.color.holo_red_light);
    public static final int ERROR_RED = ContextCompat.getColor(ApplicationContext.getContext(), R.color.error_red);

    public static final int DEFAULT_SCOREBOARD_COLOR = ContextCompat.getColor(ApplicationContext.getContext(), R.color.default_scoreboard_color);

}
