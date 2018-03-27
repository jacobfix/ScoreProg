package jacobfix.scoreprog;


import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import jacobfix.scoreprog.components.FriendsListItemConfirmedContainer;
import jacobfix.scoreprog.components.FriendsListItemPendingReceivedContainer;
import jacobfix.scoreprog.components.FriendsListItemPendingSentContainer;
import jacobfix.scoreprog.util.ViewUtil;

public class FriendsListItem extends ConstraintLayout {

    private TextView username;

    private FriendsListItemConfirmedContainer confirmedContainer;
    private FriendsListItemPendingReceivedContainer pendingReceivedContainer;
    private FriendsListItemPendingSentContainer pendingSentContainer;

    public FriendsListItem(Context context) {
        this(context, null);
    }

    public FriendsListItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendsListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public enum State {
        CONFIRMED,
        PENDING_RECEIVED,
        PENDING_SENT
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        username = ViewUtil.findById(this, R.id.username);
        confirmedContainer = ViewUtil.findById(this, R.id.confirmed_container);
        pendingReceivedContainer = ViewUtil.findById(this, R.id.pending_received_container);
        pendingSentContainer = ViewUtil.findById(this, R.id.pending_sent_container);
    }

    public void setState(State state) {
        switch (state) {
            case CONFIRMED:
                confirmedContainer.setVisibility(View.VISIBLE);
                pendingReceivedContainer.setVisibility(View.GONE);
                pendingSentContainer.setVisibility(View.GONE);
                break;

            case PENDING_RECEIVED:
                confirmedContainer.setVisibility(View.GONE);
                pendingReceivedContainer.setVisibility(View.VISIBLE);
                pendingSentContainer.setVisibility(View.GONE);
                break;

            case PENDING_SENT:
                confirmedContainer.setVisibility(View.GONE);
                pendingReceivedContainer.setVisibility(View.GONE);
                pendingSentContainer.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setUsername(String username) {
        this.username.setText(username);
    }
}
