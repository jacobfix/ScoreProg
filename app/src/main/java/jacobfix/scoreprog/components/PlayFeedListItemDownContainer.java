package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.Util;
import jacobfix.scoreprog.util.ViewUtil;

public class PlayFeedListItemDownContainer extends LinearLayout {

    private TextView upperLabel;
    private TextView lowerLabel;

    private String downAndToGo;
    private String yardLine;

    public PlayFeedListItemDownContainer(Context context) {
        this(context, null);
    }

    public PlayFeedListItemDownContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayFeedListItemDownContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        upperLabel = ViewUtil.findById(this, R.id.down_and_to_go);
        lowerLabel = ViewUtil.findById(this, R.id.yard_line);

        upperLabel.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        lowerLabel.setTypeface(FontHelper.getYantramanavRegular(getContext()));
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        int desiredWidth = suggestedWidth - getPaddingLeft() - getPaddingRight();

        ViewUtil.fitTextToWidth(upperLabel, desiredWidth, "9ww & 99");
        ViewUtil.fitTextToWidth(lowerLabel, desiredWidth, "WWW 99");

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setDownAndToGo(int down, int toGo) {
        downAndToGo = Util.formatDown(down) + " & " + toGo;
    }

    public void setYardLine(String yl) {
        yardLine = yl;
    }

    public void color(int color) {
        upperLabel.setTextColor(color);
        lowerLabel.setTextColor(color);
    }

    public void setLayoutAsDownToGoYardLine() {
        upperLabel.setText(downAndToGo);
        lowerLabel.setText(yardLine);
        lowerLabel.setVisibility(View.VISIBLE);
    }

    public void setLayoutAsJustYardLine() {
        upperLabel.setText(yardLine);
        lowerLabel.setVisibility(View.GONE);
    }

}
