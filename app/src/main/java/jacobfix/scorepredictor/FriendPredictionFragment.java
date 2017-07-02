package jacobfix.scorepredictor;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import jacobfix.scorepredictor.components.FlipCardView;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.sync.UserOracle;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class FriendPredictionFragment extends Fragment {

    private static final String TAG = FriendPredictionFragment.class.getSimpleName();

    /* This list will passed in by the activity in which this fragment resides, and it will already
       be sorted in the order that the friends are to appear in the ListView. */
    private ArrayList<User> mFriends = new ArrayList<>();

    private NflGame mGame;

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
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        mGame = ((GameProvider) context).getGame();
        mFriends = ((GameProvider) context).getRankedParticipants();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_prediction_list_new, container, false);

        /* Find the views. */
        mAwayAbbr = ViewUtil.findById(view, R.id.away_abbr);
        mHomeAbbr = ViewUtil.findById(view, R.id.home_abbr);
        mSpreadAbbr = ViewUtil.findById(view, R.id.spread_abbr);

        mAwayAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        mHomeAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        mSpreadAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        mAwayAbbr.setText(mGame.getAwayTeam().getAbbr());
        mHomeAbbr.setText(mGame.getHomeTeam().getAbbr());

        mList = ViewUtil.findById(view, R.id.list);
        // mAdapter = new FriendPredictionAdapter();
        mAdapter = new FriendPredictionAdapter();
        mList.setAdapter(mAdapter);

        mInflater = inflater;

        return view;
    }

    /* This is effectively the "update" method for the list of friends. */
    public void setFriends(ArrayList<User> friends) {
        if (mAdapter != null) {
            mFriends = friends;
            mAdapter.notifyDataSetChanged();
        }
    }

    class FriendPredictionAdapter extends BaseAdapter {

        int colors[] = {android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light, android.R.color.holo_purple};

        int currentRank = 0;
        int previousSpread = -1;

        @Override
        public int getCount() {
            Log.d(TAG, "FriendPredictionAdapter getCount()");
            Log.d(TAG, String.valueOf(mFriends.size()));
            Log.d(TAG, "In getCount(): " + mFriends.toString());
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

                holder.ranking.setTypeface(FontHelper.getYantramanavRegular(getContext()));

                holder.awayFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.homeFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.spread.setTypeface(FontHelper.getYantramanavRegular(getContext()));

                ViewUtil.applyDeboss(holder.awayFlipCard.getTextView());
                ViewUtil.applyDeboss(holder.homeFlipCard.getTextView());
                ViewUtil.applyDeboss(holder.spread);

                convertView.setTag(holder);
                // TODO: Set typefaces too
            }

            User friend = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();

            /* Highlight in yellow the user's position within the ranking. */
            if (UserOracle.getInstance().isMe(friend)) {
                holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.me_in_ranking));
            } else {
                holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.friend_prediction_list_item_background));
            }

            holder.username.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

            // holder.icon.setImageDrawable(friend.getIcon());
            holder.icon.setColorFilter(ContextCompat.getColor(getContext(), colors[position % 4]));
            holder.username.setText(friend.getUsername().substring(0, 5));

            Prediction p = friend.getPrediction(mGame.getGameId());
            holder.awayFlipCard.getTextView().setText(String.valueOf(p.getAwayScore()));
            holder.homeFlipCard.getTextView().setText(String.valueOf(p.getHomeScore()));
            // int spread = Math.abs(p.getAwayScore() - mGame.getAwayTeam().getScore()) + Math.abs(p.getHomeScore() - mGame.getHomeTeam().getScore());
            int spread = p.getSpread(mGame);
            holder.spread.setText(String.valueOf(p.getSpread(mGame)));
            holder.spreadStatus.setProgress(100 - spread);

            holder.ranking.setText(String.valueOf(position + 1));
            /*
            if (spread != previousSpread)
                currentRank++;
            previousSpread = spread;
            */

            // holder.ranking.setText(String.valueOf(currentRank));

            if (p.getAwayScore() > p.getHomeScore()) {
                // holder.awayFlipCard.showBackground();
                holder.awayFlipCard.solidBackground(ContextCompat.getColor(getContext(), android.R.color.white), mGame.getAwayTeam().getPrimaryColor());
                // holder.awayFlipCard.getTextView().setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                // holder.awayFlipCard.setColor(mGame.getAwayTeam().getPrimaryColor());

                holder.homeFlipCard.noBackground(ContextCompat.getColor(getContext(), R.color.standard_text));
                // holder.homeFlipCard.transparentBackground(ContextCompat.getColor(getContext(), R.color.standard_text), mGame.getHomeTeam().getPrimaryColor());
                // holder.homeFlipCard.getTextView().setTextColor(ContextCompat.getColor(getContext(), R.color.standard_text));

                holder.spreadStatus.setProgressTintList(ColorStateList.valueOf(mGame.getAwayTeam().getPrimaryColor()));
            } else {
                // holder.awayFlipCard.setColor(mGame.getAwayTeam().getPrimaryColor());
                // holder.awayFlipCard.setAlpha(0.5f);
                holder.awayFlipCard.noBackground(ContextCompat.getColor(getContext(), R.color.standard_text));
                // holder.awayFlipCard.transparentBackground(ContextCompat.getColor(getContext(), R.color.standard_text), mGame.getAwayTeam().getPrimaryColor());
                // holder.awayFlipCard.getTextView().setTextColor(ContextCompat.getColor(getContext(), R.color.standard_text));

                holder.homeFlipCard.solidBackground(ContextCompat.getColor(getContext(), android.R.color.white), mGame.getHomeTeam().getPrimaryColor());
                // holder.homeFlipCard.getTextView().setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
                // holder.homeFlipCard.setColor(mGame.getHomeTeam().getPrimaryColor());

                holder.spreadStatus.setProgressTintList(ColorStateList.valueOf(mGame.getHomeTeam().getPrimaryColor()));
            }

            return convertView;
        }
    }

    static class ViewHolder {
        LinearLayout root;
        TextView ranking;
        ImageView icon;
        TextView username;
        LinearLayout record;

        PredictionView awayFlipCard;
        PredictionView homeFlipCard;
        TextView spread;
        ProgressBar spreadStatus;

        void findViews(View parent) {
            root = ViewUtil.findById(parent, R.id.root);
            ranking = ViewUtil.findById(parent, R.id.ranking);
            icon = ViewUtil.findById(parent, R.id.icon);
            username = ViewUtil.findById(parent, R.id.username);
            // record = ViewUtil.findById(parent, R.id.record);

            awayFlipCard = ViewUtil.findById(parent, R.id.away_prediction);
            homeFlipCard = ViewUtil.findById(parent, R.id.home_prediction);
            spread = ViewUtil.findById(parent, R.id.spread);
            spreadStatus = ViewUtil.findById(parent, R.id.spread_progress_bar);

            awayFlipCard.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            homeFlipCard.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            awayFlipCard.resize();
            homeFlipCard.resize();
        }
    }
}
