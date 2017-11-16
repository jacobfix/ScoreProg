package jacobfix.scorepredictor;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import jacobfix.scorepredictor.util.ViewUtil;

public class LobbyFragmentListItem extends ConstraintLayout {

    private static final String TAG = LobbyFragmentListItem.class.getSimpleName();

    public LobbyFragmentListItem(Context context) {
        this(context, null);
    }

    public LobbyFragmentListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LobbyFragmentListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);

        LinearLayout awayScoreContainer = ViewUtil.findById(this, R.id.away_score_container);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
