package jacobfix.scorepredictor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.sync.UserOracle;
import jacobfix.scorepredictor.util.ViewUtil;

public class FriendPredictionFragment extends Fragment {

    private static final String TAG = FriendPredictionFragment.class.getSimpleName();

    /* This list will passed in by the activity in which this fragment resides, and it will already
       be sorted in the order that the friends are to appear in the ListView. */
    private ArrayList<User> mFriends = new ArrayList<>();

    private TextView mAwayAbbr;
    private TextView mHomeAbbr;
    private TextView mSpreadAbbr;

    private ListView mList;
    private FriendPredictionAdapter mAdapter;

    private LayoutInflater mInflater;

    public static FriendPredictionFragment newInstance() {
        return new FriendPredictionFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_prediction_list, container, false);

        /* Find the views. */
        mAwayAbbr = ViewUtil.findById(view, R.id.away_abbr);
        mHomeAbbr = ViewUtil.findById(view, R.id.home_abbr);
        mSpreadAbbr = ViewUtil.findById(view, R.id.spread_abbr);

        mList = ViewUtil.findById(view, R.id.list);
        mAdapter = new FriendPredictionAdapter();
        mList.setAdapter(mAdapter);

        mInflater = inflater;

        return view;
    }

    /* This is effectively the "update" method for the list of friends. */
    public void setFriends(ArrayList<User> friends) {
        mFriends = friends;
        mAdapter.notifyDataSetChanged();
    }

    class FriendPredictionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFriends.size();
        }

        @Override
        public User getItem(int position) {
            return mFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_friend_prediction, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.findViews(convertView);
                convertView.setTag(holder);
            }

            User friend = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();

            /* Highlight in yellow the user's position within the ranking. */
            if (UserOracle.getInstance().isMe(friend)) {
                holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.me_in_ranking));
            } else {
                holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.friend_prediction_list_header_background));
            }

            // TODO: In the case of a tie, both users should have the same ranking
            holder.ranking.setText(String.valueOf(position + 1));

            // holder.icon.setImageDrawable(friend.getIcon());
            holder.username.setText(friend.getUsername());

            return convertView;
        }
    }

    static class ViewHolder {
        LinearLayout root;
        TextView ranking;
        ImageView icon;
        TextView username;
        TextView medalsOrWhatever;

        FlipCardView awayFlipCard;
        FlipCardView homeFlipCard;
        TextView spread;

        void findViews(View parent) {
            root = ViewUtil.findById(parent, R.id.root);
            ranking = ViewUtil.findById(parent, R.id.ranking);
            icon = ViewUtil.findById(parent, R.id.icon);
            username = ViewUtil.findById(parent, R.id.username);
            medalsOrWhatever = ViewUtil.findById(parent, R.id.extra);
        }
    }
}
