package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class PlayFeedListItemDescriptionContainer extends LinearLayout {

    private static final String TAG = PlayFeedListItemDescriptionContainer.class.getSimpleName();

    private TextView title;
    private TextView description;

    public PlayFeedListItemDescriptionContainer(Context context) {
        this(context, null);
    }

    public PlayFeedListItemDescriptionContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayFeedListItemDescriptionContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        title = ViewUtil.findById(this, R.id.title);
        description = ViewUtil.findById(this, R.id.description);

        title.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        description.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        title.setTextColor(ColorUtil.STANDARD_TEXT);
        description.setTextColor(ColorUtil.STANDARD_TEXT);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        Log.d(TAG, "(" + suggestedWidth + ", " + suggestedHeight + ")");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

    public static String formatDescription(String description) {
        return description.replaceAll(" *\\([^\\(\\)]*\\) *", "");
        // return description.replaceAll("\\(.*\\)", "");
    }
}
