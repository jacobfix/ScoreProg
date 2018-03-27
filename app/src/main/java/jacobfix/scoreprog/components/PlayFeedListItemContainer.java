package jacobfix.scoreprog.components;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import jacobfix.scoreprog.NflTeamProvider;
import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ColorUtil;
import jacobfix.scoreprog.util.FontHelper;
import jacobfix.scoreprog.util.ViewUtil;

public class PlayFeedListItemContainer extends FrameLayout {

    private static final String TAG = PlayFeedListItemContainer.class.getSimpleName();

    private ConstraintLayout playContentContainer;
    private View teamStripe;
    private PlayFeedListItemDownContainer downContainer;
    private PlayFeedListItemDescriptionContainer descriptionContainer;

    private FrameLayout gameInstanceContainer;
    private TextView gameInstanceName;

    public PlayFeedListItemContainer(Context context) {
        this(context, null);
    }

    public PlayFeedListItemContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayFeedListItemContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        playContentContainer = ViewUtil.findById(this, R.id.play_content_container);
        teamStripe = ViewUtil.findById(this, R.id.stripe);
        downContainer = ViewUtil.findById(this, R.id.down_container);
        descriptionContainer = ViewUtil.findById(this, R.id.description_container);

        gameInstanceContainer = ViewUtil.findById(this, R.id.game_instance_container);
        gameInstanceName = ViewUtil.findById(this, R.id.game_instance_name);

        gameInstanceName.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        gameInstanceName.setTextColor(ColorUtil.STANDARD_TEXT);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        // Use the height of playContentContainer

        Log.d(TAG, "(" + suggestedWidth + ", " + suggestedHeight + ")");
        // setMeasuredDimension(suggestedWidth, suggestedHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setLayoutAsGameInstance(String instanceDescription) {
        gameInstanceName.setText(instanceDescription);

        playContentContainer.setVisibility(View.GONE);
        gameInstanceContainer.setVisibility(View.VISIBLE);
    }

    public void setLayoutAsPlayContent(int down, int toGo, String yardLine,
                                       String posTeam, String playTitle, String playDescription) {
        int color = NflTeamProvider.getTeamPrimaryColor(posTeam);
        teamStripe.setBackgroundColor(color);
        downContainer.setDownAndToGo(down, toGo);
        downContainer.setYardLine(yardLine);
        downContainer.color(color);
        descriptionContainer.setTitle(playTitle);
        descriptionContainer.setDescription(PlayFeedListItemDescriptionContainer
                .formatDescription(playDescription));

        if (down > 0) {
            downContainer.setLayoutAsDownToGoYardLine();
        } else {
            downContainer.setLayoutAsJustYardLine();
        }

        playContentContainer.setVisibility(View.VISIBLE);
        gameInstanceContainer.setVisibility(View.GONE);
    }

}
