package jacobfix.scoreprog.components;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import jacobfix.scoreprog.R;
import jacobfix.scoreprog.util.ViewUtil;

public class FriendsListItemPendingReceivedContainer extends LinearLayout {

    private OnClickListener listItemClickListener;

    private Button acceptRequestButton;
    private Button rejectRequestButton;

    public FriendsListItemPendingReceivedContainer(Context context) {
        this(context, null);
    }

    public FriendsListItemPendingReceivedContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendsListItemPendingReceivedContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        acceptRequestButton = ViewUtil.findById(this, R.id.accept_request_button);
        rejectRequestButton = ViewUtil.findById(this, R.id.reject_request_button);

        acceptRequestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        rejectRequestButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
