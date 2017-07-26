package jacobfix.scorepredictor;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import jacobfix.scorepredictor.sync.UserProvider;
import jacobfix.scorepredictor.task.BaseTask;
import jacobfix.scorepredictor.task.RankPredictionsTask;
import jacobfix.scorepredictor.task.TaskFinishedListener;
import jacobfix.scorepredictor.users.User;
import jacobfix.scorepredictor.util.FontHelper;
import jacobfix.scorepredictor.util.ViewUtil;

public class FriendPredictionFragment extends Fragment implements GameStateChangeListener {

    private static final String TAG = FriendPredictionFragment.class.getSimpleName();

    /* This list will passed in by the activity in which this fragment resides, and it will already
       be sorted in the order that the friends are to appear in the ListView. */
    private ArrayList<User> mFriends = new ArrayList<>();

    private Map<String, User> allDetails = new HashMap<>();
    private ArrayList<Predictions> rankedPredictions = new ArrayList<>();

    private NflGame mGame;

    private RelativeLayout header;
    private TextView awayAbbr;
    private TextView homeAbbr;
    private TextView spreadAbbr;

    private ListView list;
    private FriendPredictionAdapter adapter;
    private LayoutInflater mInflater;

    private AsyncCallback<User[]> detailsSyncListener = new AsyncCallback<User[]>() {
        @Override
        public void onSuccess(User[] result) {

        }

        @Override
        public void onFailure(Exception e) {

        }
    };

