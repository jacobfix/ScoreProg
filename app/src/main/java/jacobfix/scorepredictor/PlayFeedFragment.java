package jacobfix.scorepredictor;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.Util;
import jacobfix.scorepredictor.util.ViewUtil;

public class PlayFeedFragment extends Fragment {

    private static final String TAG = PlayFeedFragment.class.getSimpleName();

    // OriginalFullGame mGame;
    ArrayList<Play> mPlayFeed;
    PlayFeedAdapter mPlayFeedAdapter;
    LayoutInflater mInflater;

    public static PlayFeedFragment newInstance() {
        return new PlayFeedFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // mGame = ((GameActivity) context).getFullGame();
        // mPlayFeed = mGame.getDriveFeed().getPlayFeed();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        View view = inflater.inflate(R.layout.fragment_play_feed, container, false);
        mPlayFeedAdapter = new PlayFeedAdapter();
        ((ListView) ViewUtil.findById(view, R.id.plays_list)).setAdapter(mPlayFeedAdapter);
        mInflater = inflater;
        return view;
    }

    public void update() {
        mPlayFeedAdapter.notifyDataSetChanged();
    }

    class PlayFeedAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mPlayFeed.size();
        }

        @Override
        public Play getItem(int position) {
            return mPlayFeed.get(getCount() - 1 - position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.play_feed_item, parent, false);

                ViewHolder holder = new ViewHolder();

                holder.findViews(convertView);
                holder.setTypefaces(getContext());
                // holder.initializeStripes(mGame.getAwayTeam().getPrimaryColor(), mGame.getHomeTeam().getPrimaryColor());
                convertView.setTag(holder);
            }
            Play play = getItem(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();

            /*
            holder.normal.setVisibility(View.VISIBLE);
            holder.special.setVisibility(View.GONE);
            if (play.getPosTeamId() == mGame.getAwayTeam().getId()) {
                holder.awayTeamStripe.setVisibility(View.VISIBLE);
                holder.homeTeamStripe.setVisibility(View.INVISIBLE);
            } else if (play.getPosTeamId() == mGame.getHomeTeam().getId()) {
                holder.awayTeamStripe.setVisibility(View.INVISIBLE);
                holder.homeTeamStripe.setVisibility(View.VISIBLE);
            } else {
                // Should do something when creating play to check if it's for a timeout, end quarter, whatever
                holder.normal.setVisibility(View.GONE);
                holder.special.setVisibility(View.VISIBLE);
                holder.specialText.setText(play.getDescription());
                // holder.description.setText("");
            }
            */

            setSideText(holder.sideTextLeft, play);
            for (int i = 0; i < play.numSequenceItems(); i++) {
                if (play.getSequenceItem(i).hasPlayer() && play.getSequenceItem(i).getStatId() <= 120) {
                    holder.title.setText(String.format(StatProvider.getTitle(play.getSequenceItem(i).getStatId()), play.getSequenceItem(i).getYards()));
                    break;
                }
            }
            // holder.title.setText("Incomplete Pass");
            holder.description.setText(play.getDescription());
            // holder.description.setText(play.getDescription());
            return convertView;
        }

        private void setSideText(TextView sideText, Play play) {
            if (play.getDown() == 0) {
                if (play.getYardLine().isEmpty()) {
                    // Timeout, end of quarter
                } else {
                    String[] p = play.getYardLine().split(" ");
                    sideText.setText(p[0] + "\n" + p[1]);
                }
            } else {
                sideText.setText(Util.formatQuarter(getResources(), play.getDown()) + "\n" + play.getYardsToGo());
            }
        }
    }

    public interface PlayFeedFragmentListener {
        ArrayList<Play> getPlayFeed();
    }

    static class ViewHolder {
        RelativeLayout normal;
        FrameLayout special;
        TextView specialText;
        View awayTeamStripe;
        View homeTeamStripe;
        TextView sideTextLeft;
        LinearLayout middleTextContainer;
        TextView title;
        TextView description;

        void findViews(View parent) {
            normal = ViewUtil.findById(parent, R.id.normal);
            special = ViewUtil.findById(parent, R.id.special);
            specialText = ViewUtil.findById(parent, R.id.special_text);
            awayTeamStripe = ViewUtil.findById(parent, R.id.away_team_stripe);
            homeTeamStripe = ViewUtil.findById(parent, R.id.home_team_stripe);
            sideTextLeft = ViewUtil.findById(parent, R.id.side_text_left);
            middleTextContainer = ViewUtil.findById(parent, R.id.middle_text_container);
            title = ViewUtil.findById(parent, R.id.title);
            description = ViewUtil.findById(parent, R.id.description);
        }

        void setTypefaces(Context context) {
            sideTextLeft.setTypeface(FontHelper.getArimoRegular(context));
            title.setTypeface(FontHelper.getArimoRegular(context));
            description.setTypeface(FontHelper.getArimoRegular(context));
        }

        void initializeStripes(int awayTeamColor, int homeTeamColor) {
            awayTeamStripe.setBackgroundColor(awayTeamColor);
            homeTeamStripe.setBackgroundColor(homeTeamColor);
        }
    }
}