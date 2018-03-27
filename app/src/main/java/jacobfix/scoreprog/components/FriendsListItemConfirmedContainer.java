package jacobfix.scoreprog.components;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class FriendsListItemConfirmedContainer extends LinearLayout {

    private ImageView deleteFriendButton;

    public FriendsListItemConfirmedContainer(Context context) {
        this(context, null);
    }

    public FriendsListItemConfirmedContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendsListItemConfirmedContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        deleteFriendButton = ViewUtil.findById(this, R.id.delete_friend_button);
    }
}
