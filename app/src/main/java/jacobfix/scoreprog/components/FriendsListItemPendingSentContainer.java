package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class FriendsListItemPendingSentContainer extends LinearLayout {

    private Button cancelRequestButton;

    public FriendsListItemPendingSentContainer(Context context) {
        this(context, null);
    }

    public FriendsListItemPendingSentContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendsListItemPendingSentContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        cancelRequestButton = ViewUtil.findById(this, R.id.cancel_request_button);
    }


}