    private AsyncCallback<Predictions[]> predictionsSyncListener = new AsyncCallback<Predictions[]>() {
        @Override
        public void onSuccess(Predictions[] result) {
            HashSet<String> toRetrieve = new HashSet<>(LocalAccountManager.getFriends());
            toRetrieve.add(LocalAccountManager.getId());
            UserProvider.getUserPredictions(mGame.getGameId(), toRetrieve, new AsyncCallback<Map<String, Predictions>>() {
                @Override
                public void onSuccess(final Map<String, Predictions> result) {
                    /* If game is not pregame, rank and display the predictions. Otherwise, just show which users have
                    predicted. */
                    Log.d(TAG, "User predictions were retrieved: " + result.toString());
                    Log.d(TAG, "Retrieved user predictions size: " + result.size());
                    /* Rank the predictions, and collect the user details. */

                    new RankPredictionsTask(mGame, result.values(), new TaskFinishedListener() {
                        @Override
                        public void onTaskFinished(BaseTask task) {
                            rankedPredictions.clear();
                            rankedPredictions.addAll((ArrayList<Predictions>) task.getResult());

                            UserProvider.getUserDetails(result.keySet(), new AsyncCallback<Map<String, User>>() {
                                @Override
                                public void onSuccess(Map<String, User> result) {
                                    allDetails = result;
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(Exception exception) {
                                    Log.e(TAG, exception.toString());
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onFailure(Exception exception) {
                    Log.e(TAG, exception.toString());
                }
            });
        }

        @Override
        public void onFailure(Exception exception) {

        }
    };

    public static FriendPredictionFragment newInstance() {
        return new FriendPredictionFragment();
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach()");
        super.onAttach(context);
        mGame = ((GameActivity) context).getGame();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_prediction_list_new, container, false);

        /* Find the views. */
        header = ViewUtil.findById(view, R.id.header);
        setHeaderColor(((GameActivity) getContext()).getScoreboardColor() & 0xc0ffffff);

        awayAbbr = ViewUtil.findById(view, R.id.away_abbr);
        homeAbbr = ViewUtil.findById(view, R.id.home_abbr);
        spreadAbbr = ViewUtil.findById(view, R.id.spread_abbr);

        // TODO: Make the color of the header containing the abbreviations a lighter version of the current scoreboard color

        awayAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        homeAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));
        spreadAbbr.setTypeface(FontHelper.getYantramanavRegular(getContext()));

        awayAbbr.setText(mGame.getAwayTeam().getAbbr());
        homeAbbr.setText(mGame.getHomeTeam().getAbbr());

        list = ViewUtil.findById(view, R.id.list);

        View emptyView = createEmptyView(inflater, container);
        list.setEmptyView(emptyView);

        adapter = new FriendPredictionAdapter();
        list.setAdapter(adapter);

        mInflater = inflater;

        return view;
    }

    private View createEmptyView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_friend_prediction_list_empty, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        ((GameActivity) getContext()).registerGameStateChangeListener(this);
        // UserProvider.registerDetailsSyncListener(detailsSyncListener);
        Log.d(TAG, "Registering prediction sync listener");
        // UserProvider.registerPredictionsSyncListener(predictionsSyncListener);
        Log.d(TAG, "Listeners registered");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        ((GameActivity) getContext()).unregisterGameStateChangeListener(this);
        // UserProvider.unregisterDetailsSyncListener(detailsSyncListener);
        // UserProvider.unregisterPredictionsSyncListener(predictionsSyncListener);
        Log.d(TAG, "Listeners unregistered");
    }

    public void setHeaderColor(int color) {
        header.setBackgroundColor(color);
    }

    @Override
    public void onGameStateChanged(NflGame game) {
        mGame = game;
        awayAbbr.setText(game.getAwayTeam().getAbbr());
        homeAbbr.setText(game.getHomeTeam().getAbbr());
    }

    @Override
    public void onPredictionChanged(Prediction p) {

    }

    public ArrayList<Predictions> getRankedPredictions() {
        return rankedPredictions;
    }

    class FriendPredictionAdapter extends BaseAdapter {

        int colors[] = {android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light, android.R.color.holo_purple};
        Random random = new Random();

        int currentRank = 0;
        int previousSpread = -1;

        @Override
        public int getCount() {
            Log.d(TAG, "Predictions size: " + rankedPredictions.size());
            if (allDetails.size() != rankedPredictions.size()) {
                Log.d(TAG, "ALL DETAILS SIZE DID NOT MATCH");
                return 0;
            }
            Log.d(TAG, "ALL DETAILS SIZE MATCHED");
            return rankedPredictions.size();
        }

        @Override
        public Predictions getItem(int position) {
            return rankedPredictions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_friend_prediction_new_new, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.findViews(convertView);

                holder.ranking.setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.username.setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.ranking.setTextColor(ContextCompat.getColor(getContext(), R.color.standard_text));
                holder.username.setTextColor(ContextCompat.getColor(getContext(), R.color.standard_text));

                holder.awayFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.homeFlipCard.getTextView().setTypeface(FontHelper.getYantramanavRegular(getContext()));
                holder.spread.setTypeface(FontHelper.getYantramanavRegular(getContext()));

                ViewUtil.applyDeboss(holder.awayFlipCard.getTextView());
                ViewUtil.applyDeboss(holder.homeFlipCard.getTextView());
                ViewUtil.applyDeboss(holder.spread);

                convertView.setTag(holder);
            }

            Predictions predictions = getItem(position);
            User user = allDetails.get(predictions.getId());

            ViewHolder holder = (ViewHolder) convertView.getTag();

            // holder.icon.setColorFilter(ContextCompat.getColor(getContext(), colors[position % 4]));
            // holder.icon.setColorFilter(ContextCompat.getColor(getContext(), colors[random.nextInt(4)]));
            holder.username.setText(user.getUsername());

            int white = ContextCompat.getColor(getContext(), android.R.color.white);
            int standardText = ContextCompat.getColor(getContext(), R.color.standard_text);

            if (mGame.isPregame()) {
                /* Show the users who have submitted predictions, but don't display their predictions. */
                holder.awayFlipCard.setVisibility(View.INVISIBLE);
                holder.homeFlipCard.setVisibility(View.INVISIBLE);
                holder.spread.setVisibility(View.INVISIBLE);
                holder.spreadStatus.setVisibility(View.INVISIBLE);

                holder.ranking.setText("");
            } else {
                /* Display predictions. */
                holder.awayFlipCard.setVisibility(View.VISIBLE);
                holder.homeFlipCard.setVisibility(View.VISIBLE);
                holder.spread.setVisibility(View.VISIBLE);
                holder.spreadStatus.setVisibility(View.VISIBLE);

                if (user.getId().equals(LocalAccountManager.getId()))
                    holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.me_in_ranking));
                else
                    holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.friend_prediction_list_item_background));

                Prediction p = predictions.get(mGame.getGameId());
                holder.awayFlipCard.setScore(p.getAwayScore());
                holder.homeFlipCard.setScore(p.getHomeScore());

                int spread = p.getSpread(mGame);
                holder.spread.setText(String.valueOf(spread));
                holder.spreadStatus.setProgress(100 - spread);

                if (p.getAwayScore() == p.getHomeScore()) {
                    holder.awayFlipCard.strokedBackground(standardText, standardText);
                    holder.homeFlipCard.strokedBackground(standardText, standardText);
                    holder.spreadStatus.getProgressDrawable().setColorFilter(standardText, PorterDuff.Mode.SRC_IN);
                } else if (p.getAwayScore() > p.getHomeScore()) {
                    holder.awayFlipCard.solidBackground(white, mGame.getAwayTeam().getPrimaryColor());
                    // holder.homeFlipCard.noBackground(standardText);
                    holder.homeFlipCard.strokedBackground(standardText, standardText);
                    holder.spreadStatus.getProgressDrawable().setColorFilter(mGame.getAwayTeam().getPrimaryColor(), PorterDuff.Mode.SRC_IN);
                } else {
                    // holder.awayFlipCard.noBackground(standardText);
                    holder.awayFlipCard.strokedBackground(standardText, standardText);
                    holder.homeFlipCard.solidBackground(white, mGame.getHomeTeam().getPrimaryColor());
                    holder.spreadStatus.getProgressDrawable().setColorFilter(mGame.getHomeTeam().getPrimaryColor(), PorterDuff.Mode.SRC_IN);
                }

                holder.ranking.setText(String.valueOf(position + 1));
            }

            /* Highlight in yellow the user's position within the ranking. */
            /* if (UserOracle.getInstance().isMe(friend)) {
                holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.me_in_ranking));
            } else {
                holder.root.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.friend_prediction_list_item_background));
            }*/
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
